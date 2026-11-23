export const PROJECT_FORMAT = "smmwe-level-editor";
export const PROJECT_VERSION = 1;
export const TILE_TYPES = [
  {id:"ground",name:"地面",category:"地形",color:"#a85d2d",glyph:"▦"},
  {id:"brick",name:"砖块",category:"地形",color:"#bd7546",glyph:"B"},
  {id:"question",name:"问号砖",category:"地形",color:"#e8b83e",glyph:"?"},
  {id:"hardblock",name:"硬砖",category:"地形",color:"#8b6547",glyph:"▧"},
  {id:"stone",name:"石块",category:"地形",color:"#778294",glyph:"■"},
  {id:"pipe",name:"绿色水管",category:"地形",color:"#48ad50",glyph:"Π"},
  {id:"cloud",name:"云朵平台",category:"地形",color:"#dceeff",glyph:"☁"},
  {id:"hill",name:"草地山丘",category:"地形",color:"#55a995",glyph:"▲"},
  {id:"water",name:"水面",category:"地形",color:"#36a9dc",glyph:"≈"},
  {id:"lava",name:"岩浆",category:"地形",color:"#ef663b",glyph:"♨"},
  {id:"slope",name:"斜坡",category:"地形",color:"#9a754f",glyph:"◢"},
  {id:"platform",name:"移动平台",category:"机关",color:"#4f9eb5",glyph:"▰"},
  {id:"door",name:"门",category:"机关",color:"#b77942",glyph:"▣"},
  {id:"key",name:"钥匙",category:"道具",color:"#f0c33c",glyph:"⚿"},
  {id:"coin",name:"金币",category:"道具",color:"#f2bd35",glyph:"●"},
  {id:"star",name:"无敌星",category:"道具",color:"#f5d84b",glyph:"★"},
  {id:"mushroom",name:"蘑菇",category:"道具",color:"#e85d48",glyph:"🍄"},
  {id:"flower",name:"火焰花",category:"道具",color:"#ed8b39",glyph:"✿"},
  {id:"leaf",name:"叶子",category:"道具",color:"#55a84b",glyph:"♣"},
  {id:"spring",name:"弹簧",category:"机关",color:"#75b9cf",glyph:"↟"},
  {id:"checkpoint",name:"检查点",category:"机关",color:"#df7658",glyph:"⚑"},
  {id:"goal",name:"终点旗",category:"机关",color:"#45a879",glyph:"⚑"},
  {id:"goomba",name:"栗宝宝",category:"敌人",color:"#a66a43",glyph:"♟"},
  {id:"koopa",name:"慢慢龟",category:"敌人",color:"#63a84d",glyph:"♜"},
  {id:"piranha",name:"食人花",category:"敌人",color:"#d65350",glyph:"✿"},
  {id:"spiny",name:"刺刺龟",category:"敌人",color:"#d36d42",glyph:"✹"},
  {id:"thwomp",name:"刺客砖",category:"敌人",color:"#78818d",glyph:"▣"},
  {id:"spike",name:"尖刺",category:"敌人",color:"#aeb9c8",glyph:"▲"},
  {id:"decoration",name:"装饰物",category:"装饰",color:"#9b75c7",glyph:"✦"},
  {id:"bush",name:"灌木",category:"装饰",color:"#4eae64",glyph:"♣"},
  {id:"fence",name:"栅栏",category:"装饰",color:"#b88954",glyph:"╫"}
];
export function newLevel(){
  return {format:PROJECT_FORMAT,version:PROJECT_VERSION,metadata:{name:"Untitled Level",author:"",description:"",tags:[],gamestyle:"",gametheme:"",timer:300,startY:0,goalX:0,goalY:0},grid:{width:80,height:15,tileSize:32},tiles:[],swe:{imported:false,sourceName:null,document:null}};
}
export function validateLevel(v){
  if(!v||v.format!==PROJECT_FORMAT||v.version!==PROJECT_VERSION) throw new Error("不支持的工程文件版本");
  if(!Array.isArray(v.tiles)||!v.grid||!v.metadata) throw new Error("工程文件结构不完整");
  if(v.tiles.some(t=>!Number.isInteger(t.x)||!Number.isInteger(t.y)||!TILE_TYPES.some(k=>k.id===t.type))) throw new Error("工程包含无效图块");
  if(!Array.isArray(v.metadata.tags))v.metadata.tags=[];
  return v;
}