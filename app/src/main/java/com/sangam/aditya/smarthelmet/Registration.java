package com.sangam.aditya.smarthelmet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class Registration extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        SharedPreferences prefRegistrationStatus = getApplicationContext().getSharedPreferences(Home.USER_REGISTRATION, MODE_PRIVATE);
        if(prefRegistrationStatus.getString(Home.USER_REGISTRATION,"not_done").equals("done")){
            Intent intent = new Intent(this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
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

    public void nextField(View v){
        // get the handle on the shared preferences we are using to store the numbers
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Get the number which we need to store.
        EditText number = (EditText)findViewById(R.id.editTextnumber);
        EditText name = (EditText)findViewById(R.id.editTextname);

        if(name.getText().toString().isEmpty() || number.getText().toString().isEmpty()){
            Toast.makeText(this, "Enter your details!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store these numbers into memory
        editor.putLong(Home.USER_NUMBER_MASTER, Long.valueOf(number.getText().toString()));

        // Store the name into memory
        editor.putString(Home.USER_NUMBER_NAME, name.getText().toString());

        // Save the changes in SharedPreferences ie commit changes
        editor.apply();

        // Inform the user the numbers have been changed
        Toast.makeText(this, "Your Details have been Saved", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, NameBloodGroup.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
