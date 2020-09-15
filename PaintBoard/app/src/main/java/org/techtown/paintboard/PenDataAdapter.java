package org.techtown.paintboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class PenDataAdapter extends BaseAdapter {

    Context mConText;

    public static final int[] pens=new int[]{1,2,3,4,5,6,7,8,9,10,11,13,15,17,20};

    int rowCount, columnCount;

    public PenDataAdapter(Context context){
        super();
        mConText=context;

        rowCount=3;
        columnCount=3;
    }

    public int getNumColumns() {
        return columnCount;
    }

    public int getCount() {
        return rowCount * columnCount;
    }

    public Object getItem(int position) {
        return pens[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent){
        Log.d("PenDataAdapter", "getView("+position+") called.");

        //calculate position
        int rowIndex=position/rowCount;
        int columnIndex=position%rowCount;
        Log.d("PenDataAdapter", "Index : "+rowIndex+", "+columnIndex);

        GridView.LayoutParams params=new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);

        //create a pen image
        int areaWidth=10;
        int areaHeight=20;

        Bitmap penBitmap=Bitmap.createBitmap(areaWidth, areaHeight, Bitmap.Config.ARGB_8888);
        Canvas penCanvas=new Canvas();
        penCanvas.setBitmap(penBitmap);

        Paint mPaint=new Paint();
        mPaint.setColor(Color.WHITE);
        penCanvas.drawRect(0,0,areaWidth, areaHeight, mPaint);

        //set pen
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth((float)pens[position]);

        //draw the pen thickness image in the dialog
        penCanvas.drawLine(0, areaHeight/2, areaWidth-1, areaHeight/2, mPaint);
        BitmapDrawable penDrawable=new BitmapDrawable(mConText.getResources(),penBitmap);

        //creating pen thickness selection button
        Button aItem=new Button(mConText);
        aItem.setText(" ");
        aItem.setLayoutParams(params);
        aItem.setPadding(4,4,4,4);
        aItem.setBackgroundDrawable(penDrawable);
        aItem.setHeight(parent.getHeight()/3);
        aItem.setTag(pens[position]);

        aItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PenPaletteDialog.listener!=null){
                    PenPaletteDialog.listener.onPenSelected(((Integer)v.getTag()).intValue());
                }
                ((PenPaletteDialog)mConText).finish();
            }
        });

        return aItem;
    }
}

/*
setTag()
sets the tag associated with this view.
A tag can be used to mark a view in its hierarchy and does not have to be unique within the hierarchy.
Tags can also be used to store data within a view without resorting to another data structure.
 */
