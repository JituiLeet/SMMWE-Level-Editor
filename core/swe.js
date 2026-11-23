// Conservative SWE container inspector. This module does not infer unknown object IDs.
const KEY="2559F35097-2021";
const enc=new TextEncoder(),dec=new TextDecoder();
const toHex=b=>Array.from(b,x=>x.toString(16).padStart(2,"0")).join("");
export async function hmacSha1(value){
  const k=await crypto.subtle.importKey("raw",enc.encode(KEY),{name:"HMAC",hash:"SHA-1"},false,["sign"]);
  return toHex(new Uint8Array(await crypto.subtle.sign("HMAC",k,enc.encode(value))));
}
export async function inspectSwe(buffer){
  const raw=dec.decode(buffer).trim();
  const m=raw.match(/^([A-Za-z0-9+/=\r\n]+)([0-9a-f]{40})$/i);
  if(!m)throw new Error("文件容器不符合当前识别的 Base64 + 40 位摘要布局");
  const payload=m[1].replace(/\s/g,""),actual=m[2].toLowerCase(),expected=await hmacSha1(payload);
  if(actual!==expected)throw new Error("HMAC-SHA1 校验失败，文件未载入");
  let data;
  try{data=JSON.parse(dec.decode(Uint8Array.from(atob(payload),c=>c.charCodeAt(0))));}
  catch(e){throw new Error("摘要通过，但 Base64/JSON 解码失败："+e.message);}
  if(!data||typeof data!=="object")throw new Error("SWE JSON 根结构无效");
  return {data,payload,digest:actual,verified:true};
}
export async function encodeSwe(json){
  const payload=btoa(unescape(encodeURIComponent(JSON.stringify(json))));
  return new Blob([payload,await hmacSha1(payload)],{type:"application/octet-stream"});
}
