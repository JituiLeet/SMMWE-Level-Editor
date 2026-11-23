package com.jituileet.smmweeditor;
import org.json.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
public final class SweCodec {
 private static final String KEY="2559F35097-2021";
 private static String hmac(String value)throws Exception{Mac mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8),"HmacSHA1"));byte[] d=mac.doFinal(value.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:d)s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return s.toString();}
 public static JSONObject decode(byte[] bytes)throws Exception{String raw=new String(bytes,StandardCharsets.US_ASCII).trim();Matcher m=Pattern.compile("^([A-Za-z0-9+/=\\r\\n]+)([0-9a-f]{40})$",Pattern.CASE_INSENSITIVE).matcher(raw);if(!m.matches())throw new Exception("无法识别 SWE 容器格式");String payload=m.group(1).replaceAll("\\s",""),digest=m.group(2).toLowerCase(java.util.Locale.ROOT);if(!hmac(payload).equals(digest))throw new Exception("HMAC-SHA1 校验失败");JSONObject root=new JSONObject(new String(Base64.decode(payload,Base64.DEFAULT),StandardCharsets.UTF_8));if(root.optJSONObject("S0")==null||root.getJSONObject("S0").optJSONArray("S2")==null)throw new Exception("缺少 S0.S2 数据组");return root;}
 public static byte[] encode(JSONObject root)throws Exception{String payload=Base64.encodeToString(root.toString().getBytes(StandardCharsets.UTF_8),Base64.NO_WRAP);return (payload+hmac(payload)).getBytes(StandardCharsets.US_ASCII);}
}
