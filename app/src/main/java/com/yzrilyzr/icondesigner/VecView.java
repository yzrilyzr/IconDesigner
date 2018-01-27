package com.yzrilyzr.icondesigner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.yzrilyzr.longtexteditor.R;
import java.io.IOException;
import java.io.InputStream;

public class VecView extends ImageView
{
	private VECfile vec;
	public VecView(Context c,AttributeSet a){
		super(c,a);
		try{
		TypedArray t=c.obtainStyledAttributes(a,R.styleable.yzr);
		String d=t.getString(R.styleable.yzr_src)+".vec";
		InputStream i=c.getAssets().open(d);
		vec=VECfile.readFileFromIs(i);
		}catch(Throwable e){}
	}
	@Override
	protected void onDraw(Canvas canvas)
	{
		if(vec!=null)
		for(Shape s:vec.shapes)
		s.onDraw(canvas,vec.antialias,vec.dither,0,0,(float)getWidth()/(float)vec.width,vec.dp,vec.sp);
	}
	public VecView(Context c){
		this(c,null);
	}
	public void setVec(String assetPath){
		try
		{
			InputStream is=getContext().getAssets().open(assetPath+".vec");
			vec=VECfile.readFileFromIs(is);
		}
		catch (IOException e)
		{
		}
	}
}
