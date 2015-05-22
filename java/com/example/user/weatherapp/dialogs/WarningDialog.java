package com.example.user.weatherapp.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.example.user.weatherapp.utils.GPSUtil;

/**
 * Created by User on 20.05.2015.
 */
public class WarningDialog {
    private Context context;
    private Location location=null;
    private static String LOG_TAG=SaveLocationDialog.class.getSimpleName();

    public WarningDialog(Context context) {
        this.context = context;
    }


    public void showMessage(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Set an EditText view to get user input

        // Set the alert title
        builder.setTitle("OOPs!")
                .setIcon(android.R.drawable.stat_sys_warning)
                // Set the alert message
                .setMessage(msg)
                        // Can't exit via back button
                .setCancelable(true)
                        // Set the  button action

                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                });


        // Create dialog from builder
        AlertDialog alert = builder.create();
        // Don't exit the dialog when the screen is touched
        alert.setCanceledOnTouchOutside(false);
        // Show the alert
        alert.show();
    }
}

