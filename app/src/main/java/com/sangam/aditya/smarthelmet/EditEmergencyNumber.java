package com.sangam.aditya.smarthelmet;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class EditEmergencyNumber extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_emergency_number);

        //If the numbers have already been saved restore them
        restore_to_edit_text();
    }

    private void restore_to_edit_text() {

        // get a handle on to the edit text's
        EditText no1 = (EditText)findViewById(R.id.editText);
        EditText no2 = (EditText)findViewById(R.id.editText2);
        EditText no3 = (EditText)findViewById(R.id.editText3);

        // get the numbers from memory
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.EMERGENCY_NUMBER_MASTER, MODE_PRIVATE);

        // set these numbers to the edit text
        no1.setText(Long.toString(pref.getLong(Home.EMERGENCY_NUMBER1, 0)));
        no2.setText(Long.toString(pref.getLong(Home.EMERGENCY_NUMBER2, 0)));
        no3.setText(Long.toString(pref.getLong(Home.EMERGENCY_NUMBER3, 0)));

    }

    public void save_numbers(View v){

        // get the handle on the shared preferences we are using to store the numbers
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.EMERGENCY_NUMBER_MASTER, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Get the numbers which we need to store.
        EditText no1 = (EditText)findViewById(R.id.editText);
        EditText no2 = (EditText)findViewById(R.id.editText2);
        EditText no3 = (EditText)findViewById(R.id.editText3);


        // Store these numbers into memory
        editor.putLong(Home.EMERGENCY_NUMBER1, Long.valueOf(no1.getText().toString()));
        editor.putLong(Home.EMERGENCY_NUMBER2, Long.valueOf(no2.getText().toString()));
        editor.putLong(Home.EMERGENCY_NUMBER3, Long.valueOf(no3.getText().toString()));


        // Save the changes in SharedPreferences ie commit changes
        editor.commit();

        // Inform the user the numbers have been changed
        Toast.makeText(this, "Your Numbers have been saved", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_emergency_number, menu);
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
