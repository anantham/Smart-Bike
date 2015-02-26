package com.sangam.aditya.smarthelmet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class SetDestination extends ActionBarActivity {

    Long longitude;
    Long latitude;


    Bundle savgt= new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_destination);
        savgt = savedInstanceState;

        //sms part


        // Initialize a Location manager variable
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // If there is no network connectivity alert the user with a dialog box, prompting him/her to get  net access ASAP
        if(!isNetworkConnected()){
            createNetErrorDialog();
        }
        else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSDisabledAlertToUser();
        }
    }



    // IT does what its name says, pretty clear I hope.
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // this generates a dialog box which tells the user that the application needs a network connection
    // and the user can cancel, i.e exit the application or go to settings and do the needful
    protected void createNetErrorDialog() {
        // builds a alert with these things in order
        // the message, the  title, (the boolean which decides if the dialog can be cancelled, i.e just ignored),
        // the intent to be launched in case the user hits the "positive" button and the one for the "negative" button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SetDestination.this.finish();
                            }
                        }
                );
        // used the attributes defined above crate a dialog
        AlertDialog alert = builder.create();
        // Show it
        alert.show();
    }


        //a function to check if Internet is available
    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else {
            return true;
        }
    }

    // As I find that the Geocoding API which exists in the android API does not
    // work on all devices, and returns null on a lot of devices.
    // I am therefore suggesting using the Reverse Geocoding API which returns a JSON object
    // Also TODO let the user decide if this is the place he meant, offer him alternatives.
    public Address get_location_from_address(String Address){
        try {
            List<Address> result = new Geocoder(this).getFromLocationName(Address,1);
            return result.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    // Launches the google maps application with the user's desired destination
    public void launch_navigation(View v){
        // Get the address the user entered
        EditText source = (EditText)findViewById(R.id.editTextSource);

        EditText destination = (EditText)findViewById(R.id.editTextDestination);

        if(destination == null || destination.getText().toString().matches("")){
            Toast.makeText(this, "Please enter destination address and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (source == null || source.getText().toString().matches("")){
            Toast.makeText(this, "Please enter source address and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize a Location manager variable
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!isNetworkConnected()){
            Toast.makeText(this, "Please Enable Internet and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "Please Enable GPS and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        Address source_address = get_location_from_address(source.getText().toString());
        Address destination_address = get_location_from_address(destination.getText().toString());

        final Intent intent = new Intent(Intent.ACTION_VIEW,
                /** Using the web based turn by turn directions url. */
                Uri.parse(
                        "http://maps.google.com/maps?" +
                                "saddr="+String.valueOf(source_address.getLatitude())+","+String.valueOf(source_address.getLongitude())+
                                "&daddr="+String.valueOf(destination_address.getLatitude())+","+String.valueOf(destination_address.getLongitude())));
        /** Setting the Class Name that should handle
         *  this intent.  We are setting the class name to
         *  the class name of the native maps activity.
         *  Android platform recognizes this and now knows that
         *  we want to open up the Native Maps application to
         *  handle the URL.  Hence it does not give the choice of
         *  application to the user and directly opens the
         *  Native Google Maps application.
         */
        intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }


    @Override
public void onResume()
    {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_destination, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
