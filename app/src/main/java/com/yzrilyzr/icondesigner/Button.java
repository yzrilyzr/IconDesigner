package com.yzrilyzr.icondesigner;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.graphics.RectF;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

public class Button extends MView
{
	public String txt;
	public Event e;
	public boolean touch=false;
	public Paint pa;
	public int color=buttoncolor;
	public boolean selected=false;
	private float cx,cy,rr;
	public float rad=px(3);
	public Button(float x,float y,float w,float h,String txt,Event e)
	{
		super(x+3,y+3,w-6,h-6);
		this.txt=txt;
		this.e=e;
		pa=new Paint(Paint.ANTI_ALIAS_FLAG);
		pa.setTextAlign(Paint.Align.CENTER);
		pa.setTextSize(px(12));
		pa.setStrokeWidth(px(2));
	}
	public void onDraw(Canvas c)
	{
		if(selected)
		{
			pa.setStyle(Paint.Style.STROKE);
			pa.setColor(buttonselectedcolor);
			c.drawRect(this,pa);
		}
		pa.setColor(color);
		pa.setStyle(Paint.Style.FILL);
		c.drawRect(this,pa);
		if(cx!=0&&cy!=0)
		{
			c.save();
			c.clipRect(this);
			if(cx!=0&&cy!=0)rr+=width()*0.1f;
			if(rr>width())
			{cx=0;cy=0;rr=0;}
			pa.setColor(0x55000000);
			c.drawCircle(cx,cy,rr,pa);
			c.restore();
		}
		pa.setColor(0xff000000);
		c.drawText(txt,centerX(),centerY()+pa.getTextSize()/2,pa);
	}
	@Override
	public void onTouchEvent(MotionEvent k)
	{
		int a=k.getAction();float x=k.getX(),y=k.getY();
		if(a==MotionEvent.ACTION_DOWN&&contains(x,y))
		{touch=true;cx=x;rr=0;cy=y;}
		if(a==MotionEvent.ACTION_UP)
		{
			if(contains(x,y)&&touch&&e!=null)e.e(this);
			touch=false;
		}
	}
	public interface Event
	{
		public abstract void e(Button b);
	}
}
