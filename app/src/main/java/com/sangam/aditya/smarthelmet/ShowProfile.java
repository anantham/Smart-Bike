package com.sangam.aditya.smarthelmet;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class ShowProfile extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        TextView name = (TextView)findViewById(R.id.textViewname);
        TextView phonenumber = (TextView)findViewById(R.id.textViewphonenumber);
        TextView bloodgroup = (TextView)findViewById(R.id.textViewbloodgroup);
        TextView bikemodel = (TextView)findViewById(R.id.textViewbikemodel);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER, MODE_PRIVATE);

        name.setText(pref.getString(Home.USER_NUMBER_NAME,"Not registered"));
        phonenumber.setText( Long.toString(pref.getLong(Home.USER_NUMBER_MASTER,0000000000)) );
        bloodgroup.setText(pref.getString(Home.USER_BLOOD_GROUP,"Not registered"));
        bikemodel.setText(pref.getString(Home.USER_BIKE_MODEL,"Not registered"));
        new getRevolutions().execute();

        petrolremaining = (TextView)findViewById(R.id.textViewpetrolleft);

    }

    TextView petrolremaining;
    String serverResponse=null;
    String[] datearray = new String[10];

    public class getRevolutions extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void...arg0) {
            try {
                URL url = new URL(Home.USER_SERVER_URL);
                Log.i("the empId", "");

                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();

                String encoding = connection.getContentEncoding();
                if (encoding == null) encoding = "UTF-8";

                ByteArrayOutputStream outputByteByByte = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                try {
                    // Read the inputStream using the buffer
                    while ((len = inputStream.read(buffer)) != -1) {
                        // write what you get to the outputByteByByte variable
                        outputByteByByte.write(buffer, 0, len);
                    }

                    serverResponse = new String(outputByteByByte.toByteArray(), encoding);

                } catch (IOException e) {
                    Log.i("IOException", "buffer to outputByteByByte");
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                Log.i("MalformedURLException", "URL not in proper format");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("IOException", "connection with server");
                e.printStackTrace();
            }
            datearray=serverResponse.split("#");

            return serverResponse;
        }

        protected void onProgressUpdate(Integer...a){
            // Log.d("You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            Log.d("", "onpostexecte");
            double revolutions = 0;
            for(int i = 0; i<10 ; i++)
            {
                revolutions = revolutions + Integer.parseInt(datearray[i]);
                Log.i("debug",datearray[i]);
            }


            // Here I will hardcode the values of Yamaha Crux, a popular Indian Bike
            // it's MILEAGE = 91 kilometer per litre fuel economy under standard test conditions.
            // -- Do note that real world riding conditions are many times different from the standard test conditions.
            // 60 IS USERS RATED MILEAGE
            // RADIUS of wheel taking total approximation
            // Now revolutions will be total revolutions travelled till now =>
            double Radius = 30 * 0.00001; // In KiloMeters
            double distance = (2*3.14*Radius) * revolutions;
            Log.i("debug - revolutions",Double.toString(revolutions));

            // Now lets fetch total petrol filled till now
            SharedPreferences pref   = getApplicationContext().getSharedPreferences(Home.USER_FUEL, MODE_PRIVATE);
            String totalFuelFilled = pref.getString(Home.USER_FUEL_FILLED,"0");
            Log.i("debug - total filled", totalFuelFilled);

            //double totalDistanceTravelled = Double.parseDouble(pref.getString(Home.USER_TOTAL_DISTANCE,"0"))+distance;
            double totalFuelConsumed = distance / 60;

            double fuelRemaining = Double.parseDouble(totalFuelFilled) - totalFuelConsumed ;
            petrolremaining.setText(Double.toString(fuelRemaining));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_profile, menu);
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
