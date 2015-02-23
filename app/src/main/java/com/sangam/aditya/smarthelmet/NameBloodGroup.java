package com.sangam.aditya.smarthelmet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class NameBloodGroup extends ActionBarActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_blood_group);

        Spinner bike_companys = (Spinner)findViewById(R.id.spinnerbikecompany);
        String[] items = new String[]{"Yamaha", "Honda", "Bajaj"};
        context = this;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        bike_companys.setAdapter(adapter);
        bike_companys.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                switch(position){
                    case 0:
                        Log.i("debug","its 0");
                        String[] models1 = new String[]{"Yamaha Crux", "Yamaha RAY", "Yamaha Alpha"};
                        setSpinner(models1);
                        break;
                    case 1:
                        Log.i("debug","its 1");
                        String[] models2 = new String[]{"Honda Dream Neo", "Honda Dream Yuga", "Honda CB Shine"};
                        setSpinner(models2);
                        break;
                    case 2:
                        Log.i("debug","its 2");
                        String[] models3 = new String[]{"Bajaj Pulsar 200", "Bajaj Avenger", "Bajaj Discover"};
                        setSpinner(models3);
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }


    public void setSpinner(String[] models){
        Spinner bike_model = (Spinner)findViewById(R.id.spinnerbikemodel);
        ArrayAdapter<String> adapter_models = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, models);
        bike_model.setAdapter(adapter_models);
    }

    public void goToHome(View v){

        // get the handle on the shared preferences we are using to store the numbers
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Get the data
        String bloodgroup = ((EditText)findViewById(R.id.editTextbloodgroup)).getText().toString();
        Spinner spinner = (Spinner)findViewById(R.id.spinnerbikemodel);
        String model = spinner.getSelectedItem().toString();

        if( bloodgroup.isEmpty() || model.isEmpty() ){
            Toast.makeText(this, "Enter your details!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store these numbers into memory
        editor.putString(Home.USER_BLOOD_GROUP, bloodgroup);
        // Store the name into memory
        editor.putString(Home.USER_BIKE_MODEL, model);

        // Save the changes in SharedPreferences ie commit changes
        editor.apply();

        // Inform the user the numbers have been changed
        Toast.makeText(this, "Your Details have been Saved", Toast.LENGTH_SHORT).show();

        SharedPreferences prefRegistrationStatus = getApplicationContext().getSharedPreferences(Home.USER_REGISTRATION, MODE_PRIVATE);
        SharedPreferences.Editor editorRegistrationStatus = prefRegistrationStatus.edit();
        editorRegistrationStatus.putString(Home.USER_REGISTRATION, "done");
        editorRegistrationStatus.apply();

        Intent intent = new Intent(this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_name_blood_group, menu);
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
