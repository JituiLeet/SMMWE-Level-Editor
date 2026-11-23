import {newLevel,validateLevel,TILE_TYPES} from "../core/model.js";
import {History} from "../core/history.js";
import {inspectSwe,encodeSwe,cloneSweRecord} from "../core/swe.js";
if(!Array.prototype.find)Array.prototype.find=function(fn,ctx){for(var i=0;i<this.length;i++)if(fn.call(ctx,this[i],i,this))return this[i];};
if(!NodeList.prototype.forEach)NodeList.prototype.forEach=Array.prototype.forEach;
if(!Element.prototype.replaceChildren)Element.prototype.replaceChildren=function(){while(this.firstChild)this.removeChild(this.firstChild);for(var i=0;i<arguments.length;i++)this.appendChild(arguments[i]);};
function readFile(file,asBuffer){return new Promise(function(resolve,reject){var r=new FileReader();r.onload=function(){resolve(r.result);};r.onerror=function(){reject(r.error||new Error("文件读取失败"));};if(asBuffer)r.readAsArrayBuffer(file);else r.readAsText(file);});}
const $=id=>document.getElementById(id),canvas=$("board"),ctx=canvas.getContext("2d"),view=$("viewport");
let level=newLevel(),tool="paint",selected="ground",zoom=1,offset={x:28,y:24},pointerDown=false,space=false,last="",sweDoc=null,sweTemplates=null,sweLayer="S2",sweId="49";
const history=new History();
const clone=v=>JSON.parse(JSON.stringify(v));
function state(){return {level:clone(level),sweDoc:sweDoc?clone(sweDoc):null,sweTemplates:sweTemplates?clone(sweTemplates):null,sweLayer,sweId};}
function restore(v){level=v.level;sweDoc=v.sweDoc;sweTemplates=v.sweTemplates||null;sweLayer=v.sweLayer||"S2";sweId=v.sweId||"49";uiFromLevel();syncSweUI();draw();}
function checkpoint(){history.checkpoint(state());}
function metadataFromUI(){
  level.metadata={name:$("name").value,author:$("author").value,description:$("description").value,tags:$("tags").value.split(",").map(x=>x.trim()).filter(Boolean)};
  if(sweDoc){const meta=sweDoc.S0.S1&&sweDoc.S0.S1[0];if(meta){meta.user=level.metadata.author;meta.desc=level.metadata.description;}}
}
function uiFromLevel(){
  $("name").value=level.metadata.name||"";$("author").value=level.metadata.author||"";$("description").value=level.metadata.description||"";$("tags").value=(level.metadata.tags||[]).join(",");
  $("width").value=level.grid.width;$("height").value=level.grid.height;$("tileSize").value=level.grid.tileSize;
}
function palette(){
  $("palette").replaceChildren();
  for(const t of TILE_TYPES){const b=document.createElement("button");b.className="tile"+(selected===t.id?" selected":"");const sw=document.createElement("span");sw.className="swatch";sw.style.background=t.color;sw.textContent=t.glyph;b.appendChild(sw);b.appendChild(document.createTextNode(t.name));b.onclick=()=>{selected=t.id;tool="paint";syncTools();palette()};$("palette").append(b);}
}
function syncTools(){document.querySelectorAll(".tool").forEach(b=>b.classList.toggle("selected",b.dataset.tool===tool));}
function currentArray(){return sweDoc&&sweDoc.S0&&sweDoc.S0[sweLayer]||[];}
function recordId(r){return r.ID||((sweLayer==="S2")?String(r.i):"platform");}
function syncSweUI(){
  $("sweTools").hidden=!sweDoc;$("palette").hidden=!!sweDoc;$("tileSize").disabled=!!sweDoc;
  if(!sweDoc){$("exportSwe").disabled=true;return;}
  $("sweLayer").value=sweLayer;$("sweId").value=sweId;
  const records=(sweTemplates&&sweTemplates[sweLayer])||currentArray(),ids=[];records.forEach(function(r){var id=recordId(r);if(id&&ids.indexOf(id)<0)ids.push(id);});ids.sort();
  $("sweIds").replaceChildren();for(const id of ids){const o=document.createElement("option");o.value=id;$("sweIds").appendChild(o);}
  $("exportSwe").disabled=false;
  $("sweInfo").textContent="HMAC 校验通过。S0.S2 地形："+(sweDoc.S0.S2?sweDoc.S0.S2.length:0)+" 条；当前层 "+sweLayer+"："+currentArray().length+" 条。未知字段与其他数据组保留。";
}
function resizeCanvas(){const d=window.devicePixelRatio||1,w=view.clientWidth,h=view.clientHeight;canvas.width=Math.round(w*d);canvas.height=Math.round(h*d);canvas.style.width=w+"px";canvas.style.height=h+"px";ctx.setTransform(d,0,0,d,0,0);draw();}
function draw(){
  const w=view.clientWidth,h=view.clientHeight,s=level.grid.tileSize*zoom;ctx.clearRect(0,0,w,h);ctx.save();ctx.translate(offset.x,offset.y);ctx.strokeStyle="#30394b";ctx.lineWidth=1;
  const sx=Math.max(0,Math.floor(-offset.x/s)),sy=Math.max(0,Math.floor(-offset.y/s));
  for(let x=sx;x<=level.grid.width;x++){ctx.beginPath();ctx.moveTo(x*s,0);ctx.lineTo(x*s,level.grid.height*s);ctx.stroke();}
  for(let y=sy;y<=level.grid.height;y++){ctx.beginPath();ctx.moveTo(0,y*s);ctx.lineTo(level.grid.width*s,y*s);ctx.stroke();}
  if(sweDoc){
    for(const r of currentArray()){if(!Number.isFinite(r.xx)||!Number.isFinite(r.yy))continue;const x=(r.xx/16)*s,y=(r.yy/16)*s;let color="#72809a",label="";
      if(sweLayer==="S2"){color="hsl("+((Number(r.i)*47)%360)+" 42% 48%)";label=String(r.i);}
      else if(sweLayer==="S4"){color="#c75d68";label=(r.ID||"OBJ").replace("obj_","").slice(0,5);}
      else if(sweLayer==="S3"){color="#9b75c7";label="DEC";}
      else if(sweLayer==="S5"){color="#4f9eb5";label="PLT";}
      else if(sweLayer==="S8"){color="#d49a4d";label="DOOR";}
      ctx.fillStyle=color;ctx.fillRect(x+1,y+1,s-2,s-2);if(s>15){ctx.fillStyle="#fff";ctx.textAlign="center";ctx.textBaseline="middle";ctx.font="bold "+Math.max(8,s*.32)+"px sans-serif";ctx.fillText(label,x+s/2,y+s/2);}
    }
  }else{
    for(const p of level.tiles){const t=TILE_TYPES.find(v=>v.id===p.type);if(!t)continue;const x=p.x*s,y=p.y*s;ctx.fillStyle=t.color;ctx.fillRect(x+1,y+1,s-2,s-2);if(t.glyph){ctx.fillStyle="#fff";ctx.textAlign="center";ctx.textBaseline="middle";ctx.font="bold "+Math.max(9,s*.45)+"px sans-serif";ctx.fillText(t.glyph,x+s/2,y+s/2);}}
  }
  ctx.restore();
}
function coords(e){const r=canvas.getBoundingClientRect(),s=level.grid.tileSize*zoom;return{x:Math.floor((e.clientX-r.left-offset.x)/s),y:Math.floor((e.clientY-r.top-offset.y)/s)};}
function editSwe(x,y){
  const arr=currentArray(),xx=x*16,yy=y*16;
  const idx=arr.findIndex(r=>Math.round(r.xx/16)===x&&Math.round(r.yy/16)===y);
  const removed=idx>=0?arr[idx]:null;
  if(idx>=0)arr.splice(idx,1);
  if(tool==="erase")return;
  if(sweLayer==="S2"){
    const id=Number(sweId);if(!Number.isInteger(id)||!((sweTemplates&&sweTemplates.S2||[]).some(r=>r.i===id))){$("sweInfo").textContent="此样本中没有该地形 ID。请从 ID 列表选择已观察到的编号。";return;}
    var tileTemplate=removed||((sweTemplates&&sweTemplates.S2||[]).find(function(r){return r.i===id;}))||((sweTemplates&&sweTemplates.S2||[])[0]);
    var tileRecord=tileTemplate?cloneSweRecord(tileTemplate):{xx:xx,yy:yy,i:id};
    tileRecord.xx=xx;tileRecord.yy=yy;tileRecord.i=id;arr.push(tileRecord);return;
  }
  const templates=sweTemplates&&sweTemplates[sweLayer]||[];
  let proto;
  if(sweLayer==="S5")proto=removed||arr[0]||templates[0];
  else proto=templates.find(r=>recordId(r)===sweId)||removed||arr[0]||templates[0];
  if(!proto){$("sweInfo").textContent="当前数据组没有可复制的样本记录，无法安全新建此类对象。";return;}
  const rec=cloneSweRecord(proto),dx=xx-(Number(proto.xx)||0),dy=yy-(Number(proto.yy)||0);
  rec.xx=xx;rec.yy=yy;
  if(sweLayer==="S8"){if(Number.isFinite(rec.dr_x))rec.dr_x+=dx;if(Number.isFinite(rec.dr_y))rec.dr_y+=dy;}
  arr.push(rec);
}
function apply(e){
  const c=coords(e);if(c.x<0||c.y<0||c.x>=level.grid.width||c.y>=level.grid.height)return;const k=c.x+":"+c.y;if(k===last)return;last=k;
  if(sweDoc)editSwe(c.x,c.y);
  else{level.tiles=level.tiles.filter(p=>p.x!==c.x||p.y!==c.y);if(tool==="paint")level.tiles.push({x:c.x,y:c.y,type:selected});}
  draw();
}
function startPointer(e,capture){
  if(capture&&e.pointerId!==undefined)canvas.setPointerCapture(e.pointerId);
  pointerDown=true;last="";
  if(tool==="select"||space||e.button===1){canvas.dataset.pan="1";canvas.dataset.lastX=e.clientX;canvas.dataset.lastY=e.clientY;return;}
  checkpoint();apply(e);
}
function movePointer(e){
  if(!pointerDown)return;
  if(canvas.dataset.pan){const dx=e.clientX-Number(canvas.dataset.lastX||e.clientX),dy=e.clientY-Number(canvas.dataset.lastY||e.clientY);offset.x+=dx;offset.y+=dy;canvas.dataset.lastX=e.clientX;canvas.dataset.lastY=e.clientY;draw();}
  else apply(e);
}
function pointerEnd(){pointerDown=false;last="";delete canvas.dataset.pan;delete canvas.dataset.lastX;delete canvas.dataset.lastY;}
if(window.PointerEvent){
  canvas.addEventListener("pointerdown",function(e){startPointer(e,true);});
  canvas.addEventListener("pointermove",movePointer);
  canvas.addEventListener("pointerup",pointerEnd);
  canvas.addEventListener("pointercancel",pointerEnd);
}else{
  canvas.addEventListener("mousedown",function(e){startPointer(e,false);});
  window.addEventListener("mousemove",movePointer);
  window.addEventListener("mouseup",pointerEnd);
  canvas.addEventListener("touchstart",function(e){e.preventDefault();if(e.touches.length===1)startPointer(e.touches[0],false);},{passive:false});
  window.addEventListener("touchmove",function(e){if(e.touches.length===1){e.preventDefault();movePointer(e.touches[0]);}},{passive:false});
  window.addEventListener("touchend",pointerEnd);
  window.addEventListener("touchcancel",pointerEnd);
}
canvas.addEventListener("wheel",e=>{e.preventDefault();const old=zoom,mx=e.clientX-canvas.getBoundingClientRect().left,my=e.clientY-canvas.getBoundingClientRect().top;zoom=Math.max(.25,Math.min(3,zoom*(e.deltaY<0?1.12:.89)));offset.x=mx-(mx-offset.x)*zoom/old;offset.y=my-(my-offset.y)*zoom/old;$("zoom").textContent=Math.round(zoom*100)+"%";draw();},{passive:false});
window.addEventListener("keydown",e=>{if(e.code==="Space"&&!/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)){space=true;e.preventDefault();}});window.addEventListener("keyup",e=>{if(e.code==="Space")space=false;});
document.querySelectorAll(".tool").forEach(b=>b.onclick=()=>{tool=b.dataset.tool;syncTools();});
function setZoom(v){zoom=Math.max(.25,Math.min(3,v));$("zoom").textContent=Math.round(zoom*100)+"%";draw();}
$("plus").onclick=()=>setZoom(zoom*1.15);$("minus").onclick=()=>setZoom(zoom/1.15);
$("undo").onclick=()=>{const p=history.undo(state());if(p)restore(p);};$("redo").onclick=()=>{const p=history.redo(state());if(p)restore(p);};
function download(blob,name,mime){if(window.AndroidBridge){const reader=new FileReader();reader.onload=()=>AndroidBridge.saveFile(name,reader.result.split(",")[1],mime||"application/octet-stream");reader.readAsDataURL(blob);return;}const u=URL.createObjectURL(blob),a=document.createElement("a");a.href=u;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(u),1500);}
$("save").onclick=()=>{metadataFromUI();if(sweDoc)level.swe={imported:true,sourceName:level.swe.sourceName,document:sweDoc,templates:sweTemplates};validateLevel(level);download(new Blob([JSON.stringify(level,null,2)],{type:"application/json"}),(level.metadata.name||"level").replace(/[\\/:*?"<>|]/g,"_")+".smmweproj","application/json");$("status").textContent="工程已保存";};
$("exportSwe").onclick=()=>{if(!sweDoc)return;metadataFromUI();const blob=encodeSwe(sweDoc);download(blob,(level.metadata.name||"level").replace(/[\\/:*?"<>|]/g,"_")+".swe","application/octet-stream");$("status").textContent="SWE 文件已导出";};
$("new").onclick=()=>{if(!confirm("新建工程？未保存的修改将丢失。"))return;level=newLevel();sweDoc=null;sweTemplates=null;history.checkpoint(state());uiFromLevel();syncSweUI();draw();$("status").textContent="新建工程";};
$("open").onclick=()=>$("file").click();
$("file").onchange=function(e){
  var f=e.target.files[0];if(!f)return;
  var isSwe=/\.swe$/i.test(f.name);
  readFile(f,isSwe).then(function(result){
    if(isSwe){
      var parsed=inspectSwe(result);sweDoc=parsed.data;sweTemplates=clone(sweDoc.S0);level=newLevel();
      level.metadata.name=f.name.replace(/\.swe$/i,"");
      var meta=(sweDoc.S0.S1&&sweDoc.S0.S1[0])||{};
      level.metadata.author=meta.user||"";level.metadata.description=meta.desc||"";level.grid.tileSize=16;
      var groups=["S2","S3","S4","S5","S8"],records=[];
      groups.forEach(function(k){(sweDoc.S0[k]||[]).forEach(function(r){if(Number.isFinite(r.xx)&&Number.isFinite(r.yy))records.push(r);});});
      var maxX=0,maxY=0;records.forEach(function(r){maxX=Math.max(maxX,r.xx);maxY=Math.max(maxY,r.yy);});
      level.grid.width=Math.max(80,Math.min(500,Math.ceil(maxX/16)+4));
      level.grid.height=Math.max(15,Math.min(100,Math.ceil(maxY/16)+4));
      level.swe={imported:true,sourceName:f.name,document:sweDoc,templates:sweTemplates};
      sweLayer="S2";sweId="49";history.checkpoint(state());uiFromLevel();syncSweUI();draw();
      $("status").textContent="SWE 已载入，可编辑";return;
    }
    var v=validateLevel(JSON.parse(result));level=v;
    sweDoc=v.swe&&v.swe.imported?v.swe.document:null;
    sweTemplates=sweDoc?(v.swe.templates||clone(sweDoc.S0)):null;
    sweLayer="S2";history.checkpoint(state());uiFromLevel();syncSweUI();draw();
    $("status").textContent="工程已打开";
  }).catch(function(err){alert("打开失败："+err.message);}).then(function(){e.target.value="";});
};
$("resize").onclick=()=>{const width=Math.max(1,Math.min(500,Number($("width").value)||80)),height=Math.max(1,Math.min(100,Number($("height").value)||15)),tileSize=Math.max(8,Math.min(64,Number($("tileSize").value)||32));checkpoint();level.grid={width,height,tileSize:sweDoc?16:tileSize};if(!sweDoc)level.tiles=level.tiles.filter(t=>t.x<width&&t.y<height);draw();};
$("sweLayer").onchange=()=>{sweLayer=$("sweLayer").value;syncSweUI();draw();};
$("sweId").onchange=()=>{sweId=$("sweId").value;};
["name","author","description","tags"].forEach(id=>$(id).addEventListener("change",metadataFromUI));
palette();uiFromLevel();syncSweUI();history.checkpoint(state());resizeCanvas();window.addEventListener("resize",resizeCanvas);
