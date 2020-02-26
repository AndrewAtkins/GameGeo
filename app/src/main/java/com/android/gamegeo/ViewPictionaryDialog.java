package com.android.gamegeo;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPictionaryDialog extends DialogFragment {

    public ViewPictionaryDialog() {
        // Required empty public constructor
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("activity_type").toString();
        final String secretWord = getArguments().getString("secret_word").toString();
        String base64ImageString = getArguments().getString("image").toString();

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Can you guess what this image is of?")
                .setMessage("It is " + secretWord.length() + " letters long.")
                .create();

        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout2 = layoutInflater.inflate(R.layout.fragment_pictionary_dialog,null);

        alertDialog.setView(layout2);
        /*
            Set the image that the user created to the image in the View
         */
        byte[] decodedString = Base64.decode(base64ImageString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        ImageView imgV;
        imgV = (ImageView)layout2.findViewById(R.id.dialog_pictionary_imageview);
        imgV.setImageBitmap(decodedByte);
        LayoutParams params = (LayoutParams)imgV.getLayoutParams();
        params.width = 700;
        params.height = 700;

        /*
            Set the handler to check if the users guess was correct
         */
        Button submitButton = (Button)layout2.findViewById(R.id.pictionary_guess_button);
        final EditText guessText = (EditText)layout2.findViewById(R.id.pictionary_guess_text);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String value = guessText.getText().toString();
                if(value.toLowerCase().equals(secretWord.toLowerCase())){
                    Toast.makeText(getActivity(),
                            "You guessed correctly!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(),
                            "You guessed incorrectly.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return alertDialog;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_pictionary_dialog, container, false);
//    }

}
