package com.android.gamegeo;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    // The entry point to the Places API.
    private PlacesClient mPlacesClient;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // contains quality of service params for requests to FusedLocationProvider
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 600; //10 minutes between map updates
    private static final long FASTEST_INTERVAL = 1000 * 20; //20 seconds between map updates

    /* used for receiving notifications from FusedLocationProvider when location changes / no longer
     can be determined.*/
    private LocationCallback mLocationCallback;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    /*
        Challenge variables. This array will be populated with challenges pulled from the DB.
     */
    private ArrayList<Challenge> challenges = new ArrayList<Challenge>() ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location from saved instance state.
        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setContentView(R.layout.activity_maps);


        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /*
            Here we will need to call the database and populate the challenges array
         */
        challenges.add(new PictionaryChallenge("iVBORw0KGgoAAAANSUhEUgAAAH0AAABKCAIAAAAZncxnAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAASdAAAEnQB3mYfeAAABAZJREFUeF7tmr9S3DAQh32pqFPBTDJDkfQ8AnT3CFdSQnf9FXQwPAF00FJRcu/CUKWAJmmhu/wmu6MYj/9IWkkr2/rmJpF1F0v7aW8t+7LY7XZVITlf+O9CWop3HXTqzJ/vP7nVxtdfz9yaLum81133m+1flTrjXaHo3o3EGI66Vij/9YjonaSoKMh/PcJ7j5rgQhRToUFI7/lE1U8O8wzmfSzSDboTDrl/H5F0QLMl++kJ4x2zH5d0QnHOAbxrpUwQoB7zTx9CmHwfY7IbVCYv9T7SCqNOyOvqeKFqwwdJyMX7w8PD5eUlH8yAXLy/vLxsNhs+mAHS+6bJ1HeqM8limUh9l5cpMp6syrvle+u0csh3SEeZkj/zsPEeJF5b72ZCjVHRn4P3UAyGQx7kIQ/UGQxDL4xEL35jrpABsi+h0zvpRmNWuhHpoNMg6lu8z9O4E3It7fk+Z+MI3CaXLT/WxUT2kVp4q296x4lmm+kG+5Tnljsl33Uo3nUo3nUo3nUo3tsRbhMHKd51mL737XZ7cnJyf3/Px5mw+8zvbz+4ZYfr59Ozv7+PMPf29vjYGpvQvMNvyXfUNXrx8Zi5vb19e3tD4+Pjg3pygf23gcWsv7j3M139PTw9PR0fH9/d3fFxTCjZwWq14i5rBkPr0TKIw+9NXd8A19vlg4MD5CC++O/v79wVjcViQQ2MhRGpbQni7QrNqPB+VKDwu7ZxIRx6EBSZ8/NzanuM1VNpvXX/BxOS4PFF44HFQw8iKTLAu4bYMOV8FxYZ/BkgrzuYxX2Tq3QinnRQ7lebINN7KnsopN6RFN6zjHoPSdt2PxBU1GQHCvluvvVmsxGD6+trahwdHVHDBo/LlSf/rq4iXK/7V1dXPHZVXVxccG9ozGbm8fGRu3qR3AR5oOAdrNdrkgIiqeez2yVWSuOEjnds7JbLJYuJoP719ZVPPWHvQK7+8PAw4BObs7MzOi2KO3f1MiPvoKEe11t+Q8bNzQ2f0a64p5cOwngH3upPT09ZUlXhkstv+IIKY/ZLWFTu7WWO3glzmYUyZCv3emFOhQqDReXeblSkgyy8Q1D9Vl5ymfXYPnIrLcG8A0kM9U098FbP/976vmQi3oVZL9zhuG4fwRS8E2TfL56GeuBkP//toyG8d4O3+voOh7Cxj9LEn86+uIOI3gElvkd4jXLvRM7bR0Nc74SfeuBhH9IHt480n+l7J7zjtLe/Wq0GpQNd44T091UnzC8kiZ5xt5HuCXsvSb0b0gefw5LX0fEOjAgQ20UmOV5HzXudqF4ylA6y8A6Cp785YYbSQS7e60jWIHPdhhy9G0iipUGnD6uTtXeinv49jMU4MQLvk6T8Pz0dincdincdincNquovAPNgdJ320sgAAAAASUVORK5CYII=",
                "Face",30.342330, -87.096400));

        // Build the Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocationPermissionGranted) {
            try {
                startLocationUpdates();
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }


    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
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
        googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style1)));

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.getTitle().contains("Pictionary")) {
                    return true;
                }
                return true;

            }
        });


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Set properties of the map such as disabling panning
        setMapProperties();

        // Create markers from what is in the challenges array list
        createMarkers();



        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Sets properties of the map that we will always want such as not being able to pan and not being able to zoom out very far
     */
    private void setMapProperties() {
//        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(15);
        // mMap.getUiSettings().setScrollGesturesEnabled(false);
        // mMap.getUiSettings().setZoomGesturesEnabled(false);
    }

    /**
     * Creates the locationRequest and sets its parameters
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        // potentially try PRIORITY_HIGH_ACCURACY for better results at the cost of power. Also maybe adjust intervals.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Gets current location settings of a user's device.
     */
    private void getCurrentLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    //ignore the error
                }
            }
        });
    }

    private void startLocationUpdates() throws SecurityException {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.getMainLooper());
    }

    private void createMarkers() {
        for(Challenge c: challenges) {
            LatLng challenge = new LatLng(c.getLatitude(), c.getLongitude());
            mMap.addMarker(new MarkerOptions().position(challenge)
                    .title("Marker for a Pictionary Challenge"));
        }
    }


}
