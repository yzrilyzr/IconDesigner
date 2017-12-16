package com.yzrilyzr.icondesigner;

import android.graphics.*;

import java.util.ArrayList;

public class Shape
{
	public ArrayList<Point> pts=new ArrayList<Point>();
	public int[] par=new int[]{
		0xffff0000,
		0xff000000,
		0,
		100,
		0,
		0,
		0,
		0xff000000
	};
	//0:color,1:strokecolor,2:miter,3:strokewidth,4:shadowD.x,5:.y,6:shadowR,7:shadowcolor
	public long flag=0;
	public Shader shader=null;
	public PathEffect pathEffect=null;
	public String txt="";
	public static final class TEXT
	{
		public static final long
		LEFT=			0x1l,
		CENTER=			0x2l,
		RIGHT=			0x4l,
		DEFAULT_TYPE=	0x8l,
		BOLD_TYPE=		0x10l,
		MONOSPACE=		0x20l,
		SANS_SERIF=		0x40l,
		SERIF=			0x80l,
		ALL_ALIGN=LEFT+CENTER+RIGHT,
		ALL_TYPEFACE=DEFAULT_TYPE+BOLD_TYPE+MONOSPACE+SANS_SERIF+SERIF;
	}
	public static final class STROKE
	{
		public static final long
		BUTT=		0x100l,
		ROUND_CAP=	0x200l,
		SQUARE=		0x400l,
		ROUND_JOIN=	0x800l,
		MITER=		0x1000l,
		BEVEL=		0x2000l,
		ALL_CAP=BUTT+ROUND_CAP+SQUARE,
		ALL_JOIN=ROUND_JOIN+MITER+BEVEL;
	}
	public static final class STYLE
	{
		public static final long
		FILL=	0x4000l,
		STROKE=	0x8000l,
		FS=		0x10000l,
		SF=		0x20000l,
		ALL=FILL+STROKE+FS+SF;
	}
	public static final class XFERMODE
	{
		public static final long
		CLEAR=			0x40000l,
		DARKEN=			0x80000l,
		DST=			0x100000l,
		DST_ATOP=		0x200000l,
		DST_IN=			0x400000l,
		DST_OUT=		0x800000l,
		DST_OVER=		0x1000000l,
		LIGHTEN=		0x2000000l,
		MULTIPLY=		0x4000000l,
		OVERLAY=		0x8000000l,
		SCREEN=			0x10000000l,
		SRC=			0x20000000l,
		SRC_ATOP=		0x40000000l,
		SRC_IN=			0x80000000l,
		SRC_OUT=		0x100000000l,
		SRC_OVER=		0x200000000l,
		XOR=			0x400000000l,
		ADD=			0x800000000l,
		ALL=CLEAR+
		DARKEN+
		DST+
		DST_ATOP+
		DST_IN+
		DST_OUT+
		DST_OVER+
		LIGHTEN+
		MULTIPLY+
		OVERLAY+
		SCREEN+
		SRC+
		SRC_ATOP+
		SRC_IN+
		SRC_OUT+
		SRC_OVER+
		XOR+
		ADD;
	}
	public static final class SHAPEPAR
	{
		public static final long
		NEWLAYER=		0x1000000000l,
		RESTORELAYER=	0x2000000000l,
		CENTER=			0x4000000000l,
		CLOSE=			0x8000000000l;
	}
	public static final class SHADER
	{
		public static final long
		SWEEP=	0x10000000000l,
		RADIAL=	0x20000000000l,
		LINEAR=	0x40000000000l,
		ALL=SWEEP+RADIAL+LINEAR;
	}
	public static final class PATHEFFECT
	{
		public static final long
		DASH=		0x80000000000l,
		DISCRETE=	0x100000000000l,
		ROUND=		0x200000000000l,
		COMPOSE=	0x400000000000l,
		ALL=DASH+DISCRETE+ROUND+COMPOSE;
	}
	public static final class TYPE
	{
		public static final long
		RECT=		0x800000000000l,
		CIRCLE=		0x1000000000000l,
		OVAL=		0x2000000000000l,
		ARC=		0x4000000000000l,
		ROUNDRECT=	0x8000000000000l,
		PATH=		0x10000000000000l,
		POINT=		0x20000000000000l,
		LINE=		0x40000000000000l,
		TEXT=		0x80000000000000l,
		ALL=
		RECT+
		CIRCLE+
		OVAL+
		ARC+
		ROUNDRECT+
		PATH+
		POINT+
		LINE+
		TEXT;
	}

	public boolean hasFlag(long f)
	{
		return (flag&f)==f;
	}
	public void setFlag(Shape src,long all)
	{
		long sr=src.flag;
		flag=(flag|all)-(sr|all)+sr;
	}
	public void setFlag(long f,long all)
	{
		flag=(flag|all)-all+f;
	}
	public void onDraw(Canvas c,boolean antia,boolean dither,float xx,float yy,float sc,float dp,Paint sp)
	{
		sp.reset();
		sp.setAntiAlias(antia);
		sp.setDither(dither);
		Paint.Cap cp=Paint.Cap.BUTT;
		if(hasFlag(STROKE.BUTT))cp=Paint.Cap.BUTT;
		else if(hasFlag(STROKE.SQUARE))cp=Paint.Cap.SQUARE;
		else if(hasFlag(STROKE.ROUND_CAP))cp=Paint.Cap.ROUND;
		sp.setStrokeCap(cp);

		Paint.Join jn=Paint.Join.MITER;
		if(hasFlag(STROKE.BEVEL))jn=Paint.Join.BEVEL;
		else if(hasFlag(STROKE.MITER))jn=Paint.Join.MITER;
		else if(hasFlag(STROKE.ROUND_JOIN))jn=Paint.Join.ROUND;
		sp.setStrokeJoin(jn);

		sp.setStrokeMiter((float)par[2]/100f*dp*sc);
		sp.setStrokeWidth((float)par[3]/100f*dp*sc);
		sp.setPathEffect(pathEffect);
		sp.setShader(shader);
		sp.setShadowLayer((float)par[6]/100f,(par[4]*dp+xx)*sc,(par[5]*dp+yy)*sc,par[7]);

		if(hasFlag(SHAPEPAR.NEWLAYER))c.save();
		PorterDuff.Mode xm=null;
		if(hasFlag(XFERMODE.ADD))xm=PorterDuff.Mode.ADD;
		else if(hasFlag(XFERMODE.CLEAR))CLEAR:xm=PorterDuff.Mode.CLEAR;
		else if(hasFlag(XFERMODE.DARKEN))DARKEN:xm=PorterDuff.Mode.DARKEN;
		else if(hasFlag(XFERMODE.DST))DST:xm=PorterDuff.Mode.DST;
		else if(hasFlag(XFERMODE.DST_ATOP))DST_ATOP:xm=PorterDuff.Mode.DST_ATOP;
		else if(hasFlag(XFERMODE.DST_IN))DST_IN:xm=PorterDuff.Mode.DST_IN;
		else if(hasFlag(XFERMODE.DST_OUT))DST_OUT:xm=PorterDuff.Mode.DST_OUT;
		else if(hasFlag(XFERMODE.DST_OVER))DST_OVER:xm=PorterDuff.Mode.DST_OVER;
		else if(hasFlag(XFERMODE.LIGHTEN))LIGHTEN:xm=PorterDuff.Mode.LIGHTEN;
		else if(hasFlag(XFERMODE.MULTIPLY))MULTIPLY:xm=PorterDuff.Mode.MULTIPLY;
		else if(hasFlag(XFERMODE.OVERLAY))OVERLAY:xm=PorterDuff.Mode.OVERLAY;
		else if(hasFlag(XFERMODE.SCREEN))SCREEN:xm=PorterDuff.Mode.SCREEN;
		else if(hasFlag(XFERMODE.SRC))SRC:xm=PorterDuff.Mode.SRC;
		else if(hasFlag(XFERMODE.SRC_ATOP))SRC_ATOP:xm=PorterDuff.Mode.SRC_ATOP;
		else if(hasFlag(XFERMODE.SRC_IN))SRC_IN:xm=PorterDuff.Mode.SRC_IN;
		else if(hasFlag(XFERMODE.SRC_OUT))SRC_OUT:xm=PorterDuff.Mode.SRC_OUT;
		else if(hasFlag(XFERMODE.SRC_OVER))SRC_OVER:xm=PorterDuff.Mode.SRC_OVER;
		else if(hasFlag(XFERMODE.XOR))XOR:xm=PorterDuff.Mode.XOR;
		if(xm!=null)sp.setXfermode(new PorterDuffXfermode(xm));
		else sp.setXfermode(null);
		if(hasFlag(TYPE.RECT))
		{
			Point p1=pts.get(0);
			Point p2=pts.get(1);
			RectF re=new RectF((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,(p2.x*dp+xx)*sc,(p2.y*dp+yy)*sc);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawRect(re,sp);
				fs(sp);
			}
			c.drawRect(re,sp);
		}
		else if(hasFlag(TYPE.CIRCLE))
		{
			Point p1=pts.get(0);
			Point p2=pts.get(1);
			float r=(float)Math.sqrt(Math.pow(p2.x-p1.x,2)+Math.pow(p2.y-p1.y,2));
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawCircle((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,r*dp*sc,sp);
				fs(sp);
			}
			c.drawCircle((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,r*dp*sc,sp);
		}
		else if(hasFlag(TYPE.OVAL))
		{
			Point p1=pts.get(0);
			Point p2=pts.get(1);
			RectF re=new RectF((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,(p2.x*dp+xx)*sc,(p2.y*dp+yy)*sc);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawOval(re,sp);
				fs(sp);
			}
			c.drawOval(re,sp);
		}
		else if(hasFlag(TYPE.ARC))
		{
			Point p1=pts.get(0);
			Point p2=pts.get(1);
			Point p3=pts.get(2);
			Point p4=pts.get(3);
			float cx=(p1.x+p2.x)/2;
			float cy=(p1.y+p2.y)/2;
			float r1=(float)Math.sqrt(Math.pow((p3.x-cx),2)+Math.pow((p3.y-cy),2));
			float r2=(float)Math.sqrt(Math.pow((p4.x-cx),2)+Math.pow((p4.y-cy),2));
			float start=(float)(Math.asin((p3.y-cy)/r1)*180f/Math.PI);
			float end=(float)(Math.asin((p4.y-cy)/r2)*180f/Math.PI);
			if(p3.x-cx<0)start=180f-start;
			if(p3.x-cx>0&&p3.y-cy<0)start+=360f;
			if(p4.x-cx<0)end=180f-end;
			if(p4.x-cx>0&&p4.y-cy<0)end+=360f;
			if(start>end)end+=360f;
			RectF re=new RectF((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,(p2.x*dp+xx)*sc,(p2.y*dp+yy)*sc);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawArc(re,start,end-start,hasFlag(SHAPEPAR.CENTER),sp);
				fs(sp);
			}
			c.drawArc(re,start,end-start,hasFlag(SHAPEPAR.CENTER),sp);
		}
		else if(hasFlag(TYPE.ROUNDRECT))
		{
			Point p1=pts.get(0);
			Point p2=pts.get(1);
			Point p3=pts.get(2);
			float r1=p3.x-p1.x;
			float r2=p3.y-p1.y;
			RectF re=new RectF((p1.x*dp+xx)*sc,(p1.y*dp+yy)*sc,(p2.x*dp+xx)*sc,(p2.y*dp+yy)*sc);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawRoundRect(re,r1*dp*sc,r2*dp*sc,sp);
				fs(sp);
			}
			c.drawRoundRect(re,r1*dp*sc,r2*dp*sc,sp);
		}
		else if(hasFlag(TYPE.POINT))
		{
			Point r=pts.get(0);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawPoint((r.x*dp+xx)*sc,(r.y*dp+yy)*sc,sp);
				fs(sp);
			}
			c.drawPoint((r.x*dp+xx)*sc,(r.y*dp+yy)*sc,sp);
		}
		else if(hasFlag(TYPE.LINE))
		{
			Point r1=pts.get(0),r2=pts.get(1);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawLine((r1.x*dp+xx)*sc,(r1.y*dp+yy)*sc,(r2.x*dp+xx)*sc,(r2.y*dp+yy)*sc,sp);
				fs(sp);
			}
			c.drawLine((r1.x*dp+xx)*sc,(r1.y*dp+yy)*sc,(r2.x*dp+xx)*sc,(r2.y*dp+yy)*sc,sp);
		}
		else if(hasFlag(TYPE.PATH))
		{
			Paint tp=new Paint();
			tp.setTextSize(16);
			Path pa=new Path();
			Point a=pts.get(0);
			if(a.y!=0)
			{
				if(pts.size()>1)
				{
					ArrayList<PointF> poi=new ArrayList<PointF>();
					Point t=pts.get(1),t2=null;
					pa.moveTo((t.x*dp+xx)*sc,(t.y*dp+yy)*sc);
					for(int i=1;i<pts.size();i++)
					{
						t=pts.get(i);
						t2=null;
						if(i+1<pts.size())t2=pts.get(i+1);
						poi.add(new PointF((t.x*dp+xx)*sc,(t.y*dp+yy)*sc));
						if(MainView.render.tmpShape==this&&i!=0&&i!=pts.size())
						{
							if(MainView.render.tmpPoint==t)tp.setColor(0xffff0000);
							else tp.setColor(0xff000000);
							c.drawText(i+"",(t.x*dp+xx)*sc,(t.y*dp+yy)*sc,tp);
						}
						if(t2!=null&&t.equals(t2.x,t2.y))
						{
							if(poi.size()>0)
							{
								poi.add(0,poi.get(0));
								poi.add(poi.get(poi.size()-1));
							}
							Catmull_Rom(poi,a.y,pa);
							poi.clear();
						}
					}
					if(poi.size()>0)
					{
						poi.add(0,poi.get(0));
						poi.add(poi.get(poi.size()-1));
					}
					Catmull_Rom(poi,a.y,pa);
					poi.clear();poi=null;
				}
			}
			else
				for(int i=1;i<pts.size();i++)
				{
					Point t=pts.get(i);
					if(i==1)pa.moveTo((t.x*dp+xx)*sc,(t.y*dp+yy)*sc);
					else pa.lineTo((t.x*dp+xx)*sc,(t.y*dp+yy)*sc);
					if(MainView.render.tmpShape==this)
					{
						if(MainView.render.tmpPoint==t)tp.setColor(0xffff0000);
						else tp.setColor(0xff000000);
						c.drawText(i+"",(t.x*dp+xx)*sc,(t.y*dp+yy)*sc,tp);
					}
				}
			if(a.x==1)pa.close();
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawPath(pa,sp);
				fs(sp);
			}
			c.drawPath(pa,sp);
		}
		else if(hasFlag(TYPE.TEXT))
		{
			Point r=pts.get(0);
			Point t=pts.get(1);
			sp.setTextSize(t.x*dp*sc/100f);
			Typeface typefac=Typeface.DEFAULT;
			if(hasFlag(TEXT.DEFAULT_TYPE))typefac=Typeface.DEFAULT;
			else if(hasFlag(TEXT.BOLD_TYPE))typefac=Typeface.DEFAULT_BOLD;
			else if(hasFlag(TEXT.MONOSPACE))typefac=Typeface.MONOSPACE;
			else if(hasFlag(TEXT.SANS_SERIF))typefac=Typeface.SANS_SERIF;
			else if(hasFlag(TEXT.SERIF))typefac=Typeface.SERIF;
			sp.setTypeface(typefac);
			Paint.Align align=Paint.Align.LEFT;
			if(hasFlag(TEXT.LEFT))align=Paint.Align.LEFT;
			else if(hasFlag(TEXT.RIGHT))align=Paint.Align.RIGHT;
			else if(hasFlag(TEXT.CENTER))align=Paint.Align.CENTER;
			sp.setTextAlign(align);
			if(st())fill(sp);
			else
			{
				sf(sp);
				c.drawText(txt,(r.x*dp+xx)*sc,(r.y*dp+yy)*sc,sp);
				fs(sp);
			}
			c.drawText(txt,(r.x*dp+xx)*sc,(r.y*dp+yy)*sc,sp);
		}
		if(hasFlag(SHAPEPAR.RESTORELAYER))c.restore();
	}
	private boolean st()
	{
		return hasFlag(STYLE.FILL)||hasFlag(STYLE.STROKE);
	}
	private void fill(Paint sp)
	{
		sp.setColor(hasFlag(STYLE.FILL)?par[0]:par[1]);
		sp.setStyle(hasFlag(STYLE.FILL)?Paint.Style.FILL:Paint.Style.STROKE);
	}

	private void sf(Paint sp)
	{
		sp.setColor(hasFlag(STYLE.SF)?par[0]:par[1]);
		sp.setStyle(hasFlag(STYLE.SF)?Paint.Style.FILL:Paint.Style.STROKE);
	}

	private void fs(Paint sp)
	{
		sp.setColor(hasFlag(STYLE.FS)?par[0]:par[1]);
		sp.setStyle(hasFlag(STYLE.FS)?Paint.Style.FILL:Paint.Style.STROKE);
	}
	public Shape(long flag)
	{
		this.flag=flag;
		int len=0;
		if(hasFlag(TYPE.RECT)||hasFlag(TYPE.CIRCLE)||hasFlag(TYPE.OVAL)||hasFlag(TYPE.LINE)||hasFlag(TYPE.TEXT))len=2;
		else if(hasFlag(TYPE.ROUNDRECT)||hasFlag(TYPE.ARC))len=4;
		else if(hasFlag(TYPE.PATH)||hasFlag(TYPE.POINT))len=1;
		for(int i=0;i<len;i++)
			pts.add(new Point(0,0));
	}
	public Shape(Shape s){
		this(s.flag);
		pts.clear();
		for(Point p:s.pts)
			pts.add(new Point(p));
		int k=0;
		for(int ii:s.par)
			par[k++]=ii;
		flag=s.flag;
		txt=s.txt;
	}
	public void set(Shape src)
	{
		//for(Point p:src.pts)pts.add(new Point(p.x,p.y));
		int i=0;
		for(int p:src.par)par[i++]=p;
		setFlag(src,STYLE.ALL);
	}
	public static void Catmull_Rom(ArrayList<PointF> point, int cha,Path path)
	{
		if (point.size()<4||cha==0||cha>10000)
		{
			return;
		}
		for (int index = 1; index < point.size() - 2; index++)
		{
			PointF p0 = point.get(index - 1);
			PointF p1 = point.get(index);
			PointF p2 = point.get(index + 1);
			PointF p3 = point.get(index + 2);
			for (int i = 1; i <= cha; i++)
			{
				float t = i * (1.00f / cha);
				float tt = t * t;
				float ttt = tt * t;
				float x = (float) (0.5 * (2 * p1.x + (p2.x - p0.x) * t + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt + (3 * p1.x - p0.x - 3 * p2.x + p3.x)* ttt));
				float y = (float) (0.5 * (2 * p1.y + (p2.y - p0.y) * t + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt + (3 * p1.y - p0.y - 3 * p2.y + p3.y)* ttt));
				path.lineTo(x,y);
			}
		}
		path.lineTo(point.get(point.size() - 1).x, point.get(point.size() - 1).y);
	}
}
