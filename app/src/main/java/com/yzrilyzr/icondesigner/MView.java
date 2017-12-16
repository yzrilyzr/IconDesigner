package com.yzrilyzr.icondesigner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.yzrilyzr.icondesigner.MView;
import java.util.ArrayList;

public class MView extends RectF
{
	public static int
	shadowcolor=0x00555555,
	buttoncolor=0xff40ffe0,
	menucolor=0x90a0e0a0,
	editcolor=0xffffffff,
	buttonselectedcolor=0xffffff66,
	seekbarcolor=0xff33ff66,
	unavailablecolor=0xff909090,
	toastcolor=0xff66ccff;
	public static RenderThread render;
	public MView parent;
	public void onDraw(Canvas c)
	{}
	public void onTouchEvent(MotionEvent e)
	{}
	public MView(float x,float y,float w,float h)
	{
		super(px(x),px(y),px(x+w),px(y+h));
	}
	public static int px(float i)
	{
		return (int)(i*render.dpi);
	}
	public static int dip(float i)
	{
		return (int)(i/render.dpi);
	}
}
