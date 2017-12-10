package com.yzrilyzr.icondesigner;
import android.graphics.Canvas;
import android.graphics.RectF;

public class ShapePreview extends Button
{
	float scale=0.5f;
	Shape sh;
	int index;
	RectF up,down;
	public ShapePreview(float x,float y,float w,float h,int ind,Shape sh,Event e)
	{
		super(x,y,w,h,"",e);
		this.sh=sh;
		index=ind;
		if(main.tmpShape==sh)color=buttonselectedcolor;
		long ww=sh.flag;
		ww|=Shape.TYPE.ALL;
		ww-=Shape.TYPE.ALL;
		ww=sh.flag-ww;
		ww/=Shape.TYPE.RECT;
		int hh=0;
		while((ww/=2l)>0)hh++;
		txt=((Button)main.mview.get(9+hh)).txt;
		scale=height()/main.vec.height;
		up=new RectF(left,top,right,bottom);
	}
	@Override
	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int l=c.saveLayer(this,pa,Canvas.ALL_SAVE_FLAG);
		sh.onDraw(c,left/scale,top/scale,scale,main.vec.dp,main.vec.sp);
		pa.setColor(seekbarcolor);
		c.drawRect(up,pa);
		c.restoreToCount(l);
	}
}
