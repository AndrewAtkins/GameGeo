package com.android.gamegeo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartChallengeSelectDialog extends DialogFragment {


    public StartChallengeSelectDialog() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(getActivity())
//                .setTitle("What type of challenge would you like to start?")
                .create();

        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.fragment_start_challenge_select_dialog,null);

        alertDialog.setView(layout);

        /*
            Set the handler to check if the users guess was correct
         */
        Button pictionaryButton = (Button)layout.findViewById(R.id.start_pictionary_challenge_button);
        pictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button triviaButton = (Button)layout.findViewById(R.id.start_trivia_challenge_button);
        triviaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        "Trivia game functionality is not in place yet.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return alertDialog;
    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_start_challenge_select_dialog, container, false);
//    }

}
