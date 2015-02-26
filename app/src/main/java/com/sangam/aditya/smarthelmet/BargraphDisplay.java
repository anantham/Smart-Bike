package com.sangam.aditya.smarthelmet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class BargraphDisplay extends ActionBarActivity{
    ProgressDialog progress;

    int flaggt=0;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("debug", "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bargraph_display);
        progress = new ProgressDialog(this);
        progress.setMessage(" Drawing Graph ");
        progress.show();
        new TestAsync().execute();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(flaggt==1)
        {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }
    }

    String serverResponse="";
    String[] datearray = new String[10];
    String[] datee = {"2302","2402","2502","2602","2702","2802","0103","0203","0303","0403","0503"};



    public class TestAsync extends AsyncTask<Void, Integer, String>{

        protected String doInBackground(Void...arg0) {
                try {
                        URL url = new URL(Home.USER_SERVER_URL);

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
            String[] mMonth = new String[]{
                    "23/02", "24/02", "25/02", "26/02", "27/02", "28/03",
                    "01/03", "02/03", "03/03", "04/03", "05/03", "06/03", "07/03", "08/03", "09/03", "10/03", "11/03", "12/03"
            };
            int[] x = {1, 2, 3, 4, 5, 6, 7, 8};
            int[] income = {2000, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 0; i < 8; i++) {
                income[i] = Integer.valueOf(datearray[i]);
                Log.i("debug", datearray[i]);
                if(Integer.valueOf(datearray[i])>=1000){
                    Log.i("debug","first STEP!!!");
                    accident_alert();
                }
            }
            //    int[] expense = {2200, 2700, 2900, 2800, 2600, 3000, 3300, 3400 };

            // Creating an  XYSeries for Income
            XYSeries incomeSeries = new XYSeries("Distance travelled per day");
            // Creating an  XYSeries for Expense
            //   XYSeries expenseSeries = new XYSeries("AC");
            // Adding data to Income and Expense Series
            for (int i = 0; i < x.length; i++) {
                incomeSeries.add(x[i], income[i]);
                //      expenseSeries.add(x[i],expense[i]);
            }

            // Creating a dataset to hold each series
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            // Adding Income Series to the dataset
            dataset.addSeries(incomeSeries);
            // Adding Expense Series to dataset
            //  dataset.addSeries(expenseSeries);

            // Creating XYSeriesRenderer to customize incomeSeries
            XYSeriesRenderer incomeRenderer = new XYSeriesRenderer();
            incomeRenderer.setColor(Color.RED);
            incomeRenderer.setPointStyle(PointStyle.CIRCLE);
            incomeRenderer.setFillPoints(true);
            incomeRenderer.setLineWidth(1);
            incomeRenderer.setDisplayChartValues(true);

            // Creating XYSeriesRenderer to customize expenseSeries
            //  XYSeriesRenderer expenseRenderer = new XYSeriesRenderer();
            // expenseRenderer.setColor(Color.BLACK);
            //expenseRenderer.setPointStyle(PointStyle.CIRCLE);
            //expenseRenderer.setFillPoints(true);
            //expenseRenderer.setLineWidth(2);
            //expenseRenderer.setDisplayChartValues(true);

            // Creating a XYMultipleSeriesRenderer to customize the whole chart
            XYMultipleSeriesRenderer multiRenderer =
                    new XYMultipleSeriesRenderer();
            multiRenderer.setBackgroundColor(Color.BLACK);
            multiRenderer.setGridColor(Color.BLACK);
            multiRenderer.setXLabels(0);
            multiRenderer.setChartTitle("Distance Travelled");
            multiRenderer.setXTitle("Date");
            multiRenderer.setYTitle("Revolutions");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < x.length; i++) {
                multiRenderer.addXTextLabel(i + 1, mMonth[i]);
            }

            // Adding incomeRenderer and expenseRenderer to multipleRenderer
            // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
            // should be same
            multiRenderer.addSeriesRenderer(incomeRenderer);
            //multiRenderer.addSeriesRenderer(expenseRenderer);

            // Creating an intent to plot line chart using dataset and multipleRenderer
            Intent intent = ChartFactory.getBarChartIntent(getBaseContext(), dataset, multiRenderer, BarChart.Type.DEFAULT);

            progress.dismiss();

            flaggt=1;
            // Start Activity
            startActivity(intent);


        }
    }

    private void accident_alert() {
        Log.i("debug","REACHED HERE!!!!!!!!!");
        LocationManager locationManager = (LocationManager)
        getSystemService(Context.LOCATION_SERVICE);
        //LocationListener locationListener = new MyLocationListener();
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        //Location l = locationManager.getLastKnownLocation(locationManager.getBestProvider(null, false));
        //Log.i(Double.toString(l.getLatitude()),Double.toString(l.getLongitude()));
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Log.i("debug","finally IN");
            //String longitude = Double.toString(loc.getLongitude());
            //String latitude = Double.toString(loc.getLatitude());
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("8754302349", null, "Lon is "+loc.getLatitude()+" and the lat is "+loc.getLongitude(), null, null);
            Log.i("debug","message sent!!!!!!!!!");
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bargraph_display, menu);
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
