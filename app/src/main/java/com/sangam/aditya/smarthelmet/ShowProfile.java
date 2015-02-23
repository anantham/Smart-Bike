package com.sangam.aditya.smarthelmet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


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
