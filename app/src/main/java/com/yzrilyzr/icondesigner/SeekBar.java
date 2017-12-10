package com.yzrilyzr.icondesigner;

import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

public class SeekBar extends MView
{
	private float padd;
	private int progress=0,max=100;
	private Paint p;
	public SeekBarEvent e;
	private float kx;
	private boolean tt;
	public SeekBar(float x,float y,float w,float h,int pro,int mx){
		super(x,y,w,h);
		padd=px(5);
		p=new Paint(Paint.ANTI_ALIAS_FLAG);
		progress=pro;
		max=mx;
	}
	public void setProgress(int progress)
	{
		this.progress = progress;
	}
	public int getProgress()
	{
		return progress;
	}
	public void setMax(int max)
	{
		this.max = max;
	}
	public int getMax()
	{
		return max;
	}
	@Override
	public void onDraw(Canvas c)
	{
		p.setColor(seekbarcolor);
		RectF r=new RectF(left+padd*2,centerY()-padd,right-padd*2,centerY()+padd);
		c.drawRoundRect(r,padd,padd,p);
		p.setColor(0xffffffff);
		c.drawCircle(tt?kx:r.left+r.width()*(float)progress/(float)max,r.centerY(),padd*2,p);
	}
	@Override
	public void onTouchEvent(MotionEvent e)
	{
		if(e.getAction()==MotionEvent.ACTION_UP)tt=false;
		else tt=true;
		RectF r=new RectF(left+padd*2,centerY()-padd,right-padd*2,centerY()+padd);
		kx=e.getX();
		if(kx<r.left)kx=r.left;
		else if(kx>r.right)kx=r.right;
		int g=Math.round(max*(kx-r.left)/r.width());
		if(g<0)g=0;
		if(g>max)g=max;
		progress=g;
		if(this.e!=null)this.e.onChange(this,progress);
	}
	public interface SeekBarEvent{
		public abstract void onChange(SeekBar s,int p);
	}
}
