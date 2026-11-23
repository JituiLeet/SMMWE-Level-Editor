package com.jituileet.smmweeditor;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity implements EditorView.Actions {
    private static final int OPEN_FILE=10,SAVE_FILE=11;
    private EditorView editor; private JSONObject sweRoot; private String fileName="Untitled Level",author="",description="";
    private String layer="S2",selectedId="49"; private File pending;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);editor=new EditorView(this);editor.setActions(this);setContentView(editor);editor.setModel(new JSONArray(),null,layer,selectedId);}
    @Override public void menu(){final String[] items={"新建关卡","打开 .swe / 工程","保存工程","导出 .swe","关卡信息"};new AlertDialog.Builder(this).setTitle("关卡菜单").setItems(items,(d,w)->{if(w==0)newLevel();if(w==1)open();if(w==2)saveProject();if(w==3)exportSwe();if(w==4)properties();}).show();}
    @Override public void open(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,OPEN_FILE);}
    @Override public void save(){saveProject();}
    @Override public void export(){exportSwe();}
    @Override public void newLevel(){sweRoot=null;fileName="Untitled Level";author="";description="";editor.setModel(new JSONArray(),null,"S2","49");status("新关卡已建立");}
    @Override public void properties(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(36,8,36,0);EditText n=new EditText(this),a=new EditText(this),desc=new EditText(this);n.setSingleLine(true);a.setSingleLine(true);desc.setSingleLine(true);n.setHint("关卡名称");a.setHint("作者");desc.setHint("简介");n.setText(fileName);a.setText(author);desc.setText(description);box.addView(n);box.addView(a);box.addView(desc);new AlertDialog.Builder(this).setTitle("关卡信息").setView(box).setNegativeButton("取消",null).setPositiveButton("保存",(d,w)->{fileName=n.getText().toString();author=a.getText().toString();description=desc.getText().toString();status("关卡信息已更新");}).show();}
    @Override public void status(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void saveProject(){try{JSONObject p=new JSONObject();p.put("format","smmwe-level-editor-native");p.put("version",1);p.put("name",fileName);p.put("author",author);p.put("description",description);p.put("swe",sweRoot);p.put("tiles",editor.getTiles());launchSave(safeName()+".smmweproj","application/json",p.toString(2).getBytes(StandardCharsets.UTF_8));}catch(Exception e){status("保存失败："+e.getMessage());}}
    private void exportSwe(){if(sweRoot==null){status("请先导入 .swe 文件，当前空白工程不能生成真实 SWE");return;}try{JSONObject s0=sweRoot.getJSONObject("S0");JSONArray meta=s0.optJSONArray("S1");if(meta!=null&&meta.length()>0){JSONObject m=meta.getJSONObject(0);m.put("user",author);m.put("desc",description);}launchSave(safeName()+".swe","application/octet-stream",SweCodec.encode(sweRoot));}catch(Exception e){status("导出失败："+e.getMessage());}}
    private String safeName(){String n=fileName.trim();if(n.isEmpty())n="level";return n.replaceAll("[\\\\/:*?\"<>|]","_");}
    private void launchSave(String name,String mime,byte[] bytes){try{pending=new File(getCacheDir(),"pending_export");try(FileOutputStream o=new FileOutputStream(pending)){o.write(bytes);}Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,SAVE_FILE);}catch(Exception e){status("无法导出："+e.getMessage());}}
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;try{if(req==OPEN_FILE){byte[] bytes=read(data.getData());String n=data.getData().getLastPathSegment()==null?"level":data.getData().getLastPathSegment();if(n.toLowerCase(Locale.ROOT).endsWith(".swe"))loadSwe(bytes,n);else loadProject(new String(bytes,StandardCharsets.UTF_8));}else if(req==SAVE_FILE&&pending!=null){try(InputStream in=new FileInputStream(pending);OutputStream out=getContentResolver().openOutputStream(data.getData())){byte[] b=new byte[8192];int k;while((k=in.read(b))!=-1)out.write(b,0,k);}pending.delete();status("文件已保存");}}catch(Exception e){status("文件操作失败："+e.getMessage());}}
    private byte[] read(Uri u)throws Exception{try(InputStream in=getContentResolver().openInputStream(u);ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] x=new byte[8192];int n;while((n=in.read(x))!=-1)b.write(x,0,n);return b.toByteArray();}}
    private void loadSwe(byte[] bytes,String name)throws Exception{sweRoot=SweCodec.decode(bytes);fileName=name.replaceAll("(?i)\\.swe$","");JSONObject s0=sweRoot.getJSONObject("S0");JSONArray m=s0.optJSONArray("S1");if(m!=null&&m.length()>0){author=m.getJSONObject(0).optString("user","");description=m.getJSONObject(0).optString("desc","");}editor.setModel(null,sweRoot,layer,selectedId);status("SWE 校验通过，已载入关卡");}
    private void loadProject(String raw)throws Exception{JSONObject p=new JSONObject(raw);if(!"smmwe-level-editor-native".equals(p.optString("format")))throw new Exception("不支持的工程文件");sweRoot=p.optJSONObject("swe");fileName=p.optString("name","Untitled Level");author=p.optString("author","");description=p.optString("description","");editor.setModel(p.optJSONArray("tiles"),sweRoot,layer,selectedId);status("工程已打开");}
}
