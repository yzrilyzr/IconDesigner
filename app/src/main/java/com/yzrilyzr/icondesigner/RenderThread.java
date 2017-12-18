package com.yzrilyzr.icondesigner;

import android.graphics.*;
import android.view.inputmethod.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.EditText;
import com.yzrilyzr.icondesigner.MView;
import com.yzrilyzr.icondesigner.MainActivity;
import com.yzrilyzr.icondesigner.R;
import com.yzrilyzr.icondesigner.Shape;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

public class RenderThread extends Thread implements InputConnection,Thread.UncaughtExceptionHandler
{
	public MainActivity ctx;
	public float dpi;
	public VECfile vec=new VECfile(384,384,3.84f,null);//文件
	public VECfile.Builder builder=new VECfile.Builder();
	public Shape tmpShape,colorShape=new Shape(Shape.STYLE.FILL);//临时和颜色
	public Point tmpPoint,tmpPoint2;//临时和回退
	public int alpha=255;
	public MView curView;//逻辑view
	public Bitmap icon;
	public int ram=0,fps=0;
	public float deltax,deltay,scale,lscale,lpointLen;//上次缩放，长度
	public CopyOnWriteArrayList<CopyOnWriteArrayList<Shape>> undo=new CopyOnWriteArrayList<CopyOnWriteArrayList<Shape>>();
	public CopyOnWriteArrayList<CopyOnWriteArrayList<Shape>> redo=new CopyOnWriteArrayList<CopyOnWriteArrayList<Shape>>();
	public boolean useNet=true,moved=false;
	public int cx,cy;//指针
	public float ddx,ddy;//上次触摸
	public int pointIndex=0;//path点
	public int MODE=0;//触摸模式
	public ColorView curColorView;//当前颜色
	public CopyOnWriteArrayList<MView> mview=new CopyOnWriteArrayList<MView>();
	public StringBuilder info=new StringBuilder();
	public File localFile=new File(MainActivity.path);
	public SurfaceView surface;
	public Toast toast;
	public RenderThread(SurfaceView surface)
	{
		this.surface = surface;
		this.ctx=(MainActivity)surface.getContext();
		MView.render=this;
		dpi=ctx.getResources().getDisplayMetrics().density;
		icon=BitmapFactory.decodeResource(surface.getResources(),R.drawable.icon);
		Matrix m=new Matrix();
		m.postScale((float)MView.px(80)/(float)icon.getWidth(),(float)MView.px(80)/(float)icon.getHeight());
		icon=Bitmap.createBitmap(icon,0,0,icon.getWidth(),icon.getHeight(),m,false);
		Bitmap la=Bitmap.createBitmap(surface.getWidth(),surface.getHeight(),Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(la);
		Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(MView.px(18));
		canvas.drawColor(MView.buttoncolor);
		canvas.drawBitmap(icon,(surface.getWidth()-icon.getWidth())/2,(surface.getHeight()-icon.getHeight())/2,paint);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(0xff000000);
		canvas.drawText("图标设计",surface.getWidth()/2,surface.getHeight()*0.6f,paint);
		canvas.drawText("@Besto Design",surface.getWidth()/2,surface.getHeight()*0.7f,paint);
		icon.recycle();
		icon=la;
		initUi();
		new Thread(){
			@Override
			public void run()
			{
				while(true)
					try
					{
						vec.onDraw();
					}
					catch(Throwable e)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e2)
						{}
					}
			}
		}.start();
	}
	@Override
	public void run()
	{
		setName("Render");
		Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(MView.px(18));
		long time;
		Runtime ru=Runtime.getRuntime();
		while(true)
			try
			{
				time=System.nanoTime();
				Canvas c=surface.getHolder().lockCanvas();
				if(c!=null)
				{
					draw(c,paint);
					surface.getHolder().unlockCanvasAndPost(c);
				}
				else Thread.sleep(20);
				ram=(int)((ru.totalMemory()-ru.freeMemory())*100/ru.maxMemory());
				if(alpha>0)alpha-=5;
				time=System.nanoTime()-time;
				if(time!=0)fps=(int)(1000000000/time+fps*3)/4;
			}
			catch(Throwable e)
			{
				try
				{
					ByteArrayOutputStream os=new ByteArrayOutputStream();
					PrintWriter ps=new PrintWriter(os);
					e.printStackTrace(ps);
					ps.flush();
					ps.close();
					toast("render:"+os.toString());
					Thread.sleep(5000);
				}
				catch (Exception e2)
				{
				}
			}
	}
	@Override
	public void uncaughtException(Thread p1, Throwable p2)
	{
		try
		{
			FileOutputStream os=new FileOutputStream(ctx.path+"err.txt");
			PrintWriter ps=new PrintWriter(os);
			p2.printStackTrace(ps);
			ps.flush();
			ps.close();
		}
		catch (Exception e)
		{}
		System.exit(0);
	}
	public void draw(Canvas canvas,Paint paint)
	{
		canvas.drawColor(0xff666666);
		Matrix m=new Matrix();
		m.postTranslate(deltax,deltay);
		m.postScale(scale,scale);
		canvas.drawBitmap(vec.lock(tmpShape),m,paint);
		vec.unlock();
		if(useNet)
		{
			paint.setColor(0xff000000);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			for(float i=deltax*scale;i<(vec.width+deltax)*scale&&vec!=null;i+=vec.dp*scale)
				if(i<0)continue;
				else if(i>surface.getWidth())break;
				else canvas.drawLine(i,deltay*scale,i,(vec.height+deltay)*scale,paint);
			for(float j=deltay*scale;j<(vec.height+deltay)*scale&&vec!=null;j+=vec.dp*scale)
				if(j<0)continue;
				else if(j>surface.getHeight())break;
				else canvas.drawLine(deltax*scale,j,(vec.width+deltax)*scale,j,paint);
		}
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xff000000);
		canvas.drawCircle((cx*vec.dp+deltax)*scale,(cy*vec.dp+deltay)*scale,10,paint);
		paint.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(String.format("%d,%d;shapes:%d;fps:%d,RAM:%d,HA:%b",cx,cy,vec.shapes.size(),fps,ram,canvas.isHardwareAccelerated()),0,paint.getTextSize()*3.2f,paint);
		for(MView b:mview)b.onDraw(canvas);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(info.toString(),surface.getWidth()/2,surface.getHeight()/2,paint);
		if(alpha>0)
		{
			m=new Matrix();
			if(alpha<150){
				float k=Math.max(alpha,130);
				m.postScale(k/150f,k/150f);
				m.postTranslate(icon.getWidth()/2f*(1f-k/150f),icon.getHeight()/2f*(1f-k/150f));
			}
			if(alpha<100){
				m.postTranslate(0,(alpha-100f)/100f*icon.getHeight());
				paint.setAlpha((int)(alpha*2.55f));
			}
			canvas.drawBitmap(icon,m,paint);
		}
	}
	public boolean touch(MotionEvent event)
	{
		try
		{
			setInfo();
			int a=event.getAction();
			if(event.getPointerCount()==1)
			{
				float x=event.getX(),y=event.getY();
				if(a==MotionEvent.ACTION_DOWN)
				{
					curView=null;
					moved=false;
					if(MODE==1)MODE=2;
					else if(MODE==2)MODE=0;
					else if(MODE==3)MODE=4;
					else if(MODE==4)MODE=0;
					else if(MODE==5)MODE=6;
					else if(MODE==6)MODE=0;
					if(tmpPoint!=null)tmpPoint2=new Point(tmpPoint);
					else tmpPoint2=null;
					for(int i=mview.size()-1;i!=-1;i--)
					{
						MView b=mview.get(i);
						if(b.contains(x,y))
						{
							if(b instanceof Menu&&!((Menu)b).show)continue;
							curView=b;
							break;
						}
					}
				}
				if(curView!=null)curView.onTouchEvent(event);
				else if(!moved)
				{
					cx=Math.round((x-deltax*scale)/(vec.dp*scale));
					cy=Math.round((y-deltay*scale)/(vec.dp*scale));
					if(MODE==2)
					{
						curColorView.color=vec.front.getPixel(Math.round(x/scale-deltax),Math.round(y/scale-deltay));
						setTmpShape();
					}
					else if(MODE==4&&tmpShape!=null&&tmpShape.hasFlag(Shape.TYPE.PATH))
					{
						Point poi=new Point(cx,cy);
						int size=tmpShape.pts.size();
						Point poi2=tmpShape.pts.get(size-1);
						if(size==1||!poi.equals(poi2))tmpShape.pts.add(poi);
					}
					else if(MODE==6)
					{
						if(a==MotionEvent.ACTION_DOWN)tmpPoint2=new Point(cx,cy);
						else
						{
							colorShape.par[4]=cx-tmpPoint2.x;
							colorShape.par[5]=cy-tmpPoint2.y;
							setTmpShape();
						}
					}
					else if(tmpPoint!=null)
					{
						tmpPoint.x=cx;
						tmpPoint.y=cy;
					}
				}
			}
			else if(event.getPointerCount()==2)
			{
				float x1=event.getX(1),y1=event.getY(1);
				float x=event.getX(0),y=event.getY(0);
				if(!moved)
				{
					ddx=(x+x1)/2;
					ddy=(y+y1)/2;
					lpointLen=(float)Math.sqrt(Math.pow(x-x1,2)+Math.pow(y-y1,2));
					lscale=scale;
					if(tmpPoint!=null&&tmpPoint2!=null)
					{
						tmpPoint.x=tmpPoint2.x;
						tmpPoint.y=tmpPoint2.y;
					}
					moved=true;
				}
				else
				{
					float pointLen=(float)Math.sqrt(Math.pow(x-x1,2)+Math.pow(y-y1,2));
					float llsc=scale;
					scale=lscale*pointLen/lpointLen;
					float cx=(x+x1)/2f,cy=(y+y1)/2f;
					deltax=(deltax-cx/llsc)+cx/scale;
					deltay=(deltay-cy/llsc)+cy/scale;
					deltax-=(ddx-(x+x1)/2)/scale;
					deltay-=(ddy-(y+y1)/2)/scale;
					ddx=(x+x1)/2;
					ddy=(y+y1)/2;
					setInfo((int)(scale*100f),"%");
				}
			}
			else return false;
		}
		catch(Throwable e)
		{
			toast("event:"+e);
		}
		return true;
	}
	public void initPosition()
	{
		deltax=(surface.getWidth()-vec.width)/2;
		deltay=(surface.getHeight()-vec.height)/2;
		scale=1;
	}
	public void initUi()
	{
		initPosition();
		mview.clear();
		final Button[] bu=new Button[27];
		final int bs=MView.dip(surface.getWidth()/9);
		final Menu[] me=new Menu[26];
		Button.Event ev=new Button.Event(){
			@Override
			public void e(Button b)
			{
				if(b==bu[0])showMenu(me[24]);
				else if(b==bu[1])
				{
					Menu m=me[22];
					showMenu(m);
					listfile((List)m);
				}
				else if(b==bu[2])
				{
					Menu m=me[20];
					showMenu(m);
					List lis=(List)m.views.get(2);
					lis.show=true;
					((Edit)m.views.get(0)).txt=vec.name;
					listfile(lis);
				}
				else if(b==bu[3])
				{
					Menu m=me[23];
					showMenu(m);
					List lis=(List)m.views.get(2);
					lis.show=true;
					((Edit)m.views.get(0)).txt=vec.name;
					listfile(lis);
				}
				else if(b==bu[4])
				{
					Menu m=me[25];
					((Edit)m.views.get(1)).txt=vec.name;
					((Edit)m.views.get(3)).txt=vec.comm;
					((Edit)m.views.get(5)).txt=vec.width+"";
					((Edit)m.views.get(7)).txt=vec.height+"";
					((Edit)m.views.get(9)).txt=vec.width/vec.dp+"";
					((SeekBar)m.views.get(14)).setProgress(vec.antialias?1:0);
					((SeekBar)m.views.get(16)).setProgress(vec.dither?1:0);
					showMenu(me[25]);
				}
				else if(b==bu[5])
				{
					List m=(List)me[19];
					showMenu(m);
					m.views.clear();
					int k=0;
					for(Shape s:vec.shapes)
					{
						m.addView(new ShapePreview(bs,0,bs*7,bs*3,k++,s,new Button.Event(){
										  @Override
										  public void e(Button b)
										  {
											  for(int j=0;j<9;j++)me[j].show=false;
											  tmpShape=((ShapePreview)b).sh;
											  colorShape.set(tmpShape);
											  long w=tmpShape.flag;
											  w|=Shape.TYPE.ALL;
											  w-=Shape.TYPE.ALL;
											  w=tmpShape.flag-w;
											  w/=Shape.TYPE.RECT;
											  int h=0;
											  while((w/=2l)>0)h++;
											  showMenu(me[h]);
											  me[19].show=false;
										  }
									  }));
					}
					m.measure();
				}
				else if(b==bu[6])useNet=!useNet;
				else if(b==bu[7])undo();
				else if(b==bu[8])redo();
				else
				{
					for(int i=0;i<9;i++)
						if(b==bu[9+i])
						{
							if(tmpShape==null)
							{
								tmpShape=new Shape(Shape.TYPE.RECT*(long)Math.pow(2,i));
								setTmpShape();
								pointIndex=0;
								showMenu(me[i]);
							}
							else
							{
								for(int j=0;j<9;j++)me[j].show=false;
								tmpShape=null;
							}
							break;
						}
					for(int i=9;i<18;i++)
						if(b==bu[9+i])
						{
							showMenu(me[i]);
							break;
						}
				}
			}
			public void undo()
			{
				if(undo.size()==0)bu[7].color=MView.unavailablecolor;
				else
				{
					bu[7].color=MView.buttoncolor;
					CopyOnWriteArrayList<Shape> list=undo.get(undo.size()-1);
					undo.remove(undo.size()-1);
					CopyOnWriteArrayList<Shape> list2=new CopyOnWriteArrayList<Shape>();
					for(Shape s:vec.shapes)list2.add(new Shape(s));
					redo.add(list2);
					bu[8].color=MView.buttoncolor;
					if(undo.size()==0)bu[7].color=MView.unavailablecolor;
					vec.shapes.clear();
					for(Shape s:list)vec.shapes.add(new Shape(s));
				}
			}
			public void redo()
			{
				if(redo.size()==0)bu[8].color=MView.unavailablecolor;
				else
				{
					bu[8].color=MView.buttoncolor;
					CopyOnWriteArrayList<Shape> list=redo.get(redo.size()-1);
					redo.remove(redo.size()-1);
					CopyOnWriteArrayList<Shape> list2=new CopyOnWriteArrayList<Shape>();
					for(Shape s:vec.shapes)list2.add(new Shape(s));
					undo.add(list2);
					bu[7].color=MView.buttoncolor;
					if(redo.size()==0)bu[8].color=MView.unavailablecolor;
					vec.shapes.clear();
					for(Shape s:list)vec.shapes.add(new Shape(s));
				}
			}
			public void listfile(final List m)
			{
				m.views.clear();
				File[] src=localFile.listFiles();
				if(src==null)src=new File[0];
				final File[] dst=new File[src.length+1];
				Arrays.sort(src,new Comparator<File>(){
						@Override
						public int compare(File p1, File p2)
						{
							// TODO: Implement this method
							return p1.getName().compareToIgnoreCase(p2.getName());
						}
					});
				int k=0;
				dst[k++]=new File("…");
				for(File f:src)if(f.isDirectory())dst[k++]=f;
				for(File f:src)if(f.isFile())dst[k++]=f;
				k=0;
				for(File ffs:dst)
				{
					final int p3=k++;
					Button tb=new Button(bs*2,0,bs*6,bs,ffs.getName(),new Button.Event(){
							@Override
							public void e(Button b)
							{
								File f=dst[p3];
								if(f.isDirectory())
								{
									localFile=f;
									listfile(m);
								}
								else if(f.isFile())
								{
									if(m.parent==me[20]||m.parent==me[23])
									{
										String ss=f.getName();
										((Edit)((Menu)m.parent).views.get(0)).txt=ss.substring(0,ss.lastIndexOf("."));
									}
									else if(m==me[22])
									{
										try
										{
											VECfile v=VECfile.readFile(f.getAbsolutePath());
											if(v!=null)
											{
												vec=v;
												initPosition();
												builder.bgpath=vec.bgpath;
												builder.backgcolor=vec.backgcolor;
												m.show=false;
											}
										}
										catch (IOException e)
										{
											ByteArrayOutputStream o=new ByteArrayOutputStream();
											PrintWriter pw=new PrintWriter(o);
											e.printStackTrace(pw);pw.close();
											toast("读取错误:"+o.toString());}
										catch (IllegalStateException e)
										{toast("不是标准的VEC文件");}
									}
								}
								else
								{
									localFile=localFile.getParentFile();
									listfile(m);
								}
							}
						});
					if(ffs.isFile())tb.color=0xff10e0ff;
					else tb.color=0xffffe020;
					m.addView(tb);
				}
				m.measure();
			}
		};
		final Button.Event sv=new Button.Event(){
			@Override
			public void e(Button b)
			{
				for(int j=0;j<9;j++)
					if(b.parent==me[j])
					{
						Menu m=(Menu)b.parent;
						for(int i=0;i<m.views.size();i++)
						{
							MView sub=m.views.get(i);
							if(sub instanceof Button)((Button)sub).selected=false;
							if(sub==b)
							{
								b.selected=true;
								if(
									((tmpShape.hasFlag(Shape.TYPE.RECT)||tmpShape.hasFlag(Shape.TYPE.OVAL)||tmpShape.hasFlag(Shape.TYPE.CIRCLE)||tmpShape.hasFlag(Shape.TYPE.LINE))&&i<2)||
									((tmpShape.hasFlag(Shape.TYPE.ROUNDRECT))&&i<3)||
									((tmpShape.hasFlag(Shape.TYPE.ARC))&&i<4)||
									((tmpShape.hasFlag(Shape.TYPE.POINT)||tmpShape.hasFlag(Shape.TYPE.TEXT))&&i<1))tmpPoint=tmpShape.pts.get(i);
								else if(i==4&&tmpShape.hasFlag(Shape.TYPE.ARC))
								{
									if(tmpShape.hasFlag(Shape.SHAPEPAR.CENTER))tmpShape.setFlag(0,Shape.SHAPEPAR.CENTER);
									else tmpShape.setFlag(Shape.SHAPEPAR.CENTER,Shape.SHAPEPAR.CENTER);
								}
								else if(tmpShape.hasFlag(Shape.TYPE.TEXT)&&i!=3)
								{
									if(i==2)
									{
										final EditText edit=new EditText(ctx);
										edit.setText(tmpShape.txt);
										new AlertDialog.Builder(ctx)
											.setTitle("设置文本(不支持换行)")
											.setView(edit)
											.setPositiveButton("确定",new DialogInterface.OnClickListener(){
												@Override
												public void onClick(DialogInterface p1, int p2)
												{
													tmpShape.txt=edit.getText()+"";
												}
											})
											.setNegativeButton("取消",null)
											.show();
									}
									else if(i<9)tmpShape.setFlag(Shape.TEXT.DEFAULT_TYPE*(long)Math.pow(2,i-4),Shape.TEXT.ALL_TYPEFACE);
									else if(i>8)tmpShape.setFlag(Shape.TEXT.LEFT*(long)Math.pow(2,i-9),Shape.TEXT.ALL_ALIGN);
								}
								else if(tmpShape.hasFlag(Shape.TYPE.PATH)&&i<7)
								{
									if(i==0)pointIndex--;
									else if(i==1)pointIndex++;
									else if(i==2)tmpShape.pts.add(++pointIndex,new Point(cx,cy));
									else if(i==3&&tmpShape.pts.size()!=1)tmpShape.pts.remove(tmpPoint);
									else if(i==4)
									{
										Point po=tmpShape.pts.get(0);
										if(po.x==0)po.x=1;
										else po.x=0;
									}
									else if(i==5)MODE=3;
									if(pointIndex<1)pointIndex=1;
									if(pointIndex>tmpShape.pts.size()-1)pointIndex=tmpShape.pts.size()-1;
									if(pointIndex<tmpShape.pts.size()&&pointIndex>0)
										tmpPoint=tmpShape.pts.get(pointIndex);
									((Button)m.views.get(6)).txt=pointIndex+"";
								}
								else
								{
									if(!vec.shapes.contains(tmpShape))vec.addShape(tmpShape);
									pointIndex=0;
									tmpShape=null;
									tmpPoint=null;
									m.show=false;
								}
							}
						}
					}
				for(int j=9;j<me.length;j++)
					if(b.parent==me[j])
					{
						Menu m=(Menu)b.parent;
						for(int i=0;i<m.views.size();i++)
						{
							if(m.views.get(i)==b)
							{
								if(j==24)
								{
									if(i==7)
									{
										Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
										intent.addCategory(Intent.CATEGORY_OPENABLE);
										intent.setType("image/*");
										ctx.startActivityForResult(intent,3);
									}
									else if(i==8)
									{
										showMenu(me[18]);
										((ColorPicker)me[18].views.get(0)).setIColor(builder);
									}
									else if(i==9)
									{
										builder.setWidth(Integer.parseInt(((Edit)m.views.get(1)).txt));
										builder.setHeight(Integer.parseInt(((Edit)m.views.get(3)).txt));
										builder.setDp(((float)builder.getWidth())/Float.parseFloat(((Edit)m.views.get(5)).txt));
										vec=builder.build();
										initPosition();
									}
								}
								else if(j==13)
								{
									if(i>1&&i<5)tmpShape.setFlag(Shape.STROKE.BUTT*(long)Math.pow(2,i-2),Shape.STROKE.ALL_CAP);
									else if(i>4)tmpShape.setFlag(Shape.STROKE.ROUND_JOIN*(long)Math.pow(2,i-5),Shape.STROKE.ALL_JOIN);
								}
								else if(j==15)
								{
									if(i==0)
									{
										int index=vec.shapes.indexOf(tmpShape);
										if(index!=-1)
										{
											Shape sh=vec.shapes.get(index);
											vec.shapes.add(index,new Shape(sh));
										}
									}
									else if(i==1)
									{
										int index=vec.shapes.indexOf(tmpShape);
										if(index!=-1)
										{
											saveUndo();
											vec.shapes.remove(index);
											tmpShape=null;
											for(int jj=0;jj<9;jj++)me[jj].show=false;
											me[15].show=false;
										}
									}
									else if(i==2)MODE=7;
									else if(i==3&&tmpShape!=null)
										for(int ik=tmpShape.hasFlag(Shape.TYPE.PATH)?1:0;ik<tmpShape.pts.size();ik++)
										{
											Point pp=tmpShape.pts.get(ik);
											pp.x=(int)(vec.width/vec.dp)-pp.x;
											//pp.y=(int)(vec.width/vec.dp)-pp.y;
										}
									else if(i==4)
									{
										int index=vec.shapes.indexOf(tmpShape);
										if(index!=-1)
										{
											Shape sh=vec.shapes.remove(index);
											if(--index<0)index=0;
											vec.shapes.add(index,sh);
										}
									}
									else if(i==5)
									{
										int index=vec.shapes.indexOf(tmpShape);
										if(index!=-1)
										{
											Shape sh=vec.shapes.remove(index);
											if(++index>vec.shapes.size())index=vec.shapes.size();
											vec.shapes.add(index,sh);
										}
									}
								}
								else if(j==25)
								{
									if(i==11)
									{
										Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
										intent.addCategory(Intent.CATEGORY_OPENABLE);
										intent.setType("image/*");
										ctx.startActivityForResult(intent,3);
									}
									else if(i==12)
									{
										showMenu(me[18]);
										((ColorPicker)me[18].views.get(0)).setIColor(builder);
									}
									else if(i==17)
									{
										try
										{
											vec.name=((Edit)m.views.get(1)).txt;
											vec.comm=((Edit)m.views.get(3)).txt;
											vec.antialias=((SeekBar)m.views.get(14)).getProgress()==0?false:true;
											vec.dither=((SeekBar)m.views.get(16)).getProgress()==0?false:true;
											vec.backgcolor=builder.backgcolor;
											vec.bgpath=builder.bgpath;
											int w=Integer.parseInt(((Edit)m.views.get(5)).txt);
											vec.init(w,Integer.parseInt(((Edit)m.views.get(7)).txt),(float)w/Float.parseFloat(((Edit)m.views.get(9)).txt));
											initPosition();
										}
										catch(Throwable e)
										{}
									}	
								}
								else if(j==9)
								{
									if(i==0)
									{
										showMenu(me[18]);
										((ColorPicker)me[18].views.get(0)).setIColor(curColorView);
									}
									else if(i==1)MODE=1;
								}
								else if(j==10)colorShape.setFlag(Shape.STYLE.FILL*(long)Math.pow(2,i),Shape.STYLE.ALL);
								else if(j==12&&i==0)MODE=5;
								else if(j==20&&i==1)
								{
									String f=localFile.getAbsolutePath()+"/"+((Edit)((Menu)b.parent).views.get(0)).txt+".vec";
									/*if(new File(f).exists())
									 {
									 me[20].showMenu;
									 ((Button)me[20].views.get(1)).txt="发现同名文件，是否替换？";
									 }
									 else*/
									vec.saveFile(f);
									me[20].show=false;
								}
								else if(j==23&&i==1)
								{
									String f=localFile.getAbsolutePath()+"/"+((Edit)((Menu)b.parent).views.get(0)).txt+".png";
									/*if(new File(f).exists())
									 {
									 me[20].showMenu;
									 ((Button)me[20].views.get(1)).txt="发现同名文件，是否替换？";
									 }
									 else*/
									vec.loadoutFile(f);
									me[23].show=false;
								}
								setTmpShape();
							}
						}
					}
			}
		};
		final FloatPicker.FloatPickerEvent fpe=new FloatPicker.FloatPickerEvent(){
			@Override
			public void onChange(FloatPicker b,float f)
			{
				for(int j=0;j<me.length;j++)
					if(b.parent==me[j])
					{
						Menu m=(Menu)b.parent;
						for(int i=0;i<m.views.size();i++)
						{
							if(m.views.get(i)==b)
							{
								if(j==8&&i==1&&tmpShape.hasFlag(Shape.TYPE.TEXT))
								{
									tmpShape.pts.get(1).x=(int)(f*100);
								}
								else if(j==5&&i==8&&tmpShape.hasFlag(Shape.TYPE.PATH))
								{
									tmpShape.pts.get(0).y=(int)(f*100);
								}
								else if(j==12)
								{
									colorShape.par[6]=(int)(f*100);
									setTmpShape();
								}
								else if(j==13)
								{
									if(i==0)colorShape.par[3]=(int)(f*100);
									else if(i==1)colorShape.par[2]=(int)(f*100);
									setTmpShape();
								}
							}
						}
					}
			}
		};
		addView(
			new Button(0,0,bs,bs,"新建",ev),
			new Button(bs,0,bs,bs,"打开",ev),
			new Button(bs*2,0,bs,bs,"保存",ev),
			new Button(bs*3,0,bs,bs,"导出",ev),
			new Button(bs*5,0,bs,bs,"参数",ev),
			new Button(bs*4,0,bs,bs,"形状表",ev),
			new Button(bs*6,0,bs,bs,"网格",ev),
			new Button(bs*7,0,bs,bs,"撤销",ev),
			new Button(bs*8,0,bs,bs,"恢复",ev),
			new Button(0,bs*2,bs,bs,"矩形",ev),
			new Button(0,bs*3,bs,bs,"圆",ev),
			new Button(0,bs*4,bs,bs,"椭圆",ev),
			new Button(0,bs*5,bs,bs,"弧",ev),
			new Button(0,bs*6,bs,bs,"圆角矩形",ev),
			new Button(0,bs*7,bs,bs,"路径",ev),
			new Button(0,bs*8,bs,bs,"点",ev),
			new Button(0,bs*9,bs,bs,"直线",ev),
			new Button(0,bs*10,bs,bs,"文本",ev),
			new Button(bs*8,bs*2,bs,bs,"颜色",ev),
			new Button(bs*8,bs*3,bs,bs,"样式",ev),
			new Button(bs*8,bs*4,bs,bs,"XFERMODE",ev),
			new Button(bs*8,bs*5,bs,bs,"阴影",ev),
			new Button(bs*8,bs*6,bs,bs,"线样式",ev),
			new Button(bs*8,bs*7,bs,bs,"着色器",ev),
			new Button(bs*8,bs*8,bs,bs,"形状设置",ev),
			new Button(bs*8,bs*9,bs,bs,"线特效",ev),
			new Button(bs*8,bs*10,bs,bs,"笔触",ev));
		addView(
			new Menu(bs,bs*14,bs*3,bs*1,
					 new Button(bs,bs*14,bs,bs,"点1",sv),
					 new Button(bs*2,bs*14,bs,bs,"点2",sv),
					 new Button(bs*3,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*14,bs*3,bs*1,
					 new Button(bs,bs*14,bs,bs,"圆心",sv),
					 new Button(bs*2,bs*14,bs,bs,"半径",sv),
					 new Button(bs*3,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*14,bs*3,bs*1,
					 new Button(bs,bs*14,bs,bs,"点1",sv),
					 new Button(bs*2,bs*14,bs,bs,"点2",sv),
					 new Button(bs*3,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*14,bs*6,bs*1,
					 new Button(bs,bs*14,bs,bs,"左上",sv),
					 new Button(bs*2,bs*14,bs,bs,"右下",sv),
					 new Button(bs*3,bs*14,bs,bs,"开始",sv),
					 new Button(bs*4,bs*14,bs,bs,"结束",sv),
					 new Button(bs*5,bs*14,bs,bs,"中心",sv),
					 new Button(bs*6,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*14,bs*4,bs*1,
					 new Button(bs,bs*14,bs,bs,"左上",sv),
					 new Button(bs*2,bs*14,bs,bs,"右下",sv),
					 new Button(bs*3,bs*14,bs,bs,"圆角",sv),
					 new Button(bs*4,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*11,bs*6,bs*4,
					 new Button(bs,bs*11,bs,bs,"上个",sv),
					 new Button(bs*2,bs*11,bs,bs,"下个",sv),
					 new Button(bs,bs*12,bs,bs,"添加",sv),
					 new Button(bs*2,bs*12,bs,bs,"删除",sv),
					 new Button(bs,bs*13,bs,bs,"封闭",sv),
					 new Button(bs*2,bs*13,bs,bs,"自由选点",sv),
					 new Button(bs*2,bs*14,bs,bs,"",sv),
					 new Button(bs,bs*14,bs,bs,"确定",sv),
					 new FloatPicker(bs*3,bs*11,bs*4,fpe)
					 ),
			new Menu(bs,bs*14,bs*2,bs*1,
					 new Button(bs,bs*14,bs,bs,"点",sv),
					 new Button(bs*2,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*14,bs*3,bs*1,
					 new Button(bs,bs*14,bs,bs,"点1",sv),
					 new Button(bs*2,bs*14,bs,bs,"点2",sv),
					 new Button(bs*3,bs*14,bs,bs,"确定",sv)),
			new Menu(bs,bs*11,bs*7,bs*4,
					 new Button(bs,bs*11,bs,bs,"点",sv),
					 new FloatPicker(bs*4,bs*11,bs*4,fpe),
					 new Button(bs*2,bs*11,bs,bs,"文本",sv),
					 new Button(bs*3,bs*11,bs,bs,"确定",sv),
					 new Button(bs,bs*12,bs,bs,"默认",sv),
					 new Button(bs*2,bs*12,bs,bs,"加粗",sv),
					 new Button(bs*3,bs*12,bs,bs,"等宽",sv),
					 new Button(bs,bs*13,bs,bs,"sans",sv),
					 new Button(bs*2,bs*13,bs,bs,"serif",sv),
					 new Button(bs,bs*14,bs,bs,"左对齐",sv),
					 new Button(bs*2,bs*14,bs,bs,"居中",sv),
					 new Button(bs*3,bs*14,bs,bs,"右对齐",sv)),
			new Menu(bs,bs*14,bs*2,bs*1,
					 new Button(bs,bs*14,bs,bs,"调色",sv),
					 new Button(bs*2,bs*14,bs,bs,"取色",sv)),
			new Menu(bs,bs*14,bs*4,bs*1,
					 new Button(bs,bs*14,bs,bs,"填充",sv),
					 new Button(bs*2,bs*14,bs,bs,"描线",sv),
					 new Button(bs*3,bs*14,bs,bs,"描线填充",sv),
					 new Button(bs*4,bs*14,bs,bs,"填充描线",sv)),
			new Menu(bs,bs*11,bs*7,bs*4),
			new Menu(bs,bs*11,bs*5,bs*4,
					 new Button(bs,bs*14,bs,bs,"偏移",sv),
					 new FloatPicker(bs*2,bs*11,bs*4,fpe)),
			new Menu(bs,bs*11,bs*7,bs*4,
					 new FloatPicker(bs,bs*11,bs*3,fpe),
					 new FloatPicker(bs*5,bs*11,bs*3,fpe),
					 new Button(bs,bs*14,bs,bs,"无",sv),
					 new Button(bs*2,bs*14,bs,bs,"圆",sv),
					 new Button(bs*3,bs*14,bs,bs,"方",sv),
					 new Button(bs*5,bs*14,bs,bs,"圆角",sv),
					 new Button(bs*6,bs*14,bs,bs,"锐角",sv),
					 new Button(bs*7,bs*14,bs,bs,"直线",sv)),
			new Menu(bs,bs*11,bs*7,bs*4),
			new Menu(bs,bs*12,bs*2,bs*3,
					 new Button(bs,bs*12,bs,bs,"复制",sv),
					 new Button(bs*2,bs*12,bs,bs,"删除",sv),
					 new Button(bs,bs*13,bs,bs,"平移",sv),
					 new Button(bs*2,bs*13,bs,bs,"镜像",sv),
					 new Button(bs,bs*14,bs,bs,"上层",sv),
					 new Button(bs*2,bs*14,bs,bs,"下层",sv)
					 ),
			new Menu(bs,bs*11,bs*7,bs*4),
			new Menu(bs,bs*11,bs*7,bs*4),
			new Menu(bs,bs*3,bs*7,bs*7,
					 new ColorPicker(bs,bs*3,bs*7,bs*3),
					 new SeekBar(bs,bs*6,bs*7,bs,0,255),
					 new SeekBar(bs,bs*7,bs*7,bs,0,255),
					 new SeekBar(bs,bs*8,bs*7,bs,0,255),
					 new SeekBar(bs,bs*9,bs*7,bs,0,255)),
			new List(bs,bs*3,bs*7,bs*9),//shape
			new Menu(bs,bs*3,bs*7,bs*9,
					 new Edit(bs,bs*3,bs*6,bs,""),
					 new Button(bs*7,bs*3,bs,bs,"保存",sv),
					 new List(bs,bs*4,bs*7,bs*8)),
			new Menu(bs*2,bs*5,bs*5,bs*4,
					 new Button(bs*2,bs*5,bs*5,bs,"提示",null),
					 new Button(bs*2,bs*6,bs*5,bs*2,"",null),
					 new Button(bs*2,bs*8,bs*2,bs,"确定",null),
					 new Button(bs*5,bs*8,bs*2,bs,"取消",null)),
			new List(bs,bs*3,bs*7,bs*9),
			new Menu(bs,bs*3,bs*7,bs*9,
					 new Edit(bs,bs*3,bs*6,bs,""),
					 new Button(bs*7,bs*3,bs,bs,"导出",sv),
					 new List(bs,bs*4,bs*7,bs*8)),
			new Menu(bs*3,bs*5,bs*3,bs*5,
					 new Button(bs*3,bs*5,bs,bs,"宽",null),
					 new Edit(bs*4,bs*5,bs*2,bs,""),
					 new Button(bs*3,bs*6,bs,bs,"高",null),
					 new Edit(bs*4,bs*6,bs*2,bs,""),
					 new Button(bs*3,bs*7,bs,bs,"精度",null),
					 new Edit(bs*4,bs*7,bs*2,bs,""),
					 new Button(bs*3,bs*8,bs,bs,"背景色",null),
					 new Button(bs*4,bs*8,bs,bs,"图片",sv),
					 new Button(bs*5,bs*8,bs,bs,"调色",sv),
					 new Button(bs*3,bs*9,bs*3,bs,"确定",sv)),
			new Menu(bs*2,bs*4,bs*5,bs*8,
					 new Button(bs*2,bs*4,bs,bs,"名称",null),
					 new Edit(bs*3,bs*4,bs*4,bs,""),
					 new Button(bs*2,bs*5,bs,bs,"描述",null),
					 new Edit(bs*3,bs*5,bs*4,bs,""),
					 new Button(bs*2,bs*6,bs,bs,"宽",null),
					 new Edit(bs*3,bs*6,bs*4,bs,""),
					 new Button(bs*2,bs*7,bs,bs,"高",null),
					 new Edit(bs*3,bs*7,bs*4,bs,""),
					 new Button(bs*2,bs*8,bs,bs,"精度",null),
					 new Edit(bs*3,bs*8,bs*4,bs,""),
					 new Button(bs*2,bs*9,bs,bs,"背景色",null),
					 new Button(bs*3,bs*9,bs*2,bs,"图片",sv),
					 new Button(bs*5,bs*9,bs*2,bs,"调色",sv),
					 new Button(bs*2,bs*10,bs,bs,"抗锯齿",null),
					 new SeekBar(bs*3,bs*10,bs,bs,vec.antialias?1:0,1) ,
					 new Button(bs*5,bs*10,bs,bs,"防抖动",null),
					 new SeekBar(bs*6,bs*10,bs,bs,vec.dither?1:0,1) ,
					 new Button(bs*2,bs*11,bs*5,bs,"确定",sv))
		);
		final int[] color=new int[]{0xffff5555,0xff55ff55,0xff5555ff,0xffffff55,0xff55ffff,0xffff55ff,0xff888888,0xffffffff,0xff000000};
		final ColorView[] cbt=new ColorView[9];
		ColorView.Event ev2=new ColorView.Event(){
			@Override public void e(ColorView b,int p)
			{
				curColorView=b;
				if(me[12].show)colorShape.par[7]=b.color;
				else if(p==0)colorShape.par[0]=b.color;
				else if(p==1)colorShape.par[1]=b.color;
				for(ColorView bb:cbt)
					if(bb==b)bb.selected=true;
					else bb.selected=false;
				setTmpShape();
			}
		};
		for(int i=0;i<9;i++)
		{
			cbt[i]=new ColorView(bs*i,bs*15,bs,bs,ev2);
			cbt[i].color=color[i];
		}
		curColorView=cbt[0];
		curColorView.selected=true;
		addView(cbt);
		for(int i=0;i<bu.length;i++)bu[i]=(Button)mview.get(i);
		for(int i=0;i<me.length;i++)me[i]=(Menu)mview.get(i+27);
		ColorPicker pi=(ColorPicker)me[18].views.get(0);
		for(int i=1;i<5;i++)
		{
			pi.argb[i-1]=(SeekBar)me[18].views.get(i);
			pi.argb[i-1].e=pi;
		}
		addView(toast=new Toast(0,bs*16,bs*9,bs));
	}
	public void toast(String s)
	{
		if(toast!=null)toast.show(s);
		mview.remove(toast);
		mview.add(toast);
	}
	public void addView(MView... b)
	{
		for(MView bb:b)if(bb!=null)mview.add(bb);
	}
	public void showMenu(Menu m)
	{
		m.show=!m.show;
		/*if(m.show)
		{
			mview.remove(m);
			mview.add(m);
		}*/
	}
	public void saveUndo()
	{
		redo.clear();
		CopyOnWriteArrayList<Shape> list=new CopyOnWriteArrayList<Shape>();
		for(Shape s:vec.shapes)list.add(new Shape(s));
		undo.add(list);
	}
	public void setTmpShape()
	{
		if(tmpShape!=null)tmpShape.set(colorShape);
	}
	public void setInfo(Object... o)
	{
		info.delete(0,info.length());
		for(Object c:o)info.append(c);
	}
	@Override
	public CharSequence getTextBeforeCursor(int p1, int p2)
	{
		return null;
	}
	@Override
	public CharSequence getTextAfterCursor(int p1, int p2)
	{
		return null;
	}
	@Override
	public CharSequence getSelectedText(int p1)
	{
		return null;
	}
	@Override
	public int getCursorCapsMode(int p1)
	{
		return 0;
	}
	@Override
	public ExtractedText getExtractedText(ExtractedTextRequest p1, int p2)
	{
		return null;
	}
	@Override
	public boolean deleteSurroundingText(int p1, int p2)
	{
		return false;
	}
	@Override
	public boolean setComposingText(CharSequence p1, int p2)
	{
		return false;
	}
	@Override
	public boolean setComposingRegion(int p1, int p2)
	{
		return false;
	}
	@Override
	public boolean finishComposingText()
	{
		return false;
	}
	@Override
	public boolean commitText(CharSequence p1, int p2)
	{
		if(curView!=null&&curView instanceof Edit)((Edit)curView).txt+=p1.toString();
		return false;
	}
	@Override
	public boolean commitCompletion(CompletionInfo p1)
	{
		return false;
	}
	@Override
	public boolean commitCorrection(CorrectionInfo p1)
	{
		return false;
	}
	@Override
	public boolean setSelection(int p1, int p2)
	{
		return false;
	}
	@Override
	public boolean performEditorAction(int p1)
	{
		return false;
	}
	@Override
	public boolean performContextMenuAction(int p1)
	{
		return false;
	}
	@Override
	public boolean beginBatchEdit()
	{
		return false;
	}
	@Override
	public boolean endBatchEdit()
	{
		return false;
	}
	@Override
	public boolean sendKeyEvent(KeyEvent event)
	{
		try
		{
			if (event.getAction() == KeyEvent.ACTION_DOWN&&curView!=null&&curView instanceof Edit)
			{
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL)
				{
					String t=((Edit)curView).txt;
					if(t.length()>0)t=t.substring(0,t.length()-1);
					((Edit)curView).txt=t;
				}
			}
			return true;
		}
		catch(Throwable e)
		{
		}
		return false;
	}
	@Override
	public boolean clearMetaKeyStates(int p1)
	{
		return false;
	}
	@Override
	public boolean reportFullscreenMode(boolean p1)
	{
		return false;
	}
	@Override
	public boolean performPrivateCommand(String p1, Bundle p2)
	{
		return false;
	}
	@Override
	public boolean requestCursorUpdates(int p1)
	{
		return false;
	}
	public void showIME()
	{
		surface.setFocusableInTouchMode(true);
		InputMethodManager imm = (InputMethodManager)ctx
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(surface,InputMethodManager.SHOW_IMPLICIT);

	}
}
