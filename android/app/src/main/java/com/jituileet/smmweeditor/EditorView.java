package com.jituileet.smmweeditor;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import org.json.*;

public class EditorView extends View {
    private final Paint p=new Paint(3); private JSONArray tiles=new JSONArray(); private JSONObject swe; private String layer="S2",id="49"; private boolean erase=false,drag=false; private int selectedType=0; private float scale=1,ox=20,oy=20,lx,ly; private final float cell=32;
    private final int[] colors={0xff8b6547,0xffbd7546,0xffe8b83e,0xff778294,0xff45a879,0xffcf5e68};
    public EditorView(Context c){super(c);setBackgroundColor(0xfffafbfd);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setErase(boolean v){erase=v;invalidate();}\n    public void setTileType(int type){selectedType=Math.max(0,Math.min(5,type));erase=false;invalidate();}
    public void setLayer(String l,String i){layer=l;id=i;invalidate();}
    public void setModel(JSONArray t,JSONObject s,String l,String i){tiles=t==null?new JSONArray():t;swe=s;layer=l;id=i;invalidate();}
    public JSONArray getTiles(){return tiles;}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.translate(ox,oy);c.scale(scale,scale);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1/scale);p.setColor(0xffd8dee8);for(int x=0;x<90;x++)c.drawLine(x*cell,0,x*cell,Math.max(getHeight()*2,1200),p);for(int y=0;y<40;y++)c.drawLine(0,y*cell,Math.max(getWidth()*2,2400),y*cell,p);
        JSONArray a=records();if(a!=null){for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r==null)continue;float x=(float)r.optDouble("xx",0)/16*cell,y=(float)r.optDouble("yy",0)/16*cell;p.setStyle(Paint.Style.FILL);p.setColor(layer.equals("S2")?Color.HSVToColor(new float[]{(r.optInt("i")*47)%360,0.42f,0.72f}):layer.equals("S4")?0xffc75d68:layer.equals("S3")?0xff9b75c7:layer.equals("S5")?0xff4f9eb5:0xffd49a4d);c.drawRect(x+1,y+1,x+cell-1,y+cell-1,p);p.setColor(Color.WHITE);p.setTextSize(10);c.drawText(layer.equals("S2")?String.valueOf(r.optInt("i")):layer,x+4,y+cell/2,p);}}
        else{for(int i=0;i<tiles.length();i++){JSONObject t=tiles.optJSONObject(i);if(t==null)continue;int x=t.optInt("x"),y=t.optInt("y"),k=t.optInt("type");if(k<0||k>=colors.length)continue;p.setStyle(Paint.Style.FILL);p.setColor(colors[k]);c.drawRect(x*cell+1,y*cell+1,(x+1)*cell-1,(y+1)*cell-1,p);p.setColor(Color.WHITE);p.setTextSize(16);String[] glyph={"","B","?","■","G","E"};c.drawText(glyph[k],x*cell+cell*.3f,y*cell+cell*.65f,p);}}c.restore();}
    private JSONArray records(){if(swe==null)return null;JSONObject s=swe.optJSONObject("S0");return s==null?null:s.optJSONArray(layer);}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:lx=x;ly=y;drag=false;return true;case MotionEvent.ACTION_POINTER_DOWN:drag=true;return true;case MotionEvent.ACTION_MOVE:if(drag||e.getPointerCount()>1){ox+=x-lx;oy+=y-ly;lx=x;ly=y;invalidate();return true;}return true;case MotionEvent.ACTION_UP:if(!drag){int gx=(int)Math.floor((x-ox)/(cell*scale)),gy=(int)Math.floor((y-oy)/(cell*scale));if(gx>=0&&gy>=0&&gx<500&&gy<100)place(gx,gy);}return true;}return true;}
    private void place(int x,int y){JSONArray a=records();if(a!=null){int xx=x*16,yy=y*16;for(int i=a.length()-1;i>=0;i--){JSONObject r=a.optJSONObject(i);if(r!=null&&Math.round(r.optDouble("xx")/16)==x&&Math.round(r.optDouble("yy")/16)==y)a.remove(i);}if(!erase){JSONObject proto=null;for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&(layer.equals("S2")?String.valueOf(r.optInt("i")).equals(id):id.equals(r.optString("ID")))){proto=r;break;}}if(layer.equals("S5")&&proto==null&&a.length()>0)proto=a.optJSONObject(0);if(proto==null)return;try{JSONObject n=new JSONObject(proto.toString());n.put("xx",xx);n.put("yy",yy);if(layer.equals("S2"))n.put("i",Integer.parseInt(id));a.put(n);}catch(Exception ignored){}}}else{for(int i=tiles.length()-1;i>=0;i--){JSONObject t=tiles.optJSONObject(i);if(t!=null&&t.optInt("x")==x&&t.optInt("y")==y)tiles.remove(i);}if(!erase)try{JSONObject t=new JSONObject();t.put("x",x);t.put("y",y);t.put("type",selectedType);tiles.put(t);}catch(Exception ignored){}}invalidate();}
}
