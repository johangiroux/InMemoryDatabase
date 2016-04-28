package com.johan.inmemorydatabase;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.johan.inmemorydatabase.database.MemoryDatabase;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        resultTextView = (TextView) findViewById(R.id.result);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onClick(View v) {
        InputStream inputStream = null;
        if (v.getId() == R.id.button1) {
            inputStream = getResources().openRawResource(R.raw.test_input1);
        } else if (v.getId() == R.id.button2) {
            inputStream = getResources().openRawResource(R.raw.test_input2);
        } else if (v.getId() == R.id.button3) {
            showDialog();
        }

        if (inputStream != null) {
            String result = MemoryDatabase.executeInputStream(inputStream);
            resultTextView.setText(result);
        }
    }

    private void showDialog() {

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        int margin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
        lp.setMargins(margin, margin, margin, margin);
        input.setLayoutParams(lp);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Execute", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String result = "invalid input";
                try {
                    InputStream stream = new ByteArrayInputStream(input.getText().toString().getBytes("UTF-8"));
                    result = MemoryDatabase.executeInputStream(stream);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                resultTextView.setText(result);
                dialog.dismiss();
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog alertDialog = builder.create();
        alertDialog.setView(input);
        alertDialog.show();
    }
}
