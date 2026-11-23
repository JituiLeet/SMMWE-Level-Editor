export const PROJECT_FORMAT = "smmwe-level-editor";
export const PROJECT_VERSION = 1;
export const TILE_TYPES = [
  {id:"ground",name:"地面",color:"#8b6547",glyph:""},
  {id:"brick",name:"砖块",color:"#bd7546",glyph:"B"},
  {id:"question",name:"问号砖",color:"#e8b83e",glyph:"?"},
  {id:"solid",name:"实心方块",color:"#778294",glyph:"■"},
  {id:"goal",name:"终点（占位）",color:"#45a879",glyph:"G"},
  {id:"enemy",name:"敌人（占位）",color:"#cf5e68",glyph:"E"}
];
export function newLevel(){
  return {format:PROJECT_FORMAT,version:PROJECT_VERSION,metadata:{name:"Untitled Level",author:"",description:"",tags:[]},grid:{width:80,height:15,tileSize:32},tiles:[],swe:{imported:false,sourceName:null}};
}
export function validateLevel(v){
  if(!v||v.format!==PROJECT_FORMAT||v.version!==PROJECT_VERSION) throw new Error("不支持的工程文件版本");
  if(!Array.isArray(v.tiles)||!v.grid||!v.metadata) throw new Error("工程文件结构不完整");
  if(v.tiles.some(t=>!Number.isInteger(t.x)||!Number.isInteger(t.y)||!TILE_TYPES.some(k=>k.id===t.type))) throw new Error("工程包含无效图块");
  return v;
}
