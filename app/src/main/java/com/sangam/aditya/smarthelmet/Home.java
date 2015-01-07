package com.sangam.aditya.smarthelmet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;


public class Home extends ActionBarActivity implements
        TextToSpeech.OnInitListener {
    //CONSTANTS

    // this is the String used to identify and accesses the shared preferences file used to store the numbers.
    public static final String EMERGENCY_NUMBER_MASTER = "com.sangam.smarthelmet_emergencynumbermasterkey";
    public static final String EMERGENCY_NUMBER1 = "com.sangam.smarthelmet_emergencynumber1key";
    public static final String EMERGENCY_NUMBER2 = "com.sangam.smarthelmet_emergencynumber2key";
    public static final String EMERGENCY_NUMBER3 = "com.sangam.smarthelmet_emergencynumber3key";

    // this is the default number which will be stored as emergency numbers
    private static final long DEFAULT_EMERGENCY_NUMBER = 0;


    // these are the variables which hold the numbers them self
    long emergency_number1;
    long emergency_number2;
    long emergency_number3;

    boolean journey = false;
    String readfromwebpage;

    TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        handle_calls();
        // We get the stored emergency numbers onto application's variables
        restore_data();
        journey=false;

        textToSpeech = new TextToSpeech(this, this);

    }
    @Override
    public void onDestroy() {
        textToSpeech.shutdown();
    }

    private void convertTextToSpeech() {
        String text = "Welcome to Smart Helmet";
        if (null == text || "".equals(text)) {
            text = "Please give some input.";
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    
    private void handle_calls() {
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public String getContactName(final String phoneNumber)
    {
        Uri uri;
        String[] projection;
        Uri mBaseUri = Contacts.Phones.CONTENT_FILTER_URL;
        projection = new String[] { android.provider.Contacts.People.NAME };
        try {
            Class<?> c =Class.forName("android.provider.ContactsContract$PhoneLookup");
            mBaseUri = (Uri) c.getField("CONTENT_FILTER_URI").get(mBaseUri);
            projection = new String[] { "display_name" };
        }
        catch (Exception e) {
        }


        uri = Uri.withAppendedPath(mBaseUri, Uri.encode(phoneNumber));
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);

        String contactName = "";

        if (cursor.moveToFirst())
        {
            contactName = cursor.getString(0);
        }

        cursor.close();
        cursor = null;

        return contactName;
    }

    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                Context ctx = getApplicationContext();
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // do something...
                        TextView tv = (TextView)findViewById(R.id.textView6);
                        tv.setText("You are getting a call from "+getContactName(incomingNumber));
                        textToSpeechwithtring("You are getting a call from " + getContactName(incomingNumber));
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // do something...

                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // do something...

                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;
                    default:
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                }
            } catch (Exception e) {}
        }
    };

    private void textToSpeechwithtring(String text) {
        if (null == text || "".equals(text)) {
            text = "Please give some input.";
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }

    // This is called when we click the change emergency numbers button
    public void set_emergency_number(View v){
        /* Make an intent from "this" ie Home - the class to EditEmergencyNumber the class */
        Intent intent = new Intent(this, EditEmergencyNumber.class);
        /*
        For now we don't have any Extra to send along with this intent
        So lets just launch the next activity.
        */
        startActivity(intent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                convertTextToSpeech();
            }
        } else {
            Log.e("error", "Initilization Failed!");
        }
        
    }


    class TestAsynch extends AsyncTask<Void, Integer, String>
    {

        protected String doInBackground(Void...arg0) {
            Log.i("doing","backgroud");
            URL url = null;
            try {
                url = new URL("http://spider.nitt.edu/~adityap/dead.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("error", "1");
            }
            URLConnection con = null;
            try {
                con = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();

                Log.i("error", "2");
            }
            InputStream in = null;
            try {
                in = con.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();

                Log.i("error", "3");
            }
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[2048];
            int len = 0;
            try {
                Log.i("error", "4");
                while ((len = in.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();

                Log.i("error", "5");
            }

            try {
                readfromwebpage= new String(baos.toByteArray(), encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

                Log.i("error", "6");
            }
            Log.i("background", readfromwebpage);
            return null;
        }


        protected void onPostExecute(String result) {
            Log.i("onpost","executed");
            if(readfromwebpage.equals("1"))
            {
                Log.i("myroom", "sendsmssuccesful");
            }
            else
            {
                //send_sms();
                Log.i("myroom", "unsuccessful");
            }

        }
    }

    // This is called when the user clicks the start journey button
    public void start_journey(View v)  {

        journey = true;

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(journey) {

                        Log.i("inside","thread");
                        sleep(2000);
                        new TestAsynch().execute();

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
        Log.i("thread","started");


        // Because this requires Google Play Services
        //send_sms();


    }

    TextToSpeech ttobj;




    private void send_sms() {
        Log.i("asd", "asd");
        SmsManager smsManager =     SmsManager.getDefault();
        smsManager.sendTextMessage("8122514058", null, "Message", null, null);
    }

    private void restore_data() {
        // here we get a handle on the shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(EMERGENCY_NUMBER_MASTER, Context.MODE_PRIVATE);

        // Here we get the shared preferences values into usable variables
        emergency_number1 = sharedPref.getLong(EMERGENCY_NUMBER1, DEFAULT_EMERGENCY_NUMBER);
        emergency_number2 = sharedPref.getLong(EMERGENCY_NUMBER2, DEFAULT_EMERGENCY_NUMBER);
        emergency_number3 = sharedPref.getLong(EMERGENCY_NUMBER3, DEFAULT_EMERGENCY_NUMBER);

        // Now if at least one such number has been saved then
        if(emergency_number1 != DEFAULT_EMERGENCY_NUMBER || emergency_number2 != DEFAULT_EMERGENCY_NUMBER || emergency_number3 != DEFAULT_EMERGENCY_NUMBER) {
            //change color of button which is used to Set new emergency numbers AND change its text
            Button emergency = (Button) findViewById(R.id.button_emergency);
            emergency.setTextColor(Color.BLUE);
            emergency.setText("Change Emergency Numbers");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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





