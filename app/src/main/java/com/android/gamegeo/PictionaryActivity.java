package com.android.gamegeo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.AdapterView.OnItemSelectedListener;

public class PictionaryActivity extends AppCompatActivity implements OnItemSelectedListener {

    private PaintView paintView;
    private final Context c = this;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
            Create the eraser button handler
         */
        ImageButton eraserButton = (ImageButton)findViewById(R.id.eraser_button);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setEraser();
            }
        });

        /*
            Set color picker for pictionary
         */
        final Spinner spinner = (Spinner) findViewById(R.id.color_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        /*
            Create the brush button handler
         */
        ImageButton brushButton = (ImageButton)findViewById(R.id.drawing_button);
        brushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setCurrentColor("BLACK");
                spinner.setSelection(3);
            }
        });

        /*
            Create the submit button handler
         */
        Button submitButton = (Button) findViewById(R.id.submit_pictionary_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textSubmit = (EditText) findViewById(R.id.drawing_title_text);
                String secretWord = textSubmit.getText().toString();
                if(secretWord.length() > 0) {
                    String imageEncoded = paintView.convertImageToBase64();
                    /* PLACEHOLDER: Once we have the database set up, we will want to push this data to the database rather than sending it as an extra*/
                    Intent myIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("new_image", imageEncoded);
                    bundle.putString("new_secret_word", secretWord);
                    bundle.putDouble("new_lat", getIntent().getExtras().getDouble("user_lat"));
                    bundle.putDouble("new_long", getIntent().getExtras().getDouble("user_long"));
                    myIntent.putExtras(bundle);
                    startActivityForResult(myIntent, 0);

                } else {
                    Toast.makeText(PictionaryActivity.this, "You must enter a secret word!", Toast.LENGTH_SHORT);
                }
            }
        });
    }
    /*
        Method for the back button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String colorChoice = parent.getItemAtPosition(pos).toString();
        switch (colorChoice.toUpperCase()){
            case "RED":
                paintView.setCurrentColor("RED");
                break;
            case "BLUE":
                paintView.setCurrentColor("BLUE");
                break;
            case "GREEN":
                paintView.setCurrentColor("GREEN");
                break;
            case "BLACK":
                paintView.setCurrentColor("BLACK");
                break;
            default:
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
