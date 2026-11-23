package com.jituileet.smmweeditor;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import org.json.*;
import java.io.*;

public class EditorView extends View {
    public interface Actions { void menu(); void open(); void save(); void export(); void newLevel(); void properties(); void status(String s); }
    private final Paint p=new Paint(); private final float density;
    private JSONArray tiles=new JSONArray(); private JSONObject swe;
    private String layer="S2", id="49";
    private boolean erase=false, dragging=false, moved=false;
    private int selectedType=2, category=0;
    private float scale=1, ox, oy, downX, downY, lastX, lastY, pinchStart;
    private Bitmap tileSheet, charSheet, bgSheet;
    private Actions actions;
    private final int[] accents={0xfff1d600,0xff58d83e,0xff17c6df,0xffd44bc4,0xffff9d36,0xfff3f3f3};
    private final String[] categories={"地形","装饰","敌人","道具","机关","其他"};
    private final String[] layers={"S2","S3","S4","S5","S8"};
    private final String[] layerNames={"地形","装饰","对象","平台","门"};
    private float topH,leftW,rightW,bottomH,gridCell;
    public EditorView(Context c){super(c);density=getResources().getDisplayMetrics().density;setLayerType(View.LAYER_TYPE_SOFTWARE,null);setFocusable(true);loadArt();setBackgroundColor(0xff8192ff);}
    private float d(float v){return v*density;}
    public void setActions(Actions a){actions=a;}
    private void loadArt(){try{tileSheet=BitmapFactory.decodeStream(getContext().getAssets().open("art/tilemap.png"));}catch(Exception ignored){}try{charSheet=BitmapFactory.decodeStream(getContext().getAssets().open("art/tilemap-characters.png"));}catch(Exception ignored){}try{bgSheet=BitmapFactory.decodeStream(getContext().getAssets().open("art/tilemap-backgrounds.png"));}catch(Exception ignored){}}
    public void setErase(boolean v){erase=v;invalidate();}
    public void setTileType(int type){selectedType=Math.max(0,Math.min(5,type));erase=false;invalidate();}
    public void setLayer(String l,String i){layer=l;id=i;invalidate();}
    public void setModel(JSONArray t,JSONObject s,String l,String i){tiles=t==null?new JSONArray():t;swe=s;layer=l;id=i;ox=0;oy=0;scale=1;invalidate();}
    public JSONArray getTiles(){return tiles;}
    private JSONArray records(){if(swe==null)return null;JSONObject s=swe.optJSONObject("S0");return s==null?null:s.optJSONArray(layer);}
    private void fill(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
    private void border(Canvas c,int color,float l,float t,float r,float b,float w){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.FILL);}
    private void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setColor(color);p.setTextSize(size);p.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);}
    private void panel(Canvas c,float l,float t,float r,float b){fill(c,0x553b4a9a,l+ d(3),t+d(4),r+d(3),b+d(4));fill(c,0xffc4c9ff,l,t,r,b);fill(c,0xffaeb7f5,l+d(3),t+d(3),r-d(3),b-d(3));border(c,0xfff7f7ff,l,t,r,b,d(2));}
    private void button(Canvas c,float l,float t,float r,float b,int stripe,String label,String glyph,boolean active){fill(c,0xff5967a6,l+d(2),t+d(4),r+d(2),b+d(4));fill(c,0xfffdfdfd,l,t,r,b);fill(c,stripe,l,t,r,t+d(5));if(active)border(c,0xff263eaa,l,t,r,b,d(3));if(glyph!=null&&!glyph.isEmpty())txt(c,glyph,(l+r)/2-d(glyph.length()*5), (t+b)/2+d(7),d(23),0xff30374d,true);if(label!=null&&!label.isEmpty())txt(c,label,l+d(7),b-d(5),d(10),0xff33384b,true);}
    private void sprite(Canvas c,Bitmap sheet,int index,float l,float t,float size){if(sheet==null)return;int cols=Math.max(1,(sheet.getWidth()+1)/19);int sx=(index%cols)*19,sy=(index/cols)*19;if(sx+18>sheet.getWidth()||sy+18>sheet.getHeight())return;p.setFilterBitmap(false);p.setAlpha(255);c.drawBitmap(sheet,new Rect(sx,sy,sx+18,sy+18),new RectF(l,t,l+size,t+size),p);}
    private void drawGrid(Canvas c,float l,float t,float r,float b){float cell=gridCell;fill(c,0xff788bfa,l,t,r,b);c.save();c.clipRect(l,t,r,b);c.translate(l+ox,t+oy);c.scale(scale,scale);
        // Soft pixel-art sky details.
        if(bgSheet!=null){sprite(c,bgSheet,0,l+ d(80),t+d(55),0); }
        p.setColor(0xff6578d9);p.setStrokeWidth(d(1));for(float x=0;x<((r-l)+cell*3)/cell;x++)c.drawLine(x*cell,0,x*cell,(b-t+cell*3)/scale,p);for(float y=0;y<((b-t)+cell*3)/cell;y++)c.drawLine(0,y*cell,(r-l+cell*3)/scale,y*cell,p);
        // Sample decorative clouds and a low green hill.
        drawCloud(c,cell*5,cell*3,cell*3.5f);drawCloud(c,cell*13,cell*2,cell*2.2f);drawHill(c,cell*9,cell*13,cell*7);
        JSONArray a=records();if(a!=null){for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;int x=(int)Math.round(o.optDouble("xx",0)/16.0),y=(int)Math.round(o.optDouble("yy",0)/16.0);drawPlaced(c,x,y,o.optInt("i",i));}}
        else{for(int i=0;i<tiles.length();i++){JSONObject o=tiles.optJSONObject(i);if(o!=null)drawPlaced(c,o.optInt("x"),o.optInt("y"),o.optInt("type"));}}
        c.restore();
    }
    private void drawCloud(Canvas c,float x,float y,float w){float h=gridCell*.65f;fill(c,0xffd8e9ff,x,y+h*.3f,x+w,y+h);fill(c,0xfff7fbff,x+w*.15f,y,x+w*.8f,y+h*.75f);fill(c,0xfff7fbff,x+w*.35f,y-h*.25f,x+w*.65f,y+h*.75f);fill(c,0xffbceeff,x+w*.12f,y+h*.78f,x+w*.82f,y+h*.9f);}
    private void drawHill(Canvas c,float x,float y,float w){Path path=new Path();path.moveTo(x-w/2,y);path.lineTo(x,y-w*.55f);path.lineTo(x+w/2,y);path.close();p.setColor(0xff55a995);p.setStyle(Paint.Style.FILL);c.drawPath(path,p);p.setColor(0xff3c897c);p.setStrokeWidth(d(2));c.drawLine(x-w*.28f,y-w*.1f,x-w*.2f,y-w*.18f,p);c.drawLine(x+w*.12f,y-w*.28f,x+w*.2f,y-w*.34f,p);}
    private void drawPlaced(Canvas c,int x,int y,int kind){float s=gridCell;float px=x*s,py=y*s;if(tileSheet!=null){sprite(c,tileSheet,Math.max(0,kind*7+3),px,py,s);}else{int[] colors={0xff8b6547,0xffbd7546,0xffe8b83e,0xff778294,0xff45a879,0xffcf5e68};fill(c,colors[Math.floorMod(kind,colors.length)],px+d(1),py+d(1),px+s-d(1),py+s-d(1));} }
    private void drawTop(Canvas c,float w){fill(c,0xffaab5ff,0,0,w,topH);fill(c,0xffe0e3ff,0,topH-d(3),w,topH);float x=d(8),size=topH-d(14),gap=d(8);for(int i=0;i<categories.length;i++){float l=x+i*(size+gap);if(l+size>w-rightW-d(8))break;button(c,l,d(5),l+size,topH-d(7),accents[i],null,"",category==i);if(tileSheet!=null)sprite(c,tileSheet,new int[]{12,45,76,91,108,132}[i],l+d(9),d(13),size-d(18));else txt(c,new String[]{"▦","✿","♟","●","◆","＋"}[i],l+size*.28f,topH*.68f,d(23),0xff3b476e,true);} }
    private void drawLeft(Canvas c,float h){float l=d(7),t=topH+d(8),r=leftW-d(7),b=h-bottomH-d(8);panel(c,l,t,r,b);button(c,l+d(7),t+d(8),r-d(7),t+d(48),0xffd12c16,"SMMWE","",false);txt(c,"关卡设置",l+d(12),t+d(69),d(12),0xff343a66,true);
        button(c,l+d(7),t+d(80),r-d(7),t+d(130),0xff5ed53e,"主题","🌱",false);button(c,l+d(7),t+d(138),r-d(7),t+d(184),0xff20b9d5,"背景","☁",false);
        button(c,l+d(7),t+d(192),r-d(7),t+d(235),0xfff5f5f5,"时间","◷",false);txt(c,"300",l+d(62),t+d(222),d(19),0xff111111,false);
        button(c,l+d(7),t+d(243),r-d(7),t+d(290),0xfff5f5f5,"终点","⚑",false);
        txt(c,"图层",l+d(12),t+d(312),d(12),0xff343a66,true);
        for(int i=0;i<layers.length;i++){float yy=t+d(322+i*34);button(c,l+d(7),yy,r-d(7),yy+d(29),accents[i],layerNames[i]+"  "+layers[i],"",layer.equals(layers[i]));}
        txt(c,erase?"当前工具：擦除":"当前工具：绘制",l+d(12),b-d(12),d(10),0xff343a66,true);
    }
    private void drawRight(Canvas c,float h,float w){float x=w-rightW+d(7),y=topH+d(8),s=rightW-d(14);panel(c,x,y,w-d(7),h-bottomH-d(8));String[] glyph={"☰","◉","▣","♟","⌕"};for(int i=0;i<glyph.length;i++){float yy=y+d(9)+i*(s+d(13));button(c,x+d(7),yy,x+s-d(7),yy+s,accents[(i+1)%accents.length],null,glyph[i],false);}}
    private void drawBottom(Canvas c,float w,float h){fill(c,0xffaeb8ff,0,h-bottomH,w,h);fill(c,0xffdce0ff,0,h-bottomH,w,h-bottomH+d(3));txt(c,"SMMWE LEVEL EDITOR",d(10),h-d(11),d(10),0xff303d76,true);txt(c,Math.round(scale*100)+"%  ·  "+layer,d(175),h-d(11),d(10),0xff303d76,true);button(c,w-d(105),h-bottomH+d(5),w-d(10),h-d(5),0xff303a70,"运行","▶",false);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();boolean landscape=w>h;topH=d(landscape?64:58);bottomH=d(landscape?30:38);leftW=d(landscape?156:112);rightW=d(landscape?70:54);gridCell=d(landscape?38:30);drawTop(c,w);drawBottom(c,w,h);drawGrid(c,leftW,topH,w-rightW,h-bottomH);drawLeft(c,h);drawRight(c,h,w);}
    public void setErase(boolean v,boolean redraw){erase=v;invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();switch(e.getActionMasked()){
        case MotionEvent.ACTION_DOWN:downX=lastX=x;downY=lastY=y;dragging=false;moved=false;return true;
        case MotionEvent.ACTION_POINTER_DOWN:dragging=true;if(e.getPointerCount()>=2)pinchStart=distance(e);return true;
        case MotionEvent.ACTION_MOVE:if(e.getPointerCount()>=2){float dist=distance(e);if(pinchStart>0)scale=Math.max(.55f,Math.min(2.6f,scale*dist/pinchStart));pinchStart=dist;float dx=x-lastX,dy=y-lastY;ox+=dx;oy+=dy;lastX=x;lastY=y;moved=true;invalidate();return true;}if(dragging||Math.abs(x-downX)>d(8)||Math.abs(y-downY)>d(8)){dragging=true;moved=true;ox+=x-lastX;oy+=y-lastY;lastX=x;lastY=y;invalidate();}return true;
        case MotionEvent.ACTION_UP:if(!moved&&!dragging)handleTap(x,y);pinchStart=0;return true;
        case MotionEvent.ACTION_CANCEL:pinchStart=0;return true;
    }return true;}
    private float distance(MotionEvent e){if(e.getPointerCount()<2)return 0;float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);return (float)Math.sqrt(dx*dx+dy*dy);}
    private void handleTap(float x,float y){float w=getWidth(),h=getHeight();if(y<topH){float size=topH-d(14),gap=d(8),start=d(8);int i=(int)((x-start)/(size+gap));if(i>=0&&i<categories.length){category=i;selectedType=i;invalidate();}return;}
        if(x>w-rightW){float y0=topH+d(8)+d(9);float s=rightW-d(28);int i=(int)((y-y0)/(s+d(13)));if(i==0&&actions!=null)actions.menu();else if(i==1&&actions!=null)actions.open();else if(i==2&&actions!=null)actions.save();else if(i==3&&actions!=null)actions.export();else if(i==4){scale=Math.min(2.6f,scale*1.25f);}invalidate();return;}
        if(x<leftW){float yy=y-topH-d(8);if(yy>=d(322)){int i=(int)((yy-d(322))/d(34));if(i>=0&&i<layers.length){layer=layers[i];if(actions!=null)actions.status("已切换到"+layerNames[i]);invalidate();return;}}if(yy<d(290)&&yy>=d(80)){selectedType=yy<d(138)?0:yy<d(192)?1:yy<d(235)?2:3;category=selectedType;invalidate();return;}if(yy>=d(192)&&yy<d(235)){if(actions!=null)actions.properties();return;}return;}
        if(y>h-bottomH){if(x>w-d(115)&&actions!=null)actions.status("预览功能尚未接入");return;}
        int gx=(int)Math.floor((x-leftW-ox)/(gridCell*scale)),gy=(int)Math.floor((y-topH-oy)/(gridCell*scale));if(gx>=0&&gy>=0)place(gx,gy);
    }
    private void place(int x,int y){JSONArray a=records();if(a!=null){int xx=x*16,yy=y*16;for(int i=a.length()-1;i>=0;i--){JSONObject r=a.optJSONObject(i);if(r!=null&&Math.round(r.optDouble("xx")/16)==x&&Math.round(r.optDouble("yy")/16)==y)a.remove(i);}if(!erase){JSONObject proto=null;for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&(layer.equals("S2")?String.valueOf(r.optInt("i")).equals(id):id.equals(r.optString("ID")))){proto=r;break;}}if(layer.equals("S5")&&proto==null&&a.length()>0)proto=a.optJSONObject(0);if(proto==null){if(actions!=null)actions.status("此图层没有可复制的原始对象模板");return;}try{JSONObject n=new JSONObject(proto.toString());n.put("xx",xx);n.put("yy",yy);if(layer.equals("S2"))n.put("i",Integer.parseInt(id));a.put(n);}catch(Exception ignored){}}}else{for(int i=tiles.length()-1;i>=0;i--){JSONObject t=tiles.optJSONObject(i);if(t!=null&&t.optInt("x")==x&&t.optInt("y")==y)tiles.remove(i);}if(!erase)try{JSONObject t=new JSONObject();t.put("x",x);t.put("y",y);t.put("type",selectedType);tiles.put(t);}catch(Exception ignored){}}invalidate();}
}
