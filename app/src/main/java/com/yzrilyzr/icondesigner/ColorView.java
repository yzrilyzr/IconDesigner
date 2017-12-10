package com.yzrilyzr.icondesigner;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.graphics.RectF;

public class ColorView extends MView
{
	public Event e;
	private RectF re;
	public int color=0xff000000;
	private boolean touch=false;
	public boolean selected;
	private Paint pa;
	public ColorView(float x,float y,float w,float h,Event e)
	{
		super(x,y,w,h);
		this.e=e;
		re=new RectF(this);
		re.top-=height();
		re.bottom-=height();
		pa=new Paint(Paint.ANTI_ALIAS_FLAG);
		pa.setStrokeWidth(px(2));
	}
	public void onDraw(Canvas c)
	{
		pa.setColor(color);
		pa.setStyle(Paint.Style.FILL);
		c.drawRoundRect(this,px(5),px(5),pa);
		if(touch){
			pa.setColor(color);
			c.drawRoundRect(re,px(5),px(5),pa);
		}
		if(selected){
			pa.setStyle(Paint.Style.STROKE);
			pa.setColor(buttonselectedcolor);
			c.drawRoundRect(this,px(5),px(5),pa);
		}
	}
	@Override
	public void onTouchEvent(MotionEvent k)
	{
		// TODO: Implement this method
		int a=k.getAction();float x=k.getX(),y=k.getY();
		if(a==MotionEvent.ACTION_DOWN&&contains(x,y))touch=true;
		if(a==MotionEvent.ACTION_UP){
			if(touch&&e!=null)
				if(contains(x,y))this.e.e(this,0);
				else if(re.contains(x,y))this.e.e(this,1);
			touch=false;
		}
	}
	public interface Event
	{
		public abstract void e(ColorView b,int p);
	}
}
