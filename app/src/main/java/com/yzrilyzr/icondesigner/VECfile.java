package com.yzrilyzr.icondesigner;

import java.io.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.yzrilyzr.icondesigner.Shape;
import java.util.concurrent.CopyOnWriteArrayList;
import android.graphics.BitmapFactory;

public class VECfile
{
	int width,height;//图像大小
	float dp;//精度
	int backgcolor=0;
	CopyOnWriteArrayList<Shape> shapes=new CopyOnWriteArrayList<Shape>();
	Paint sp=new Paint();//形状
	String name="",comm="",bgpath=null;
	public Bitmap front,back;
	Canvas can;
	boolean antialias=false,dither=false;
	public VECfile(int width,int height,float dp,String b)
	{
		bgpath=b;
		init(width, height,dp);
	}
	public void addShape(Shape s)
	{
		if(s!=null)shapes.add(s);
	}
	public void init(int width, int height,float dp)
	{
		this.width=width;
		this.height=height;
		this.dp=dp;
		Paint pp=new Paint();
		setHeap((long)width*(long)height*10l);
		back=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		front=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas c=new Canvas(back);
		can=new Canvas(front);
		boolean grey=false;
		int w=width/25;
		for(int i=0;i<width;i+=w)
		{
			grey=!grey;
			boolean g2=grey;
			for(int j=0;j<height;j+=w)
			{
				g2=!g2;
				pp.setColor(g2?0xffffffff:0xffaaaaaa);
				c.drawRect(i,j,i+w,j+w,pp);
			}
		}
		if(bgpath!=null){
			Bitmap bb=BitmapFactory.decodeFile(bgpath);
			if(bb!=null){
			c.drawBitmap(bb,0,0,pp);
			bb.recycle();
			}
		}
	}
	public static void setHeap(long l)
	{
		try
		{
			Class cls=Class.forName("dalvik.system.VMRuntime");
			Object o=cls.getMethod("getRuntime").invoke(cls);
			o.getClass().getMethod("setMinimumHeapSize",long.class).invoke(o,l);
		}
		catch (Exception e)
		{}
	}
	public void initPosition(Main m)
	{
		m.setPosition((m.getWidth()-width)/2,(m.getHeight()-height)/2,1);
	}
	public VECfile()
	{}
	public void recycle()
	{
		front.recycle();
		back.recycle();
		back=null;
		front=null;
		System.gc();
	}
	public void onDraw()
	{
		sp.setAntiAlias(antialias);
		sp.setDither(dither);
		if(back!=null)can.drawBitmap(back,0,0,sp);
		can.drawColor(backgcolor);
		for(Shape s:shapes)s.onDraw(can,0,0,1,dp,sp);
	}
	public void loadoutFile(String f)
	{
		try
		{
			Bitmap bit=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			Canvas c=new Canvas(bit);
			c.drawColor(backgcolor);
			for(Shape s:shapes)s.onDraw(c,0,0,1,dp,sp);
			BufferedOutputStream os=new BufferedOutputStream(new FileOutputStream(f));
			bit.compress(Bitmap.CompressFormat.PNG,0,os);
			os.close();
			bit.recycle();
		}
		catch (Exception e)
		{}
	}

	public void saveFile(String path)
	{
		try
		{
			DataOutputStream os=new DataOutputStream(new FileOutputStream(path));
			os.writeBytes("VEC");
			os.writeByte(2);
			os.writeUTF(name);
			os.writeUTF(comm);
			os.writeInt(width);
			os.writeInt(height);
			os.writeBoolean(antialias);
			os.writeBoolean(dither);
			os.writeInt(backgcolor);
			os.writeFloat(dp);
			os.writeInt(shapes.size());
			for(Shape s:shapes)
			{
				/*	public Shader shader=null;
				 public PathEffect pathEffect=null;*/
				os.writeLong(s.flag);
				if(s.hasFlag(Shape.TYPE.TEXT))os.writeUTF(s.txt);
				os.writeInt(s.pts.size());
				for(Point po:s.pts)
				{
					os.writeInt(po.x);
					os.writeInt(po.y);
				}
				for(int po:s.par)os.writeInt(po);
			}
			os.writeUTF(bgpath);
			os.flush();
			os.close();
		}
		catch (Exception e)
		{}
	}

	public static VECfile readFile(String path)throws IllegalStateException,IOException
	{
		VECfile v=new VECfile();
			DataInputStream os=new DataInputStream(new FileInputStream(path));
			byte[] h=new byte[4];
			os.read(h);
			if(h[3]!=2||!new String(h,0,3).equals("VEC"))throw new IllegalStateException("不是标准的vec文件");
			v.name=os.readUTF();
			v.comm=os.readUTF();
			v.width=os.readInt();
			v.height=os.readInt();
			v.antialias=os.readBoolean();
			v.dither=os.readBoolean();
			v.backgcolor=os.readInt();
			v.dp=os.readFloat();
			v.shapes.clear();
			int siz=os.readInt();
			for(int i=0;i<siz;i++)
			{
				Shape s=new Shape(0);
				s.flag=os.readLong();
				if(s.hasFlag(Shape.TYPE.TEXT))s.txt=os.readUTF();
				int ptsl=os.readInt();
				s.pts.clear();
				for(int u=0;u<ptsl;u++)
				{
					Point po=new Point();
					po.x=os.readInt();
					po.y=os.readInt();
					s.pts.add(po);
				}
				for(int u=0;u<s.par.length;u++)
				{
					s.par[u]=os.readInt();
				}
				v.shapes.add(s);
			}
			if(os.available()>0)v.bgpath=os.readUTF();
			os.close();
			v.init(v.width,v.height,v.dp);
			return v;
	}
	public static class Builder{
		int width=500,height=500;//图像大小
		float dp=20;//精度
		int backgcolor=0xff000000;
		String name="未命名",comm="请输入描述",bgpath=null;
		boolean antialias=false,dither=false;
		public void setWidth(int width)
		{
			this.width = width;
		}

		public int getWidth()
		{
			return width;
		}

		public void setHeight(int height)
		{
			this.height = height;
		}

		public int getHeight()
		{
			return height;
		}

		public void setDp(float dp)
		{
			this.dp = dp;
		}

		public float getDp()
		{
			return dp;
		}

		public void setBackgcolor(int backgcolor)
		{
			this.backgcolor = backgcolor;
		}

		public int getBackgcolor()
		{
			return backgcolor;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public void setComm(String comm)
		{
			this.comm = comm;
		}

		public String getComm()
		{
			return comm;
		}

		public void setBgpath(String bgpath)
		{
			this.bgpath = bgpath;
		}

		public String getBgpath()
		{
			return bgpath;
		}

		public void setAntialias(boolean antialias)
		{
			this.antialias = antialias;
		}

		public boolean isAntialias()
		{
			return antialias;
		}

		public void setDither(boolean dither)
		{
			this.dither = dither;
		}

		public boolean isDither()
		{
			return dither;
		}
		public VECfile build(VECfile v2,Main main){
			v2.recycle();
			VECfile v=new VECfile(this.width,this.height,this.dp,this.bgpath);
			v.backgcolor=this.backgcolor;
			v.name=this.name;
			v.comm=this.comm;
			v.antialias=this.antialias=false;
			v.dither=this.dither;
			v.initPosition(main);
			return v;
		}
	}
}
