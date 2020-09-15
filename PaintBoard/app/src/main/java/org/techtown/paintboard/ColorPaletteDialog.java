package org.techtown.paintboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class ColorPaletteDialog extends Activity {
    GridView grid;
    Button closeBtn;
    ColorDataAdapter adapter;
    public static OnColorSelectedListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        this.setTitle("Color Selection");

        grid=findViewById(R.id.colorGrid);
        closeBtn=findViewById(R.id.closeBtn);

        grid.setColumnWidth(14);
        grid.setBackgroundColor(Color.GRAY);
        //define a space interval
        grid.setVerticalSpacing(4);
        grid.setHorizontalSpacing(4);

        adapter=new ColorDataAdapter(this);
        grid.setAdapter(adapter);
        grid.setNumColumns(adapter.getNumColumns());

        closeBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }
}



