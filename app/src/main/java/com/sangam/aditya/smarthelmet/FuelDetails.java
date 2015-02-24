package com.sangam.aditya.smarthelmet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FuelDetails extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_details);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fuel_details, menu);
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

    public void saveFuelData(View view) {

        String fueladded = ((EditText) findViewById(R.id.editTextfueladded)).getText().toString();
        if (fueladded.isEmpty()) {
            Toast.makeText(this, "Enter fuel amount in litre's", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> fuelData = new HashSet<>(Arrays.asList("No Values"));

        // get the handle on the shared preferences we are using to store the numbers
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER_FUEL, MODE_PRIVATE);
        fuelData = pref.getStringSet(Home.USER_FUEL, fuelData);

        SharedPreferences.Editor editor = pref.edit();

        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        Log.i("debug", Integer.toString(date));
        Log.i("debug", fuelData.toString());

        fuelData.add(fueladded);

        editor.putStringSet(Home.USER_FUEL, fuelData);
        editor.apply();

        List<String> fuelList = new ArrayList<>(fuelData);
        ListView list = (ListView)findViewById(R.id.listViewfueldata);
        ArrayAdapter<String> listAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, (String[]) fuelList.toArray());
        list.setAdapter(listAdapter);
    }
}
