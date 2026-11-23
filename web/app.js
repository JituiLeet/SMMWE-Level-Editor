import {newLevel,validateLevel,TILE_TYPES} from "../core/model.js";
import {History} from "../core/history.js";
import {inspectSwe} from "../core/swe.js";
const $=id=>document.getElementById(id),canvas=$("board"),ctx=canvas.getContext("2d"),view=$("viewport");
let level=newLevel(),tool="paint",selected="ground",zoom=1,offset={x:28,y:24},pointerDown=false,space=false,last="",sweSource=null;
const history=new History();history.checkpoint(level);
const clone=v=>JSON.parse(JSON.stringify(v));
function checkpoint(){history.checkpoint(level)}
function metadataFromUI(){level.metadata={name:$("name").value,author:$("author").value,description:$("description").value,tags:$("tags").value.split(",").map(x=>x.trim()).filter(Boolean)}}
function uiFromLevel(){ $("name").value=level.metadata.name||"";$("author").value=level.metadata.author||"";$("description").value=level.metadata.description||"";$("tags").value=(level.metadata.tags||[]).join(",");$("width").value=level.grid.width;$("height").value=level.grid.height;$("tileSize").value=level.grid.tileSize; }
function palette(){ $("palette").replaceChildren();for(const t of TILE_TYPES){const b=document.createElement("button");b.className="tile"+(selected===t.id?" selected":"");const sw=document.createElement("span");sw.className="swatch";sw.style.background=t.color;sw.textContent=t.glyph;b.append(sw,document.createTextNode(t.name));b.onclick=()=>{selected=t.id;tool="paint";syncTools();palette()};$("palette").append(b)}}
function syncTools(){document.querySelectorAll(".tool").forEach(b=>b.classList.toggle("selected",b.dataset.tool===tool))}
function resizeCanvas(){const d=window.devicePixelRatio||1,w=view.clientWidth,h=view.clientHeight;canvas.width=Math.round(w*d);canvas.height=Math.round(h*d);canvas.style.width=w+"px";canvas.style.height=h+"px";ctx.setTransform(d,0,0,d,0,0);draw()}
function draw(){const w=view.clientWidth,h=view.clientHeight,s=level.grid.tileSize*zoom;ctx.clearRect(0,0,w,h);ctx.save();ctx.translate(offset.x,offset.y);ctx.strokeStyle="#30394b";ctx.lineWidth=1;const sx=Math.max(0,Math.floor(-offset.x/s)),sy=Math.max(0,Math.floor(-offset.y/s));for(let x=sx;x<=level.grid.width;x++){ctx.beginPath();ctx.moveTo(x*s,0);ctx.lineTo(x*s,level.grid.height*s);ctx.stroke()}for(let y=sy;y<=level.grid.height;y++){ctx.beginPath();ctx.moveTo(0,y*s);ctx.lineTo(level.grid.width*s,y*s);ctx.stroke()}for(const p of level.tiles){const t=TILE_TYPES.find(v=>v.id===p.type);if(!t)continue;const x=p.x*s,y=p.y*s;ctx.fillStyle=t.color;ctx.fillRect(x+1,y+1,s-2,s-2);if(t.glyph){ctx.fillStyle="#fff";ctx.textAlign="center";ctx.textBaseline="middle";ctx.font="bold "+Math.max(9,s*.45)+"px sans-serif";ctx.fillText(t.glyph,x+s/2,y+s/2)}}ctx.restore()}
function coords(e){const r=canvas.getBoundingClientRect(),s=level.grid.tileSize*zoom;return{x:Math.floor((e.clientX-r.left-offset.x)/s),y:Math.floor((e.clientY-r.top-offset.y)/s)}}
function apply(e){const c=coords(e);if(c.x<0||c.y<0||c.x>=level.grid.width||c.y>=level.grid.height)return;const k=c.x+":"+c.y;if(k===last)return;last=k;level.tiles=level.tiles.filter(p=>p.x!==c.x||p.y!==c.y);if(tool==="paint")level.tiles.push({x:c.x,y:c.y,type:selected});draw()}
canvas.addEventListener("pointerdown",e=>{canvas.setPointerCapture(e.pointerId);pointerDown=true;last="";if(tool==="select"||space||e.button===1){canvas.dataset.pan="1";return}checkpoint();apply(e)});
canvas.addEventListener("pointermove",e=>{if(!pointerDown)return;if(canvas.dataset.pan){offset.x+=e.movementX;offset.y+=e.movementY;draw()}else apply(e)});
function pointerEnd(){pointerDown=false;last="";delete canvas.dataset.pan}
canvas.addEventListener("pointerup",pointerEnd);canvas.addEventListener("pointercancel",pointerEnd);
canvas.addEventListener("wheel",e=>{e.preventDefault();const old=zoom,mx=e.clientX-canvas.getBoundingClientRect().left,my=e.clientY-canvas.getBoundingClientRect().top;zoom=Math.max(.25,Math.min(3,zoom*(e.deltaY<0?1.12:.89)));offset.x=mx-(mx-offset.x)*zoom/old;offset.y=my-(my-offset.y)*zoom/old;$("zoom").textContent=Math.round(zoom*100)+"%";draw()},{passive:false});
window.addEventListener("keydown",e=>{if(e.code==="Space"&&!/INPUT|TEXTAREA/.test(document.activeElement.tagName)){space=true;e.preventDefault()}});window.addEventListener("keyup",e=>{if(e.code==="Space")space=false});
document.querySelectorAll(".tool").forEach(b=>b.onclick=()=>{tool=b.dataset.tool;syncTools()});
$("plus").onclick=()=>setZoom(zoom*1.15);$("minus").onclick=()=>setZoom(zoom/1.15);
function setZoom(v){zoom=Math.max(.25,Math.min(3,v));$("zoom").textContent=Math.round(zoom*100)+"%";draw()}
$("undo").onclick=()=>{const p=history.undo(level);if(p){level=p;uiFromLevel();draw()}};$("redo").onclick=()=>{const p=history.redo(level);if(p){level=p;uiFromLevel();draw()}};
function download(blob,name){if(window.AndroidBridge){const reader=new FileReader();reader.onload=()=>AndroidBridge.saveFile(name,reader.result.split(",")[1],"application/json");reader.readAsDataURL(blob);return}const u=URL.createObjectURL(blob),a=document.createElement("a");a.href=u;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(u),1500)}
$("save").onclick=()=>{metadataFromUI();validateLevel(level);download(new Blob([JSON.stringify(level,null,2)],{type:"application/json"}),(level.metadata.name||"level").replace(/[\\/:*?"<>|]/g,"_")+".smmweproj");$("status").textContent="工程已保存"};
$("new").onclick=()=>{if(!confirm("新建工程？未保存的修改将丢失。"))return;level=newLevel();sweSource=null;history.checkpoint(level);uiFromLevel();$("exportSwe").disabled=true;draw();$("status").textContent="新建工程"};
$("open").onclick=()=>$("file").click();
$("file").onchange=async e=>{const f=e.target.files[0];if(!f)return;try{if(f.name.toLowerCase().endsWith(".swe")){const result=await inspectSwe(await f.arrayBuffer());sweSource=result;$("sweInfo").textContent="容器摘要校验通过。已解析 JSON 根对象；在完整确认各图块 ID、坐标单位及元数据映射前，不自动改写原始数据。";$("status").textContent="SWE 已检查（只读）";$("exportSwe").disabled=true;return}const v=validateLevel(JSON.parse(await f.text()));level=v;sweSource=null;history.checkpoint(level);uiFromLevel();$("exportSwe").disabled=true;draw();$("status").textContent="工程已打开"}catch(err){alert("打开失败："+err.message)}e.target.value=""};
$("resize").onclick=()=>{const width=Math.max(1,Math.min(500,Number($("width").value)||80)),height=Math.max(1,Math.min(100,Number($("height").value)||15)),tileSize=Math.max(8,Math.min(64,Number($("tileSize").value)||32));checkpoint();level.grid={width,height,tileSize};level.tiles=level.tiles.filter(t=>t.x<width&&t.y<height);draw()};
["name","author","description","tags"].forEach(id=>$(id).addEventListener("change",metadataFromUI));
palette();uiFromLevel();resizeCanvas();window.addEventListener("resize",resizeCanvas);
