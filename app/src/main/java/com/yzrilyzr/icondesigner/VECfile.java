package com.yzrilyzr.icondesigner;

import java.io.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.yzrilyzr.icondesigner.Shape;
import java.util.concurrent.CopyOnWriteArrayList;
import android.graphics.BitmapFactory;
import java.util.Iterator;

public class VECfile
{
	int width,height;//图像大小
	float dp;//精度
	int backgcolor=0;
	CopyOnWriteArrayList<Shape> shapes=new CopyOnWriteArrayList<Shape>();
	Paint sp=new Paint();//形状
	String name="",comm="",bgpath=null;
	public Bitmap front=null,back=null,front2=null;
	Canvas can,can2;
	boolean antialias=false,dither=false,which=false,lock=false;
	Shape tmpShape;
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
		Bitmap b2=back,f2=front,c2=front2;
		back=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		front=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		front2=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas c=new Canvas(back);
		can=new Canvas(front);
		can2=new Canvas(front2);
		if(b2!=null)b2.recycle();
		if(f2!=null)f2.recycle();
		if(c2!=null)c2.recycle();
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
		if(bgpath!=null)
		{
			Bitmap bb=BitmapFactory.decodeFile(bgpath);
			if(bb!=null)
			{
				c.drawBitmap(bb,0,0,pp);
				bb.recycle();
			}
		}
	}
	public VECfile()
	{}
	public void onDraw()
	{
		if(!lock)
		{
			which=!which;
			if(which)
			{
				sp.reset();
				can.drawBitmap(back,0,0,sp);
				can.drawColor(backgcolor);
				for(Shape s:shapes)s.onDraw(can,antialias,dither,0,0,1,dp,sp);
				if(tmpShape!=null&&!shapes.contains(tmpShape))tmpShape.onDraw(can,antialias,dither,0,0,1,dp,sp);
			}
			else
			{
				sp.reset();
				can2.drawBitmap(back,0,0,sp);
				can2.drawColor(backgcolor);
				for(Shape s:shapes)s.onDraw(can2,antialias,dither,0,0,1,dp,sp);
				if(tmpShape!=null&&!shapes.contains(tmpShape))tmpShape.onDraw(can2,antialias,dither,0,0,1,dp,sp);
			}
		}
	}
	public Bitmap lock(Shape tmp)
	{
		tmpShape=tmp;
		lock=true;
		return which?front2:front;
	}
	public void unlock(){
		lock=false;
	}
	public void loadoutFile(String f)
	{
		try
		{
			Bitmap bit=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			Canvas c=new Canvas(bit);
			c.drawColor(backgcolor);
			Paint pp=new Paint();
			for(Shape s:shapes)s.onDraw(c,antialias,dither,0,0,1,dp,pp);
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
			os.writeByte(4);
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
				if(s.hasFlag(Shape.TYPE.PATH)){
					Iterator it=s.ppt.keySet().iterator();
					os.writeInt(s.ppt.size());
					while(it.hasNext()){
						String k=(String)it.next();
						os.writeInt(Integer.parseInt(k));
						os.write(Byte.parseByte(s.ppt.get(k)));
					}
				}
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
		if(!new String(h,0,3).equals("VEC"))throw new IllegalStateException("不是标准的vec文件");
		else if(h[3]==2)return readFileV2(path);
		else if(h[3]==3)return readFileV3(path);
		else if(h[3]==4)
		{
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
				if(s.hasFlag(Shape.TYPE.PATH)){
					int siz2=os.readInt();
					for(int ik=0;ik<siz2;ik++)
					s.ppt.put(Integer.toString(os.readInt()),Integer.toString(os.read()));
				}
				
				int ptsl=os.readInt();
				s.pts.clear();
				for(int u=0;u<ptsl;u++)
				{
					Point po=new Point();
					po.x=os.readInt();
					po.y=os.readInt();
					s.pts.add(po);
				}
				for(int u=0;u<8;u++)
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
		else return null;
	}
	public static VECfile readFileV3(String path)throws IllegalStateException,IOException
	{
		VECfile v=new VECfile();
		DataInputStream os=new DataInputStream(new FileInputStream(path));
		byte[] h=new byte[4];
		os.read(h);
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
				for(int u=0;u<8;u++)
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
	public static VECfile readFileV2(String path)throws IllegalStateException,IOException
	{
		VECfile v=new VECfile();
		DataInputStream os=new DataInputStream(new FileInputStream(path));
		byte[] h=new byte[4];
		os.read(h);
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
			for(int u=0;u<8;u++)
			{
				s.par[u]=os.readInt();
			}
			os.readInt();
			v.shapes.add(s);
		}
		if(os.available()>0)v.bgpath=os.readUTF();
		os.close();
		v.init(v.width,v.height,v.dp);
		return v;
	}
	public static class Builder
	{
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
		public VECfile build()
		{
			VECfile v=new VECfile(this.width,this.height,this.dp,this.bgpath);
			v.backgcolor=this.backgcolor;
			v.name=this.name;
			v.comm=this.comm;
			v.antialias=this.antialias=false;
			v.dither=this.dither;
			return v;
		}
	}
}
