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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Pattern;


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

        // Validate the entered numbers
        if(!valid_numbers(no1.getText().toString(),no2.getText().toString(),no3.getText().toString())){
            return;
        }

        // Store these numbers into memory
        editor.putLong(Home.EMERGENCY_NUMBER1, Long.valueOf(no1.getText().toString()));
        editor.putLong(Home.EMERGENCY_NUMBER2, Long.valueOf(no2.getText().toString()));
        editor.putLong(Home.EMERGENCY_NUMBER3, Long.valueOf(no3.getText().toString()));


        // Save the changes in SharedPreferences ie commit changes
        editor.apply();

        // Inform the user the numbers have been changed
        Toast.makeText(this, "Your Numbers have been Validated and Saved", Toast.LENGTH_SHORT).show();
    }

    private boolean valid_numbers(String no1,String no2,String no3) {
        //check if the entered numbers are valid
        if(no1.length()!=10 && !Pattern.matches("[a-zA-Z]+", no1)){
            Toast.makeText(this, "Enter a 10 digit phone number (first entry)", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(no2.length()!=10 && !Pattern.matches("[a-zA-Z]+", no2)){
            Toast.makeText(this, "Enter a 10 digit phone number (second entry)", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(no3.length()!=10 && !Pattern.matches("[a-zA-Z]+", no3)){
            Toast.makeText(this, "Enter a 10 digit phone number (third entry)", Toast.LENGTH_SHORT).show();
            return false;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber num1 = null;
        Phonenumber.PhoneNumber num2 = null;
        Phonenumber.PhoneNumber num3 = null;


        try {

            // I set the default region to IN (India)
            // You can find your country code here http://www.iso.org/iso/country_names_and_code_elements
            num1 = phoneUtil.parse(no1,"IN");
            num2 = phoneUtil.parse(no2,"IN");
            num3 = phoneUtil.parse(no3,"IN");


        } catch (NumberParseException e) {
            // if there’s any error
            Log.i("debug", "NumberParseException was thrown: " + e.toString());
        }

        // check if the the first number is valid
        boolean isValid1 = phoneUtil.isValidNumber(num1);
        // check if the second number is valid
        boolean isValid2 = phoneUtil.isValidNumber(num2);
        // check if the third number is valid
        boolean isValid3 = phoneUtil.isValidNumber(num3);

        // get the 3 number’s international format - NOT USED NOW
        //String internationalFormat1 = phoneUtil.format(num1,PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        //String internationalFormat2 = phoneUtil.format(num2,PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        //String internationalFormat3 = phoneUtil.format(num3,PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);


       if(isValid1 && isValid2 && isValid3){
           return true;
       }
       else{
            // prompt the user when the number is invalid
            Toast.makeText(getBaseContext(),"Phone numbers validation status - "+ isValid1+" "+isValid2+" "+isValid3,Toast.LENGTH_SHORT).show();
            return false;
       }

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
