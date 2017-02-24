package com.example.svg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.svg.R.id.pathedit;

public class MainActivity extends AppCompatActivity {
    protected static String svg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText svgInput = (EditText) findViewById(pathedit);
        Button drawButton = (Button) findViewById(R.id.button);
        svg = svgInput.getText().toString();
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
            }
        });
    }
}
