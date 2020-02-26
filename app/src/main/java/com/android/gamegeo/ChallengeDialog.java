package com.android.gamegeo;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChallengeDialog extends DialogFragment {


    public ChallengeDialog() {
        // Required empty public constructor
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("activity_type").toString();
        String secretWord = getArguments().getString("secret_word").toString();
        AlertDialog alertDialog;
        if(title == "Pictionary") {
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage("The user drew a picture of something that is " + secretWord.length() + " letters.\nDo you want to try this challenge?")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        } else {
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("A default title")
                    .setPositiveButton(android.R.string.yes, null)
                    .create();
        }
        return alertDialog;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_challenge_dialog, container, false);
//    }

}
