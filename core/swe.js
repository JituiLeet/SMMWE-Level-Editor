// SMMWE SWE container reader/writer.
// Pure-JS HMAC-SHA1 keeps this working in Android 5 WebView.
const KEY="2559F35097-2021";
function utf8Binary(text){return unescape(encodeURIComponent(text));}
function binaryUtf8(binary){return decodeURIComponent(escape(binary));}
function sha1(bytes){
  const bitLen=bytes.length*8, data=bytes.slice();data.push(0x80);
  while((data.length%64)!==56)data.push(0);
  const hi=Math.floor(bitLen/0x100000000),lo=bitLen>>>0;
  for(let i=3;i>=0;i--)data.push((hi>>>(i*8))&255);
  for(let i=3;i>=0;i--)data.push((lo>>>(i*8))&255);
  let h0=0x67452301,h1=0xefcdab89,h2=0x98badcfe,h3=0x10325476,h4=0xc3d2e1f0;
  const rol=(x,n)=>(x<<n)|(x>>>(32-n));
  for(let off=0;off<data.length;off+=64){
    const w=new Array(80);
    for(let i=0;i<16;i++){const j=off+i*4;w[i]=((data[j]<<24)|(data[j+1]<<16)|(data[j+2]<<8)|data[j+3])>>>0;}
    for(let i=16;i<80;i++)w[i]=rol(w[i-3]^w[i-8]^w[i-14]^w[i-16],1)>>>0;
    let a=h0,b=h1,c=h2,d=h3,e=h4;
    for(let i=0;i<80;i++){
      let f,k;
      if(i<20){f=(b&c)|((~b)&d);k=0x5a827999;}
      else if(i<40){f=b^c^d;k=0x6ed9eba1;}
      else if(i<60){f=(b&c)|(b&d)|(c&d);k=0x8f1bbcdc;}
      else{f=b^c^d;k=0xca62c1d6;}
      const t=(rol(a,5)+f+e+k+w[i])>>>0;e=d;d=c;c=rol(b,30)>>>0;b=a;a=t;
    }
    h0=(h0+a)>>>0;h1=(h1+b)>>>0;h2=(h2+c)>>>0;h3=(h3+d)>>>0;h4=(h4+e)>>>0;
  }
  return [h0,h1,h2,h3,h4].map(x=>x.toString(16).padStart(8,"0")).join("");
}
export function hmacSha1(text){
  let key=Array.from(utf8Binary(KEY),c=>c.charCodeAt(0));
  if(key.length>64)key=Array.from(sha1(key).match(/../g),x=>parseInt(x,16));
  while(key.length<64)key.push(0);
  const inner=key.map(x=>x^0x36),outer=key.map(x=>x^0x5c);
  const msg=Array.from(utf8Binary(text),c=>c.charCodeAt(0));
  const ih=sha1(inner.concat(msg));
  return sha1(outer.concat(ih.match(/../g).map(x=>parseInt(x,16))));
}
function decodeBase64(s){return binaryUtf8(atob(s));}
function encodeBase64(s){return btoa(utf8Binary(s));}
export async function inspectSwe(buffer){
  const raw=Array.from(new Uint8Array(buffer),b=>String.fromCharCode(b)).join("").trim();
  const m=raw.match(/^([A-Za-z0-9+/=\r\n]+)([0-9a-f]{40})$/i);
  if(!m)throw new Error("无法识别此 .swe 文件的容器格式");
  const payload=m[1].replace(/\s/g,""),actual=m[2].toLowerCase(),expected=hmacSha1(payload);
  if(actual!==expected)throw new Error("HMAC-SHA1 校验失败，文件未载入");
  let data;
  try{data=JSON.parse(decodeBase64(payload));}catch(e){throw new Error("校验通过，但 JSON 解码失败："+e.message);}
  if(!data||!data.S0||!Array.isArray(data.S0.S2))throw new Error("缺少已确认的 S0.S2 地形数据组");
  return {data,payload,digest:actual,verified:true,terrainCount:data.S0.S2.length};
}
export function encodeSwe(json){
  const payload=encodeBase64(JSON.stringify(json));
  return new Blob([payload,hmacSha1(payload)],{type:"application/octet-stream"});
}
export function cloneSweRecord(record){return JSON.parse(JSON.stringify(record));}
