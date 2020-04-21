package com.android.gamegeo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button submitButton = (Button) findViewById(R.id.submit_login_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // log the user in, and, if successful launch the MapsActivity
                // for now we're just gonna launch it
//                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
////                Bundle bundle = new Bundle();
////                bundle.putDouble("user_lat", getArguments().getDouble("user_lat"));
////                bundle.putDouble("user_long", getArguments().getDouble("user_long"));
////                i.putExtras(bundle);
//                startActivity(i);
//
                Intent myIntent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(myIntent);
            }
        });

    }

}
