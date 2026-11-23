package com.jituileet.smmweeditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private static final int OPEN_FILE=10, SAVE_FILE=11;
    private EditorView editor;
    private JSONObject sweRoot;
    private String fileName="Untitled Level";
    private EditText nameInput, authorInput, descInput;
    private Spinner layerSpinner, idSpinner;
    private TextView status;
    private String layer="S2", selectedId="49";
    private JSONArray templates;
    private final String[] layers={"S2","S3","S4","S5","S8"};
    private final String[] layerNames={"地形 S2","装饰 S3","对象 S4","移动平台 S5","门 S8"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        editor.setModel(new JSONArray(), null, "S2", "49");
    }

    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xff202938);return t;}
    private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xfff3f5f8);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(8),dp(4),dp(8),dp(4));
        TextView title=text("SMMWE 关卡编辑器",19);title.setTypeface(null,1);top.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        top.addView(button("新建",()->newLevel()));top.addView(button("打开",()->openFile()));top.addView(button("保存",()->saveProject()));top.addView(button("导出 SWE",()->exportSwe()));root.addView(top);
        LinearLayout props=new LinearLayout(this);props.setPadding(dp(8),0,dp(8),dp(4));props.setOrientation(LinearLayout.VERTICAL);
        nameInput=new EditText(this);nameInput.setSingleLine(true);nameInput.setText(fileName);nameInput.setHint("关卡名称");props.addView(nameInput);
        LinearLayout meta=new LinearLayout(this);authorInput=new EditText(this);authorInput.setSingleLine(true);authorInput.setHint("作者");descInput=new EditText(this);descInput.setSingleLine(true);descInput.setHint("简介");meta.addView(authorInput,new LinearLayout.LayoutParams(0,dp(48),1));meta.addView(descInput,new LinearLayout.LayoutParams(0,dp(48),2));props.addView(meta);root.addView(props);
        LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);tools.setPadding(dp(8),0,dp(8),0);
        layerSpinner=new Spinner(this);ArrayAdapter<String> la=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,layerNames);layerSpinner.setAdapter(la);tools.addView(layerSpinner,new LinearLayout.LayoutParams(0,dp(48),1));
        idSpinner=new Spinner(this);tools.addView(idSpinner,new LinearLayout.LayoutParams(0,dp(48),1));
        tools.addView(button("擦除",()->editor.setErase(true)));tools.addView(button("绘制",()->editor.setErase(false)));root.addView(tools);
        HorizontalScrollView paletteScroll=new HorizontalScrollView(this);LinearLayout palette=new LinearLayout(this);palette.setPadding(dp(6),0,dp(6),dp(2));String[] paletteNames={"地面","砖块","问号砖","实心块","终点","敌人"};for(int pi=0;pi<paletteNames.length;pi++){final int type=pi;palette.addView(button(paletteNames[pi],()->editor.setTileType(type)));}paletteScroll.addView(palette);root.addView(paletteScroll);
        layerSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){layer=layers[pos];refreshIds();if(editor!=null)editor.setLayer(layer,selectedId);}});
        idSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){if(p.getSelectedItem()!=null){selectedId=p.getSelectedItem().toString();if(editor!=null)editor.setLayer(layer,selectedId);}}});
        editor=new EditorView(this);root.addView(editor,new LinearLayout.LayoutParams(-1,0,1));
        status=text("新建工程 · 触摸画布放置图块，双指/拖动画布可调整视图",12);status.setPadding(dp(10),dp(5),dp(10),dp(5));root.addView(status);
        setContentView(root);
    }

    private void newLevel(){sweRoot=null;templates=null;fileName="Untitled Level";nameInput.setText(fileName);authorInput.setText("");descInput.setText("");editor.setModel(new JSONArray(),null,"S2","49");status.setText("已新建原生关卡工程");}
    private void openFile(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,OPEN_FILE);}
    private void saveProject(){try{JSONObject p=new JSONObject();p.put("format","smmwe-level-editor-native");p.put("version",1);p.put("name",nameInput.getText().toString());p.put("author",authorInput.getText().toString());p.put("description",descInput.getText().toString());p.put("swe",sweRoot);p.put("tiles",editor.getTiles());byte[] data=p.toString(2).getBytes(StandardCharsets.UTF_8);launchSave((safeName()+".smmweproj"),"application/json",data);}catch(Exception e){status.setText("保存失败："+e.getMessage());}}
    private void exportSwe(){if(sweRoot==null){status.setText("当前是新建工程：请先导入 .swe 文件，才能导出保留原始结构的 SWE");return;}try{JSONObject s0=sweRoot.getJSONObject("S0");JSONObject meta=s0.optJSONArray("S1")!=null&&s0.getJSONArray("S1").length()>0?s0.getJSONArray("S1").getJSONObject(0):null;if(meta!=null){meta.put("user",authorInput.getText().toString());meta.put("desc",descInput.getText().toString());}byte[] data=SweCodec.encode(sweRoot);launchSave(safeName()+".swe","application/octet-stream",data);}catch(Exception e){status.setText("导出失败："+e.getMessage());}}
    private String safeName(){String n=nameInput.getText().toString().trim();if(n.isEmpty())n="level";return n.replaceAll("[\\\\/:*?\"<>|]","_");}
    private void launchSave(String name,String mime,byte[] data){try{File f=new File(getCacheDir(),"pending_export");try(FileOutputStream o=new FileOutputStream(f)){o.write(data);}getPreferences(0).edit().putString("pending_file",f.getAbsolutePath()).apply();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,SAVE_FILE);}catch(Exception e){status.setText("无法创建导出文件："+e.getMessage());}}
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;try{if(req==OPEN_FILE){Uri u=data.getData();byte[] bytes=read(u);String n=u.getLastPathSegment()==null?"level":u.getLastPathSegment();if(n.toLowerCase(Locale.ROOT).endsWith(".swe"))loadSwe(bytes,n);else loadProject(new String(bytes,StandardCharsets.UTF_8));}else if(req==SAVE_FILE){String p=getPreferences(0).getString("pending_file",null);if(p==null)return;try(InputStream in=new FileInputStream(p);OutputStream out=getContentResolver().openOutputStream(data.getData())){byte[] buf=new byte[8192];int k;while((k=in.read(buf))!=-1)out.write(buf,0,k);}new File(p).delete();status.setText("文件已保存");}}catch(Exception e){status.setText("文件操作失败："+e.getMessage());}}
    private byte[] read(Uri u)throws Exception{try(InputStream in=getContentResolver().openInputStream(u);ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)b.write(buf,0,n);return b.toByteArray();}}
    private void loadSwe(byte[] bytes,String n)throws Exception{sweRoot=SweCodec.decode(bytes);fileName=n.replaceAll("(?i)\\.swe$","");nameInput.setText(fileName);JSONObject s0=sweRoot.getJSONObject("S0");JSONArray m=s0.optJSONArray("S1");if(m!=null&&m.length()>0){JSONObject x=m.getJSONObject(0);authorInput.setText(x.optString("user",""));descInput.setText(x.optString("desc",""));}templates=s0.optJSONArray("S2");layer="S2";layerSpinner.setSelection(0);refreshIds();editor.setModel(null,sweRoot,layer,selectedId);status.setText("SWE 容器校验通过；原始数据组已载入，编辑与导出使用原生代码");}
    private void loadProject(String raw)throws Exception{JSONObject p=new JSONObject(raw);if(!"smmwe-level-editor-native".equals(p.optString("format")))throw new Exception("不支持的工程文件");sweRoot=p.optJSONObject("swe");fileName=p.optString("name","Untitled Level");nameInput.setText(fileName);authorInput.setText(p.optString("author",""));descInput.setText(p.optString("description",""));editor.setModel(p.optJSONArray("tiles"),sweRoot,layer,selectedId);status.setText("原生工程已打开");}
    private void refreshIds(){ArrayList<String> ids=new ArrayList<>();if(sweRoot!=null){JSONArray a=sweRoot.optJSONObject("S0").optJSONArray(layer);if(a!=null)for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);String id=layer.equals("S2")?String.valueOf(r.optInt("i")):r.optString("ID","");if(!id.isEmpty()&&!ids.contains(id))ids.add(id);}}if(ids.isEmpty())ids.add(layer.equals("S2")?"49":"");Collections.sort(ids);if(!ids.contains(selectedId))selectedId=ids.get(0);ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,ids);idSpinner.setAdapter(ad);if(ids.contains(selectedId))idSpinner.setSelection(ids.indexOf(selectedId));}
}
