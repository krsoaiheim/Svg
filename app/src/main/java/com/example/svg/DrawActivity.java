package com.example.svg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class DrawActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DrawView draw = new DrawView(this);
        draw.setClickable(true);
        setContentView(draw);
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw.invalidate();
            }
        });
    }
}
