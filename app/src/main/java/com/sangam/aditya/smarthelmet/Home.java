package com.sangam.aditya.smarthelmet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


public class Home extends ActionBarActivity implements TextToSpeech.OnInitListener, LocationListener {
    //CONSTANTS
    public static final String USER_SERVER_URL          = "http://7274cc3c.ngrok.com/db.php?name=8122514058&date=2302";
    public static final String BASE_SERVER_URL          = "http://7274cc3c.ngrok.com/";

    // this is the String used to identify and accesses the shared preferences file used to store the numbers.
    public static final String USER                     = "com.sangam.smarthelmet_UserData";
    public static final String USER_FUEL                = "com.sangam.smarthelmet_fueldetails";
    public static final String EMERGENCY_NUMBER_MASTER  = "com.sangam.smarthelmet_emergencynumbermasterkey";
    public static final String EMERGENCY_NUMBER1        = "com.sangam.smarthelmet_emergencynumber1key";
    public static final String EMERGENCY_NUMBER2        = "com.sangam.smarthelmet_emergencynumber2key";
    public static final String EMERGENCY_NUMBER3        = "com.sangam.smarthelmet_emergencynumber3key";
    public static final String USER_NUMBER_MASTER       = "com.sangam.smarthelmet_usernumberkey";
    public static final String USER_NUMBER_NAME         = "com.sangam.smarthelmet_user_name";

    public static final String USER_BLOOD_GROUP         = "com.sangam.smarthelmet_userbloodgroup";
    public static final String USER_BIKE_MODEL          = "com.sangam.smarthelmet_userbikemodel";

    public static final String USER_REGISTRATION        = "com.sangam.smarthelmet_registrationstatus";
    public static final String USER_FUEL_FILLED         = "com.sangam.smarthelmet_totalfuelfilled";
    public static final String USER_FUEL_REMAINING      = "com.sangam.smarthelmet_totalremaining";

    public static final String USER_GENDER              = "com.sangam.smarthelmet_gender";
    public static final String USER_MEDICAL_HISTORY     = "com.sangam.smarthelmet_medicalhistory";
    // this is the default number which will be stored as emergency numbers
    private static final long DEFAULT_EMERGENCY_NUMBER = 0;

    public static final long BIKE_MILEAGE = 1507;
    public static final long BIKE_RADUIS = 24;
    public static final long FUEL_LIMIT = 5;

// fuelconsumed = (no of revolutions)*(2*PI*R)


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

    public void showProfile(View view) {
        if(!isNetworkConnected()){
            Toast.makeText(this,"No internet connection",Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(this, ShowProfile.class);
        startActivity(intent);
    }

    public void fuelDetails(View view) {
        Intent intent = new Intent(this, FuelDetails.class);
        startActivity(intent);
    }

    //a function to check if Internet is available
    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    public void showWeather(View view) {
        if(!isNetworkConnected()){
            Toast.makeText(this,"No internet connection",Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, BargraphDisplay.class);
        startActivity(intent);
    }

    public void showDetails(View view) {
        Intent intent = new Intent(this, Details.class);
        startActivity(intent);
    }

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
    String lat1, lon1;


    String serverResponse=null;
    String[] datearray = new String[10];


    public class acc extends AsyncTask<Void, Integer, String> {
        String servertemp;
        protected String doInBackground(Void...arg0) {
            try {
                URL url = new URL(Home.BASE_SERVER_URL+"a.txt");
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

                    servertemp = new String(outputByteByByte.toByteArray(), encoding);

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
            return "";
        }

        protected void onProgressUpdate(Integer...a){
            // Log.d("You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            Log.d("", "onpostexecte");
            if(servertemp.matches("1"))
            {
                Log.i("gt","overspeeding");

                SmsManager smsManager =     SmsManager.getDefault();
                SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER, MODE_PRIVATE);
                say_this("You are Over-speeding, Please slow down");
                smsManager.sendTextMessage("8754302349", null, pref.getString(Home.USER_NUMBER_NAME,"The bike")+" is Overspeeding", null, null);

                new reset().execute();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("DEBUG","delay");
                    }
                }, 50000);

            }

            if(servertemp.matches("2"))
            {
                Log.i("gt","accident");

                SmsManager smsManager =     SmsManager.getDefault();
                smsManager.sendTextMessage("8754302349", null, "Accident", null, null);

                new reset().execute();


            }

            new acc().execute();
        }
    }



    public class reset extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void...arg0) {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Home.BASE_SERVER_URL+"/bike.php?some=0");
            try {
                HttpResponse response = client.execute(httpGet);
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onProgressUpdate(Integer...a){
        }

        protected void onPostExecute(String result) {
        }
    }

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
            distance = (2*3.14*Radius) * revolutions;
            Log.i("debug - revolutions",Double.toString(revolutions));

            // Now lets fetch total petrol filled till now
            SharedPreferences pref   = getApplicationContext().getSharedPreferences(Home.USER_FUEL, MODE_PRIVATE);
            String totalFuelFilled = pref.getString(Home.USER_FUEL_FILLED,"0");
            Log.i("debug - total filled", totalFuelFilled);

            //double totalDistanceTravelled = Double.parseDouble(pref.getString(Home.USER_TOTAL_DISTANCE,"0"))+distance;
            double totalFuelConsumed = distance / 60;

             fuelRemaining = Double.parseDouble(totalFuelFilled) - totalFuelConsumed ;
            notification();
        }
    }
    Double distance;
    Double fuelRemaining;
    public void notification()
    {
        if(!(fuelRemaining.intValue()<Home.FUEL_LIMIT))
            return;
        Log.i("gt","not");
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, "Petrol is low", when);
        say_this("You are running low on petrol");

        String title = this.getString(R.string.app_name);

        Intent notificationIntent = new Intent(this, Home.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, title, "Petrol is low", intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);
    }

    public void service_notification()
    {
        if(distance.intValue()>20)
            return;
        {
            Log.i("gt", distance.toString());
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(icon, "Service your bike", when);

            String title = this.getString(R.string.app_name);

            Intent notificationIntent = new Intent(this, Home.class);
            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.setLatestEventInfo(this, title, "Petrol is low", intent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(0, notification);
        }
    }



    @Override
    public void onLocationChanged(Location location2)
    {
        location = location2;
        lat1 = Double.toString(location.getLatitude());
        lon1 = Double.toString(location.getLatitude());
        //tv.setText("You just moved");
        Log.i("gt", lat1+lon1);
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

      //  tv.setText("You just moved");
    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
        LocationManager lam = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lam.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//        Log.i("gt", location.getLongitude()+location.getLatitude());
    //        tv.setText("You just moved");
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

     //   tv.setText("You just moved");
    }


    Location location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(!isNetworkConnected()){
            finish();
        }
        new getRevolutions().execute();
        new acc().execute();
        //notification();

        if(location!=null) {
            String lat1 = Double.toString(location.getLatitude());
            String lon1 = Double.toString(location.getLongitude());
            String towrite = "geo:" + lat1 + "," + lon1;
            Log.i("gt", towrite);
        }

        mSMSreceiver = new SMSreceiver();
        IntentFilter mIntentFilter = new IntentFilter();
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
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Home.USER, MODE_PRIVATE);
        String name = pref.getString(Home.USER_NUMBER_NAME, "Dude");

        ((TextView)findViewById(R.id.textViewhellouser)).setText("Hello "+name);
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
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
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
        String text = "Welcome to the Smart Bike Android application";
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
            return phoneNumber;
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
                        say_this("check 2 looking at ringing");
                        // Its ringing
                        //TextView tv = (TextView)findViewById(R.id.textView6); used for DEBUGGING TODO delete this
                        //say_this("You are getting a call from " + getContactName(incomingNumber));
                        AudioManager am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        call_came=1;
                        String caller = getContactName(incomingNumber);
                        if(caller.equals("")){
                            String[] phonenumberdigits = incomingNumber.split("(?!^)");

                            for (String phonenumberdigit : phonenumberdigits) {
                                caller = caller + phonenumberdigit + " ";
                                Log.i("DEBUG", phonenumberdigit);
                            }
                        }

                        Log.i("DEBUG",caller);
                        say_this("You are getting a call from " + caller);
                        Log.i("CAll CAME","yo");

                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(incomingNumber, "Unknown phone state=" + state);
                        break;
                    default:
                        Log.d("DEBUG", "Unknown phone state=" + state);
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
                //say_welcome();
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

                for (Object smsextra : smsextras) {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextra);

                    String strMsgBody = smsmsg.getMessageBody();
                    String strMsgSrc = smsmsg.getOriginatingAddress();
                    String sender = getContactName(strMsgSrc);
                    Log.i("DEBUG", sender);
                    if (sender.equals("")) {
                        String[] phoneNumberDigits = strMsgSrc.split("(?!^)");

                        for (String phonenumberdigit : phoneNumberDigits) {
                            sender = sender + phonenumberdigit + " ";
                            Log.i("DEBUG", phonenumberdigit);
                        }
                        strMessage += "You received a SMS from " + sender + " : " + strMsgBody;
                        say_this(strMessage);
                        Log.i("DEBUG", strMessage);
                    }
                }

            }

        }

    }


    public void start_journey(View v){
        // Switching on the Bluetooth automatically
        BluetoothAdapter.getDefaultAdapter().enable();

        // First start the HotSpot which will be used by the helmet to send data to server
        /*
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
        */
        // Now take user to the page where the user can set the source and destination
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





