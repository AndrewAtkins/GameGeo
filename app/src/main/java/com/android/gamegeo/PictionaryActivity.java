package com.android.gamegeo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PictionaryActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pictionary);
//
//        Button backToMap = (Button)findViewById(R.id.back_to_map_button);
//        final Context c = this;
//        backToMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(c, MapsActivity.class);
//                startActivity(i);
//            }
//        });
//    }


    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictionary);
        /*
            Create the paint area
         */
        paintView = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        /*
            Create the back button
         */
        Button backToMap = (Button)findViewById(R.id.back_to_map_button);
        final Context c = this;
        backToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, MapsActivity.class);
                startActivity(i);
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.normal:
//                paintView.normal();
//                return true;
//            case R.id.emboss:
//                paintView.emboss();
//                return true;
//            case R.id.blur:
//                paintView.blur();
//                return true;
//            case R.id.clear:
//                paintView.clear();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
