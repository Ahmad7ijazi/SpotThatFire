package com.example.spotfire;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class FireMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public double Latitude, Longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLocation;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_map);


        Latitude = getIntent().getDoubleExtra("Latitude", 32);
        Longitude = getIntent().getDoubleExtra("Longitude", 39);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Fab
        fab = findViewById(R.id.fab);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //work with different map types
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.clear(); //clear old markers

        CameraPosition firePlace = CameraPosition.builder()
                .target(new LatLng(Latitude,Longitude))
                .zoom(0)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(firePlace));

        //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null);


        // Add a marker in Sydney and move the camera
        LatLng Fire = new LatLng(Latitude, Longitude);
        mMap.addMarker(new MarkerOptions()
                .position(Fire)
                .title("Fire Location")
                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_fire,R.color.colorPrimaryDark)));

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation = location;
                            CameraPosition myLocation = CameraPosition.builder()
                                    .target(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()))
                                    .zoom(10)
                                    .bearing(0)
                                    .tilt(45)
                                    .build();
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()))
                                    .title("My Location")
                                    .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_my_location,R.color.colorAccent)));
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myLocation), 1000, null);
                                }
                            });
                        } else {
                            fab.setEnabled(false);
                        }
                    }
                });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int color) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        DrawableCompat.setTint(vectorDrawable,ContextCompat.getColor(getApplicationContext(), color));
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
