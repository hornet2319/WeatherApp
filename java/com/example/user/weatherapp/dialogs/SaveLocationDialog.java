package com.example.user.weatherapp.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.example.user.weatherapp.utils.GPSUtil;

/**
 * Created by User on 19.05.2015.
 */
public class SaveLocationDialog {
    private Context context;
    private Location location=null;
    private static String LOG_TAG=SaveLocationDialog.class.getSimpleName();

    public SaveLocationDialog(Context context) {
        this.context = context;
    }

    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        // Set the alert title
        builder.setTitle("Saving current location")
                // Set the alert message
                .setMessage("Please enter a name of this location:")
                        // Set the view to the dialog
                .setView(input)
                        // Can't exit via back button
                .setCancelable(true)
                        // Set the positive button action
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(LOG_TAG, "OK has been pushed");
                        GPSUtil gpsUtil=new GPSUtil(context);
                        location=gpsUtil.getLocation();
                        String name=input.getText().toString();
                        //saving goes here
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i(LOG_TAG, "Cancel has been pushed");
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
