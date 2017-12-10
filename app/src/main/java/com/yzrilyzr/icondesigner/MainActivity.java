package com.yzrilyzr.icondesigner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import android.view.KeyEvent;
public class MainActivity extends Activity 
{
	static String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/yzr的app/";
	Main main;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
				@Override
				public void uncaughtException(Thread p1, Throwable p2)
				{
					try
					{
						FileOutputStream o=new FileOutputStream(path+"err.txt");
						PrintStream p=new PrintStream(o);
						p2.printStackTrace(p);
						o.flush();
						o.close();
					}
					catch (Exception e)
					{}
					System.exit(0);
				}
			});
		setContentView(main=new Main(this));
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 3 && resultCode == RESULT_OK && null != data)
		{  
			Uri selectedImage = data.getData();  
			String[] filePathColumn = { MediaStore.Images.Media.DATA };  
			Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);  
			cursor.moveToFirst();  
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
			final String picturePath = cursor.getString(columnIndex);  
			cursor.close();
			Main.builder.setBgpath(picturePath);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return true;
	}
}
