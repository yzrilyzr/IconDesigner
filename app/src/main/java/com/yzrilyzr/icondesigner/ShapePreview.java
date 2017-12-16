package com.yzrilyzr.icondesigner;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.graphics.Paint;

public class ShapePreview extends Button
{
	float scale=0.5f;
	Shape sh;
	int index;
	RectF up,down;
	boolean issub=false;
	Paint pp;
	public ShapePreview(float x,float y,float w,float h,int ind,Shape sh,Event e)
	{
		super(x,y,w,h,"",e);
		this.sh=sh;
		this.index=ind;
		if(render.tmpShape==sh)color=buttonselectedcolor;
		long ww=sh.flag;
		ww|=Shape.TYPE.ALL;
		ww-=Shape.TYPE.ALL;
		ww=sh.flag-ww;
		ww/=Shape.TYPE.RECT;
		int hh=0;
		while((ww/=2l)>0)hh++;
		txt=((Button)render.mview.get(9+hh)).txt;
		scale=height()/render.vec.height;
		up=new RectF(left+width()/3*2,0,right,0);
		down=new RectF(up);
		pp=new Paint();
	}
	@Override
	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		pa.setColor(seekbarcolor);
		up.top=top;
		up.bottom=centerY();
		down.top=centerY();
		down.bottom=bottom;
		c.drawRoundRect(up,rad,rad,pa);
		c.drawRoundRect(down,rad,rad,pa);
		pa.setColor(0xff000000);
		c.drawText("上层",up.centerX(),up.centerY()+pa.getTextSize()/2,pa);
		c.drawText("下层",down.centerX(),down.centerY()+pa.getTextSize()/2,pa);
		int l=c.saveLayer(this,pa,Canvas.ALL_SAVE_FLAG);
		sh.onDraw(c,render.vec.antialias,render.vec.dither,left/scale,top/scale,scale,render.vec.dp,pp);
		c.restoreToCount(l);
	}

	@Override
	public void onTouchEvent(MotionEvent k)
	{
		int a=k.getAction();float x=k.getX(),y=k.getY();
		if(a==MotionEvent.ACTION_DOWN&&(up.contains(x,y)||down.contains(x,y)))issub=true;
		else if(a==MotionEvent.ACTION_UP&&issub)
		{
			if(up.contains(x,y)){
				List lis=(List)parent;
				render.vec.shapes.remove(index);
				lis.views.remove(index);
				if(--index<0)index=0;
				render.vec.shapes.add(index,sh);
				lis.views.add(index,this);
				int kk=0;
				for(MView v:lis.views)
					((ShapePreview)v).index=kk++;
			}
			else if(down.contains(x,y)){
				List lis=(List)parent;
				render.vec.shapes.remove(index);
				lis.views.remove(index);
				if(++index>render.vec.shapes.size())index=render.vec.shapes.size();
				render.vec.shapes.add(index,sh);
				lis.views.add(index,this);
				int kk=0;
				for(MView v:lis.views)
					((ShapePreview)v).index=kk++;
			}
			issub=false;
		}
		if(!issub)super.onTouchEvent(k);
	}
	
}
