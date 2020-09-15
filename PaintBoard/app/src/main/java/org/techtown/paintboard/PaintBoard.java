package org.techtown.paintboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.OutputStream;
import java.util.Stack;


public class PaintBoard extends View {

    //save undo data
    Stack undos=new Stack();

    //limit # of undos
    public static int maxUndos=10;

    //changed flag
    public boolean changed=false;

    //canvas instance, bitmap for double buffering, paint instance
    Canvas mCanvas;
    Bitmap mBitmap;
    Paint mPaint;

    //location
    float lastX, lastY;

    private final Path mPath=new Path();

    private float mCurveEndX;
    private float mCurveEndY;

    private int mInvalidateExtraBorder=10;

    //limit the strength of touch
    static final float TOUCH_TOLERANCE=8;

    private static final boolean RENDERING_ANTIALIAS=true;
    //dithering=decrease noise
    private static final boolean DITHER_FLAG=true;

    private int certainColor=0xFF000000;
    private float strokeWidth=2.0f;

    public PaintBoard(Context context){
        super(context);
        init();
    }

    public void init(){
        mPaint=new Paint();
        mPaint.setAntiAlias(RENDERING_ANTIALIAS);
        mPaint.setColor(certainColor);
        mPaint.setStyle(Paint.Style.STROKE);
        //set the connection shape used at the vertex of the stroke
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        //set the start and end shape of Stroke
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setDither(DITHER_FLAG);

        lastX=-1;
        lastY=-1;

        //use Log.i() to give information
        Log.i("PaintBoard", "initialized.");
    }

    //clear undo
    public void clearUndo(){
        while(true){
            Bitmap prev=(Bitmap)undos.pop();
            if(prev==null)//when undos is empty
                return;
            prev.recycle();
        }
    }

    //save undo
    public void saveUndo(){
        if(mBitmap==null)//if there's nothing to save
            return;
        while(undos.size()>=maxUndos){
            Bitmap i=(Bitmap)undos.get(undos.size()-1);
            i.recycle();
            undos.remove(i);
        }

        //draw to save in bitmap form
        Bitmap img=Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas();
        canvas.setBitmap(img);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        //save
        undos.push(img);

        Log.i("PaintBoard", "saveUndo() called");
    }

    //Undo
    public void undo(){
        Bitmap prev=null;
        try{
            prev=(Bitmap)undos.pop();
        }catch(Exception ex){
            //logging an error
            Log.e("PaintBoard", "Exception : "+ex.getMessage());
        }

        if(prev!=null){//if there's something get from undos stack
            drawBackground(mCanvas);
            mCanvas.drawBitmap(prev, 0, 0, mPaint);
            //redraw
            invalidate();

            prev.recycle();
        }

        Log.i("PaintBoard", "undo() called");
    }

    //paint background
    public void drawBackground(Canvas canvas){
        if(canvas!=null){
            canvas.drawColor(Color.WHITE);
        }
    }

    //update paint properties
    public void updatePaintProperty(int color, int size){
        mPaint.setColor(color);
        mPaint.setStrokeWidth(size);
    }

    //set CAP style
    public void setCapStyle(Paint.Cap capStyle){
        mPaint. setStrokeCap(capStyle);
    }

    //create a new image(use when size changed)
    public void newImage(int width, int height){
        Bitmap img=Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas();
        canvas.setBitmap(img);

        mBitmap=img;
        mCanvas=canvas;
        drawBackground(mCanvas);

        changed=false;
        invalidate();
    }

    public void setImage(Bitmap newImage){
        changed=false;

        setImageSize(newImage.getWidth(), newImage.getHeight(), newImage);
        invalidate();
    }

    //make the image size same as bitmap size
    public void setImageSize(int width, int height, Bitmap newImage){
        if(mBitmap!=null){
            if(width<mBitmap.getWidth())
                width=mBitmap.getWidth();
            if(height<mBitmap.getHeight())
                height=mBitmap.getHeight();
        }

        if(width<1||height<1) return;

        Bitmap img= Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas();
        drawBackground(canvas);

        if(newImage!=null)
            canvas.setBitmap(newImage);

        if(mBitmap!=null){
            mBitmap.recycle();
            //remove all modifications since the last save call.
            mCanvas.restore();
        }

        mBitmap=img;
        mCanvas=canvas;

        clearUndo();
    }


    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        if(w>0&&h>0)
            newImage(w,h);
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if(mBitmap!=null){
            canvas.drawBitmap(mBitmap,0,0,null);
        }
    }

    //handles touch event, UP, DOWN, and MOVE
    public boolean onTouchEvent(MotionEvent event){
        int action=event.getAction();
        switch(action){
            case MotionEvent.ACTION_UP:
                changed=true;

                Rect rect=touchUp(event, false);
                if(rect!=null){
                    invalidate(rect);
                }
                //clear lines and curves
                mPath.rewind();
                return true;

            case MotionEvent.ACTION_DOWN:
                saveUndo();
                rect=touchDown(event);
                if(rect!=null) {
                    invalidate(rect);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                rect=touchMove(event);
                if(rect!=null){
                    invalidate(rect);
                }
                return true;
        }
        return false;
    }

    //process event for touch down
    private Rect touchDown(MotionEvent event){
        float x=event.getX();
        float y=event.getY();

        lastX=x;
        lastY=y;

        Rect mInvalidRect=new Rect();
        mPath.moveTo(x,y);

        final int border=mInvalidateExtraBorder;
        mInvalidRect.set((int)x-border, (int)y-border,(int)x+border, (int)y+border);

        mCurveEndX=x;
        mCurveEndY=y;

        mCanvas.drawPath(mPath, mPaint);

        return mInvalidRect;
    }


    private Rect touchMove(MotionEvent event){
        Rect rect=processMove(event);
        return rect;
    }
    private Rect touchUp(MotionEvent event, boolean cancel){
        Rect rect=processMove(event);
        return rect;
    }

    private Rect processMove(MotionEvent event){
        final float x=event.getX();
        final float y=event.getY();

        //amount of change of x and y
        //abs() : returns the absolute value
        final float dx=Math.abs(x-lastX);
        final float dy=Math.abs(y-lastY);

        Rect mInvalidRect = new Rect();
        //if change is bigger than the expectation
        if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
            final int border =mInvalidateExtraBorder;
            mInvalidRect.set((int)mCurveEndX-border, (int)mCurveEndY-border, (int)mCurveEndX+border, (int)mCurveEndY+border);

            //center point
            float cX=mCurveEndX=(x+lastX)/2;
            float cY=mCurveEndY=(y+lastY)/2;

            //add a quadratic bezier(funcation) start at (x1,y1), and ending at (x2,y2) to make a smooth line
            //If no moveTo() call has been made for this contour, the first point is automatically set to (0,0).
            mPath.quadTo(lastX, lastY, cX, cY);

            //union with the control point of the new curve
            mInvalidRect.union((int)lastX-border, (int)lastY-border, (int)lastX+border, (int)lastY+border);

            lastX=x;
            lastY=y;

            mCanvas.drawPath(mPath, mPaint);
        }

        return mInvalidRect;
    }

    //save in jpeg
    public boolean Save(OutputStream outStream){
        try{
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            invalidate();

            return true;
        }catch (Exception e){
            return false;
        }
    }
}

/*
double buffering
a technique for drawing graphics that shows no (or less) stutter, tearing, and other artifacts.

Antialiasing
the process of making edge lines look smoother by blending in the colors at the edges.

recycle() method
allows an app to reclaim memory as soon as possible.
Caution: You should use recycle() only when you are sure that the bitmap is no longer being used.

invalidate()
Invalidate the whole view. If the view is visible, onDraw(android.graphics.Canvas) will be called at some point in the future.
This must be called from a UI thread. To call from a non-UI thread, call postInvalidate().
If in the course of processing the event the view's appearance may need to be changed, the view will call invalidate()

restore()
This call balances a previous call to save(), and is used to remove all modifications to the matrix/clip state since the last save call.
It is an error to call restore() more times than save() was called.

bezier
a curve made mathematically to express any form of curve.
 */
