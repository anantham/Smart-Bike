package com.sangam.aditya.smarthelmet;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


public class Home extends ActionBarActivity implements TextToSpeech.OnInitListener {
    //CONSTANTS

    // this is the String used to identify and accesses the shared preferences file used to store the numbers.
    public static final String USER                     = "com.sangam.smarthelmet_UserData";
    public static final String EMERGENCY_NUMBER_MASTER  = "com.sangam.smarthelmet_emergencynumbermasterkey";
    public static final String EMERGENCY_NUMBER1        = "com.sangam.smarthelmet_emergencynumber1key";
    public static final String EMERGENCY_NUMBER2        = "com.sangam.smarthelmet_emergencynumber2key";
    public static final String EMERGENCY_NUMBER3        = "com.sangam.smarthelmet_emergencynumber3key";
    public static final String USER_NUMBER_MASTER       = "com.sangam.smarthelmet_usernumberkey";
    public static final String USER_NUMBER_NAME         = "com.sangam.smarthelmet_user_name";

    public static final String USER_BLOOD_GROUP       = "com.sangam.smarthelmet_userbloodgroup";
    public static final String USER_BIKE_MODEL         = "com.sangam.smarthelmet_userbikemodel";

    public static final String USER_REGISTRATION         = "com.sangam.smarthelmet_registrationstatus";


    // this is the default number which will be stored as emergency numbers
    private static final long DEFAULT_EMERGENCY_NUMBER = 0;


    // these are the variables which hold the numbers them self
    long emergency_number1;
    long emergency_number2;
    long emergency_number3;

    // This flag variable is used to keep track of whether the user is travelling or not
    boolean journey = false;

    // This is used to store the response from the server
    String server_response;

    // This is used to handle the conversion from text to speech
    TextToSpeech textToSpeech;

    //delay between successive pinging of server asking about status in milliseconds
    long delay = 7000;

    //While sending a SMS we use a test number for now
    String test_number = "8122514058";

    // the URL at which the required data is hosted
    String server_URL = "http://spider.nitt.edu/~adityap/dead.php";

    // Variables used for SMS notification broadcast reciever
    private SMSreceiver mSMSreceiver;
    private IntentFilter mIntentFilter;

    // classes nested inside the main class Home.java

    // This is the class used to start threads which keeps checking with the server to see if any accident has happened
    class ping_server extends AsyncTask<Void, Integer, String>
    {
        protected String doInBackground(Void...arg0) {
            URL url = null;
            try {
                url = new URL(server_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("error", "Url is not correct");
            }

            try {
                URLConnection con;
                if (url == null) con = null;
                else con = url.openConnection();
                if(con == null){
                    return null;
                }
                // because of the preceding if statement, we don't have to worry about con being null
                InputStream in = con.getInputStream();

                // get the encoding used by the server
                String encoding = con.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;

                // Get this temp variable which will hold all the data we fetch byte by byte
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int len;
                // traverse through the file and write the data into the buffer variable buf
                while ((len = in.read(buf)) != -1) {
                    temp.write(buf, 0, len);
                }

                // we don't need to return this as this variable is a global one
                server_response = new String(temp.toByteArray(), encoding);

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("error", "unable to open a connection");
            }

            return null;
        }

        protected void onPostExecute(String result) {
            // now that we have finished checking with the server
            if(server_response.equals("1"))
            {
                Log.i("1", "deathed server sends 1");
            }
            else
            {
                //send_sms();
                Log.i("0", "no news, server sends 0");
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AudioManager am;
        am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        //am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        mSMSreceiver = new SMSreceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);

        // Sets a listener to override on any incoming phone call and inform the user the caller's name
        handle_calls();

        // We get the stored emergency numbers onto application's variables
        restore_data();

        // In case the journey is over, or a new journey is about to start this activity will be called ? TODO look into if this flag will be set correctly all the time
        journey = false;

        // Here we initialize it, to get ready for conversion
        textToSpeech = new TextToSpeech(this, this);

    }

    // At the end of the activity close the handle used for text conversion and stop pinging the server
    @Override
    public void onDestroy() {
        Log.i("Destoy", "Time");
        journey = false;
        textToSpeech.shutdown();
        unregisterReceiver(mSMSreceiver);
        AudioManager am;
        am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        //am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        BluetoothAdapter.getDefaultAdapter().disable();
        super.onDestroy();
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.i("Stop", "Time");
    }


    //TODO  A deprecated function - speak
    private void say_welcome() {
        String text = "Welcome to Smart Helmet";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    // Set a listener to maintain a eye if any calls come the listener is defined later
    private void handle_calls() {
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    // This function takes the phone number as  argument and returns the corresponding contact name from the list of contacts in the mobile
    public String getContactName(final String phoneNumber)
    {
        Uri uri;
        String[] projection;
        // TODO deprecated guys! we need to use a different library
        Uri mBaseUri = Contacts.Phones.CONTENT_FILTER_URL;
        projection = new String[] { android.provider.Contacts.People.NAME };
        try {
            Class<?> c =Class.forName("android.provider.ContactsContract$PhoneLookup");
            mBaseUri = (Uri) c.getField("CONTENT_FILTER_URI").get(mBaseUri);
            projection = new String[] { "display_name" };
        }
        catch (Exception e) {
            Log.i("Error-adi","getting contact name unsuccessful");
        }

        uri = Uri.withAppendedPath(mBaseUri, Uri.encode(phoneNumber));
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);

        String contactName = "";

        if (cursor.moveToFirst())
        {
           contactName = cursor.getString(0);
        }
        cursor.close();

        return contactName;
    }

    // This is where the incoming call listener is defined
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        // This function is called when we get a incoming call
        int call_came =0;
        public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Its ringing
                        //TextView tv = (TextView)findViewById(R.id.textView6); used for DEBUGGING TODO delete this
                        //say_this("You are getting a call from " + getContactName(incomingNumber));
                        AudioManager am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                        //am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        call_came=1;
                        say_this("You are getting a call from " + getContactName(incomingNumber));

                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // do something...
                        if(call_came==1)
                        {
                            say_this("You recieved a call from " + getContactName(incomingNumber));
                            call_came=0;
                        }
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // do something...
                        if(call_came==1)
                        {
                            say_this("You recieved a call from " + getContactName(incomingNumber));
                            call_came=0;
                        }
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;
                    default:
                        if(call_came==1)
                        {
                            say_this("You recieved a call from " + getContactName(incomingNumber));
                            call_came=0;
                        }
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                }
        }
    };

    // This function converts the string that is passed as argument into speech and 'speaks' it
    private void say_this(String text) {

        if (null == text || "".equals(text)) {
            text = "You have a Call";
        }
        //TODO same as before, deprecated function
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

    // This is a method that was inherited, as this class Home implements, This is called when we initialize the TextToSpeech variable in Oncreate
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                // Welcome the user to the application TODO  do we need this? it takes time for this message to be spoken, doesn't serve a purpose
                say_welcome();
            }
        } else {
            Log.e("error", "Initialization Failed!");
        }
        
    }

    private class SMSreceiver extends BroadcastReceiver
    {
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();

            String strMessage = "";

            if ( extras != null )
            {
                Object[] smsextras = (Object[]) extras.get( "pdus" );

                for ( int i = 0; i < smsextras.length; i++ )
                {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                    String strMsgBody = smsmsg.getMessageBody().toString();
                    String strMsgSrc = smsmsg.getOriginatingAddress();

                    strMessage += "You received a SMS from " + getContactName(strMsgSrc) + " : " + strMsgBody;
                    say_this(strMessage);
                    Log.i(TAG, strMessage);
                }

            }

        }

    }


    public void start_journey(View v){
        // Switching on the Bluetooth automatically
        BluetoothAdapter.getDefaultAdapter().enable();

        // First start the HotSpot which will be used by the helmet to send data to server
        Context context= v.getContext();
        WifiManager wifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = "MyAP";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try{
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus=(Boolean) setWifiApMethod.invoke(wifiManager, netConfig,true);

            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){};
            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apstate=(Integer)getWifiApStateMethod.invoke(wifiManager);
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig=(WifiConfiguration)getWifiApConfigurationMethod.invoke(wifiManager);
            Log.e("CLIENT", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");

        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }

        // Now take user to the page where the user can set the source and desti
        Intent intent = new Intent(this, SetDestination.class);
        startActivity(intent);

    }

    // This is to keep checking with the server if the accident has occurred
    public void start_pinging()  {

        journey = true;

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(journey) {
                        // wait for 'delay' milliseconds'
                        sleep(delay);
                        // Start a new async task to fetch data from server
                        new ping_server().execute();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        // The code above defines the thread, here we start it,
        // A loop which keeps pinging the server URL
        thread.start();
        Log.i("thread","started");
    }

    // sends sms to test_number for now
    private void send_sms() {
        SmsManager smsManager =     SmsManager.getDefault();
        smsManager.sendTextMessage(test_number, null, "Message", null, null);
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





