package com.yzrilyzr.icondesigner;

import android.graphics.*;
import android.view.inputmethod.*;
import java.io.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.Toast;
import com.yzrilyzr.icondesigner.MView;
import com.yzrilyzr.icondesigner.Shape;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
public class MainView extends SurfaceView implements Callback
{
	public static RenderThread render;
	public MainView(Context c)
	{
		super(c);
		getHolder().addCallback(this);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder p1)
	{
	}
	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4)
	{
	}
	@Override
	public void surfaceCreated(SurfaceHolder p1)
	{
		if(render==null)
		{
			render=new RenderThread(this);
			render.start();
		}
		else render.surface=this;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return render.touch(event);
	}
	@Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
        return render;
    }
}