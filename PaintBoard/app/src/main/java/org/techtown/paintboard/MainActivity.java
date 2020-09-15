package org.techtown.paintboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    PaintBoard board;
    Button colorBtn;
    Button penBtn;
    Button eraserBtn;
    Button undoBtn;

    RadioButton radio01, radio02, radio03;

    //put legend buttons
    LinearLayout legendLayout;
    Button colorLegendButton, sizeLegendButton;

    int mColor=0xff000000;
    int mSize=2;
    int oldColor, oldSize;
    boolean eraserSelected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //menu buttons layout
        LinearLayout toolsLayout=findViewById(R.id.toolsLayout);
        //paintboard part(drawing part)
        final LinearLayout boardLayout=findViewById(R.id.boardLayout);

        colorBtn=findViewById(R.id.colorBtn);
        penBtn=findViewById(R.id.penBtn);
        eraserBtn=findViewById(R.id.eraserBtn);
        undoBtn=findViewById(R.id.undoBtn);

        //cap style_butt, round, square
        radio01=findViewById(R.id.radio01);
        radio02=findViewById(R.id.radio02);
        radio03=findViewById(R.id.radio03);

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        board=new PaintBoard(this);
        board.setLayoutParams(params);
        board.setPadding(2,2,2,2);

        boardLayout.addView(board);

        legendLayout=findViewById(R.id.legendLayout);
        colorLegendButton=findViewById(R.id.colorLegendButton);
        sizeLegendButton=findViewById(R.id.sizeLegendButton);

        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPaletteDialog.listener=new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        mColor=color;
                        board.updatePaintProperty(mColor, mSize);
                        displayPaintProperty();
                    }
                };

                //show color palette dialog
                Intent intent=new Intent(getApplicationContext(), ColorPaletteDialog.class);
                startActivity(intent);
            }
        });

        penBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PenPaletteDialog.listener=new OnPenSelectedListener() {
                    @Override
                    public void onPenSelected(int pen) {
                        mSize=pen;
                        board.updatePaintProperty(mColor, mSize);
                        displayPaintProperty();
                    }
                };

                Intent intent=new Intent(getApplicationContext(), PenPaletteDialog.class);
                startActivity(intent);
            }
        });

        eraserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change to know it's time to erase or not
                eraserSelected= !eraserSelected;
                if(eraserSelected){
                    //cannot change settings while erasing
                    colorBtn.setEnabled(false);
                    penBtn.setEnabled(false);
                    undoBtn.setEnabled(false);

                    //initialize the settings
                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    //save the current setting(need when change after erasing is done)
                    oldColor=mColor;
                    oldSize=mSize;

                    mColor= Color.WHITE;
                    mSize=15;

                    board.updatePaintProperty(mColor, mSize);
                    displayPaintProperty();
                }else{//end erasing
                    colorBtn.setEnabled(true);
                    penBtn.setEnabled(true);
                    undoBtn.setEnabled(true);

                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    mColor=oldColor;
                    mSize=oldSize;

                    board.updatePaintProperty(mColor, mSize);
                    displayPaintProperty();
                }
            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                board.undo();
            }
        });

        radio01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "RadioButton 01(BUTT) clicked.", Toast.LENGTH_LONG).show();
                board.setCapStyle(Paint.Cap.BUTT);
            }
        });

        radio02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "RadioButton 02(Round) clicked.", Toast.LENGTH_LONG).show();
                board.setCapStyle(Paint.Cap.ROUND);
            }
        });

        radio03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "RadioButton 03(Square) clicked.", Toast.LENGTH_LONG).show();
                board.setCapStyle(Paint.Cap.SQUARE);
            }
        });
    }

    public int getChosenColor(){
        return mColor;
    }

    public int getPenThickness(){
        return mSize;
    }

    private void displayPaintProperty(){
        colorLegendButton.setBackgroundColor(mColor);
        sizeLegendButton.setText("Size : "+mSize);

        legendLayout.invalidate();
    }
}