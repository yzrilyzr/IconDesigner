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
import android.content.SharedPreferences;
public class MainActivity extends Activity 
{
	long time=0;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(new MainView(this));
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
			MainView.render.builder.setBgpath(picturePath);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK&&time+1000<System.currentTimeMillis())
		{
			MainView.render.toast("再按一次退出");
			time=System.currentTimeMillis();
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}
}
