package com.example.spotfire;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSIONS_REQUEST = 23;
    public TextView mLocationText;
    public static final String TAG = MainActivity.class.getSimpleName();
    public Location mLocation;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationText = (TextView) findViewById(R.id.locationText);

        OneSignal.promptLocation();

    }
    public void reportFire(View view){
        Log.d(TAG, "reportFire");
        reportFireLocation();
    }

    public void reportFireLocation() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d(TAG, location.toString());
                            mLocation = location;
                            mLocationText.setText(location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                });
        AsyncTask.execute(sendAlerts);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //imageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    reportFireLocation();
                }
                return;
            }
        }
    }


    private Runnable sendAlerts = new Runnable() {
        @Override
        public void run() {
            try {
                String jsonResponse;

                URL url = new URL("https://onesignal.com/api/v1/notifications");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Authorization", "Basic ZTEzZTYwZjItMTZhMi00MjE4LTg3YjktODMzODJiY2Y2ZDY4");
                con.setRequestMethod("POST");

                String strJsonBody = "{"
                        +   "\"app_id\": \"e34c4205-851a-4743-8e25-fe5dd946f9fc\","
                        +   "\"filters\": [{\"field\": \"location\", \"radius\": \"1000\", \"lat\": \""+mLocation.getLatitude()+"\", \"long\": \""+mLocation.getLongitude()+"\"}],"
                        +   "\"data\": {\"foo\": \"bar\"},"
                        +   "\"headings\": {\"en\": \"Be Cautious\"},"
                        +   "\"contents\": {\"en\": \"There is a fire Nearby\"},"
                        +   "\"data\": {\"Latitude\": \""+mLocation.getLatitude()+"\", \"Longitude\": \""+mLocation.getLongitude()+"\"},"
                        +   "\"app_url\": \"app://com.example.spotfire\""
                        + "}";


                System.out.println("strJsonBody:\n" + strJsonBody);

                byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                con.setFixedLengthStreamingMode(sendBytes.length);

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(sendBytes);

                int httpResponse = con.getResponseCode();
                System.out.println("httpResponse: " + httpResponse);

                if (  httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                    Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                }
                else {
                    Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                }
                System.out.println("jsonResponse:\n" + jsonResponse);

            } catch(Throwable t) {
                t.printStackTrace();
            }
        }
    };
}
