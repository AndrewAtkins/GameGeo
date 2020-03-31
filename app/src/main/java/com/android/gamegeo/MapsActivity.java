package com.android.gamegeo;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.android.gamegeo.ChallengeModels.Challenge;
import com.android.gamegeo.ChallengeModels.PictionaryChallenge;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mongodb.client.MongoClient;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private static final long INTERVAL = 1000 * 30; //30 seconds between map updates
    private static final long FASTEST_INTERVAL = 1000 * 5; //5 seconds between map updates
    /* used for receiving notifications from FusedLocationProvider when location changes / no longer
     can be determined.*/
    private LocationCallback mLocationCallback;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_updates";
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    //provides access to Location Settings API
    private SettingsClient mSettingsClient;
    /* Stores the types of location services the client is interested in using. Used for checking
     settings to determine if the device has optimal location settings.*/
    private LocationSettingsRequest mLocationSettingsRequest;
    private boolean mRequestingLocationUpdates;

    /* PLACEHOLDER: variables for creating new challenge on map*/
    private String newImage = "";
    private String newSecretWord = "";
    private double newChallengeLat = 0;
    private double newChallengeLong = 0;
    private double lastKnownLat = 0;
    private double lastKnownLong = 0;

//    /* DATABASE variables */
//    private RemoteMongoCollection<Document> pictionaryCollection;
    /*
        Challenge variables. This array will be populated with challenges pulled from the DB.
     */
    private HashMap<String, Challenge> challenges = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location from saved instance state.
        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mRequestingLocationUpdates = savedInstanceState.getParcelable(KEY_REQUESTING_LOCATION_UPDATES);
        }

        if(getIntent() != null && getIntent().getExtras() != null) {
            if(getIntent().getExtras().getString("new_image") != null) {
                newImage = getIntent().getExtras().getString("new_image");
            }
            if(getIntent().getExtras().getString("new_secret_word") != null) {
                newSecretWord = getIntent().getExtras().getString("new_secret_word");
            }
            if(getIntent().getExtras().getDouble("new_lat") != 0) {
                newChallengeLat = getIntent().getExtras().getDouble("new_lat");
            }
            if(getIntent().getExtras().getDouble("new_long") != 0) {
                newChallengeLong = getIntent().getExtras().getDouble("new_long");
            }
        }

        setContentView(R.layout.activity_maps);


        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //construct SettingsClient
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;
        /*
            Here we will need to call the database and populate the challenges array
         */
        PictionaryChallenge testchallenge = new PictionaryChallenge("iVBORw0KGgoAAAANSUhEUgAAAH0AAABKCAIAAAAZncxnAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAASdAAAEnQB3mYfeAAABAZJREFUeF7tmr9S3DAQh32pqFPBTDJDkfQ8AnT3CFdSQnf9FXQwPAF00FJRcu/CUKWAJmmhu/wmu6MYj/9IWkkr2/rmJpF1F0v7aW8t+7LY7XZVITlf+O9CWop3HXTqzJ/vP7nVxtdfz9yaLum81133m+1flTrjXaHo3o3EGI66Vij/9YjonaSoKMh/PcJ7j5rgQhRToUFI7/lE1U8O8wzmfSzSDboTDrl/H5F0QLMl++kJ4x2zH5d0QnHOAbxrpUwQoB7zTx9CmHwfY7IbVCYv9T7SCqNOyOvqeKFqwwdJyMX7w8PD5eUlH8yAXLy/vLxsNhs+mAHS+6bJ1HeqM8limUh9l5cpMp6syrvle+u0csh3SEeZkj/zsPEeJF5b72ZCjVHRn4P3UAyGQx7kIQ/UGQxDL4xEL35jrpABsi+h0zvpRmNWuhHpoNMg6lu8z9O4E3It7fk+Z+MI3CaXLT/WxUT2kVp4q296x4lmm+kG+5Tnljsl33Uo3nUo3nUo3nUo3tsRbhMHKd51mL737XZ7cnJyf3/Px5mw+8zvbz+4ZYfr59Ozv7+PMPf29vjYGpvQvMNvyXfUNXrx8Zi5vb19e3tD4+Pjg3pygf23gcWsv7j3M139PTw9PR0fH9/d3fFxTCjZwWq14i5rBkPr0TKIw+9NXd8A19vlg4MD5CC++O/v79wVjcViQQ2MhRGpbQni7QrNqPB+VKDwu7ZxIRx6EBSZ8/NzanuM1VNpvXX/BxOS4PFF44HFQw8iKTLAu4bYMOV8FxYZ/BkgrzuYxX2Tq3QinnRQ7lebINN7KnsopN6RFN6zjHoPSdt2PxBU1GQHCvluvvVmsxGD6+trahwdHVHDBo/LlSf/rq4iXK/7V1dXPHZVXVxccG9ozGbm8fGRu3qR3AR5oOAdrNdrkgIiqeez2yVWSuOEjnds7JbLJYuJoP719ZVPPWHvQK7+8PAw4BObs7MzOi2KO3f1MiPvoKEe11t+Q8bNzQ2f0a64p5cOwngH3upPT09ZUlXhkstv+IIKY/ZLWFTu7WWO3glzmYUyZCv3emFOhQqDReXeblSkgyy8Q1D9Vl5ymfXYPnIrLcG8A0kM9U098FbP/976vmQi3oVZL9zhuG4fwRS8E2TfL56GeuBkP//toyG8d4O3+voOh7Cxj9LEn86+uIOI3gElvkd4jXLvRM7bR0Nc74SfeuBhH9IHt480n+l7J7zjtLe/Wq0GpQNd44T091UnzC8kiZ5xt5HuCXsvSb0b0gefw5LX0fEOjAgQ20UmOV5HzXudqF4ylA6y8A6Cp785YYbSQS7e60jWIHPdhhy9G0iipUGnD6uTtXeinv49jMU4MQLvk6T8Pz0dincdincdincNquovAPNgdJ320sgAAAAASUVORK5CYII=",
                "Face", 30.342330, -87.096400, "999");
        PictionaryChallenge testchallenge2 = new PictionaryChallenge("iVBORw0KGgoAAAANSUhEUgAAAH0AAABKCAIAAAAZncxnAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAASdAAAEnQB3mYfeAAABAZJREFUeF7tmr9S3DAQh32pqFPBTDJDkfQ8AnT3CFdSQnf9FXQwPAF00FJRcu/CUKWAJmmhu/wmu6MYj/9IWkkr2/rmJpF1F0v7aW8t+7LY7XZVITlf+O9CWop3HXTqzJ/vP7nVxtdfz9yaLum81133m+1flTrjXaHo3o3EGI66Vij/9YjonaSoKMh/PcJ7j5rgQhRToUFI7/lE1U8O8wzmfSzSDboTDrl/H5F0QLMl++kJ4x2zH5d0QnHOAbxrpUwQoB7zTx9CmHwfY7IbVCYv9T7SCqNOyOvqeKFqwwdJyMX7w8PD5eUlH8yAXLy/vLxsNhs+mAHS+6bJ1HeqM8limUh9l5cpMp6syrvle+u0csh3SEeZkj/zsPEeJF5b72ZCjVHRn4P3UAyGQx7kIQ/UGQxDL4xEL35jrpABsi+h0zvpRmNWuhHpoNMg6lu8z9O4E3It7fk+Z+MI3CaXLT/WxUT2kVp4q296x4lmm+kG+5Tnljsl33Uo3nUo3nUo3nUo3tsRbhMHKd51mL737XZ7cnJyf3/Px5mw+8zvbz+4ZYfr59Ozv7+PMPf29vjYGpvQvMNvyXfUNXrx8Zi5vb19e3tD4+Pjg3pygf23gcWsv7j3M139PTw9PR0fH9/d3fFxTCjZwWq14i5rBkPr0TKIw+9NXd8A19vlg4MD5CC++O/v79wVjcViQQ2MhRGpbQni7QrNqPB+VKDwu7ZxIRx6EBSZ8/NzanuM1VNpvXX/BxOS4PFF44HFQw8iKTLAu4bYMOV8FxYZ/BkgrzuYxX2Tq3QinnRQ7lebINN7KnsopN6RFN6zjHoPSdt2PxBU1GQHCvluvvVmsxGD6+trahwdHVHDBo/LlSf/rq4iXK/7V1dXPHZVXVxccG9ozGbm8fGRu3qR3AR5oOAdrNdrkgIiqeez2yVWSuOEjnds7JbLJYuJoP719ZVPPWHvQK7+8PAw4BObs7MzOi2KO3f1MiPvoKEe11t+Q8bNzQ2f0a64p5cOwngH3upPT09ZUlXhkstv+IIKY/ZLWFTu7WWO3glzmYUyZCv3emFOhQqDReXeblSkgyy8Q1D9Vl5ymfXYPnIrLcG8A0kM9U098FbP/976vmQi3oVZL9zhuG4fwRS8E2TfL56GeuBkP//toyG8d4O3+voOh7Cxj9LEn86+uIOI3gElvkd4jXLvRM7bR0Nc74SfeuBhH9IHt480n+l7J7zjtLe/Wq0GpQNd44T091UnzC8kiZ5xt5HuCXsvSb0b0gefw5LX0fEOjAgQ20UmOV5HzXudqF4ylA6y8A6Cp785YYbSQS7e60jWIHPdhhy9G0iipUGnD6uTtXeinv49jMU4MQLvk6T8Pz0dincdincdincNquovAPNgdJ320sgAAAAASUVORK5CYII=",
                "Face", 30.547075, -87.216621, "988");
        PictionaryChallenge testchallenge3 = new PictionaryChallenge("iVBORw0KGgoAAAANSUhEUgAAAJMAAABqCAIAAAA3GvSTAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAATNSURBVHhe7Z27ceswEEUZOnTo8JWgEhwqdOjQoUN3oFAlKHQJClWGy1AJCv3ueO9wdiiKJmVigQX2ZPwJXB4CWPCn7jvwSSXmTqfTvx8+Pz85q3ZqMAdtj4+P3Q8PDw+NyHNvTmsTGpHn2xwMDbQJkMc16sWxud1uR1E/vL+/7/d7TnSV9N8TuIxQ8hEq+gHaZBGnw1yBDFrI7XZ7uVy4LMwVy6CFfH191doAF3Rd9UmKG3MTLaQGuYksrT5JcWPu6elJlIBBC6lpJ0lxEB7aPV3brlvIAVwvzOXlumPjgttw1TCXkV/zkVG4dpjLhdY20bFdw23CXBYOhwMP/0JtgJuFuSz0meRSbUA2BJyulBLD0+3kUm2AW4Y5Y7S2OZnkNdw4zFlyd1ai4fZhzobBxa27tQH+RJgzYHBf+y/aAH8lzKVmoG3mcHsC/lCYS8pA2+jlfzC4dDkTblwpmcPTdwBWh2VUSubw+ttpKWAZlZI5PH07bZr5/R83CHPuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3PuoLcw5w56C3O3uH5xeynY/HA48OfWg78e5m7xR21Lma+5fyqJ05Vyf3jzH/5JwYRI2bFbj27+kTlPfmIFg296rHZicq9nn+mXy+Xt7Y3b5ADH9+Pj43w+c4fGuO8JXWDwTY9s5haRXfMdcNeT4cPcgLwip5/85EphLh3H43Gz2XCnb7P0DRVuFubcwaMQ5tzBoxDm3MGjEOZ8oT/jwlnJWK2A/srFbrfjrPboXwe876sSi1jNHEa1stOgTXm6wi1KR+9jNXPY1+12yx3vuq+vLy5oBssKB9ZsjiHv+flZ9v7l5YVz2+B0OkngwKDCgZU7UlQ17n7X7fd7zm0A/cI7ZyVm/WL661LVf0lZIyGDRPcorlnfHNoKBtHSCIEBG4acpKQGRwgSL+B0epKUpEcIjfR2jNa7OT1CaKS3k2ABp9OTqiTI69vMFqqdRAo4nZ6EJfVtZgvVTiIFnE5PwpJ0tas+VZEwAafTk7akdlIVBlmNuXZSFYkRcDo9yUtqIVXRFy05Kz0WJelUxeARUnvsL1oCi5J0tauyzZTQgNlFS2B0jqCdZHDV5ZmWTzBo7AqrMs88n899c2JzQ7XHzlyVeSaaR4los9kgQM41wbSC6w7P5oWXpOh28ng8cq4VpuaAbjO917w+pURbwlmGWJtDtdMvc/jNVrDnjMHqwZMB1uYEXfM8ytPajBOTnjzmdLYCfMnT3RuiyFLhQB5zwKk8ZFX939Fk1AaymQMDecg2cTpzWZHo2gYyagM5zYGBPFDsIF0PuoHlha5RMpsDkDd4dbjMoV6fVdkPukfJb07AsdBndFFDvcE3e+wH3aOUYg7oq9JCCT2fTkkELshNQeYEPdQTcjWeKFRXNSF799ZTnLnrbg+g8YTR6a/OrMLEx8zKcSYUZ65nVGGK9nO0bmmWfljDhnLNCdedn/BHhb/aEsp0JpRuTpj51Zm1KK1hHMWHOWG0/VyFkuvWLTyZ61lFoUdbGpfmAhDmvBLmvBLmvBLmvBLmvBLmvBLmvBLmvBLmvBLmfPL9/R+lHID3E+/pLwAAAABJRU5ErkJggg==",
                "Boat", 30.546784, -87.216664, "1028");
        challenges.put(testchallenge.getId(), testchallenge);
        challenges.put(testchallenge2.getId(), testchallenge2);
        challenges.put(testchallenge3.getId(), testchallenge3);

        if(!newImage.equals("") && !newSecretWord.equals("") && newChallengeLat != 0 && newChallengeLong != 0) {
            PictionaryChallenge newChallenge = new PictionaryChallenge(newImage,
                    newSecretWord, newChallengeLat, newChallengeLong, "77");
            challenges.put(newChallenge.getId(), newChallenge);
        }

        // Build the Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* Button Handler for starting a challenge */
        Button startChallengeButton = (Button) findViewById(R.id.start_challenge_button);
        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putDouble("user_lat", lastKnownLat);
                args.putDouble("user_long", lastKnownLong);

                FragmentManager fm = getSupportFragmentManager();
                StartChallengeSelectDialog dialog = new StartChallengeSelectDialog();
                dialog.setArguments(args);

                dialog.show(fm, "Challenge Select");
            }
        });


        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        /* Create the connection to the pictionary_pins collection*/
//        pictionaryCollection = ((App)this.getApplication()).getMongoClient().getDatabase("GameGeo").getCollection("pictionary_pins");
//
//        /* If a challenge was created, send it to the database*/
//        if(!newImage.equals("") && !newSecretWord.equals("") && newChallengeLat != 0 && newChallengeLong != 0) {
//            insertPictionaryChallengToDatabase(newChallengeLat, newChallengeLong, newSecretWord, newImage);
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationPermissionGranted) {
            mRequestingLocationUpdates = true;
            try {
                startLocationUpdates();
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
        else if(!mLocationPermissionGranted)
        {
            getLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mMap != null) {
            savedInstanceState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            savedInstanceState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);

            super.onSaveInstanceState(savedInstanceState);
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

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.getTitle().contains("Pictionary")) {
                    String markerId = (String) marker.getTag();
                    PictionaryChallenge pc = (PictionaryChallenge) challenges.get(markerId);
                    // launch a pictionary guessing preview and pass in the data from the challenge
                    // pass in data of pictionary marker
                    Bundle bundle = new Bundle();
                    bundle.putString("activity_type", "Pictionary");
                    bundle.putString("image", pc.getImage());
                    bundle.putString("secret_word", pc.getSecretWord());

                    FragmentManager fm = getSupportFragmentManager();
                    ViewPictionaryDialog dialog = new ViewPictionaryDialog();
                    dialog.setArguments(bundle);
                    dialog.show(fm, "Pictionary");

                    return true;
                }
                return true;

            }
        });

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
            mRequestingLocationUpdates = true;

            if(mRequestingLocationUpdates)
            {
                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                startLocationUpdates();
            }

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
                    mRequestingLocationUpdates = true;
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
                if (mLastKnownLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    lastKnownLat = mLastKnownLocation.getLatitude();
                    lastKnownLong = mLastKnownLocation.getLongitude();
                }
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
        //mMap.getUiSettings().setScrollGesturesEnabled(false);
        //mMap.getUiSettings().setZoomGesturesEnabled(false);
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
     * Creates the locationCallback
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastKnownLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() throws SecurityException {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MapsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }


    private void createMarkers() {
//        for(Challenge c: challenges) {
//            LatLng challenge = new LatLng(c.getLatitude(), c.getLongitude());
//            Marker m = mMap.addMarker(new MarkerOptions().position(challenge)
//                    .title("Marker for a Pictionary Challenge"));
//            m.setTag(c.getId());
//        }
        for (Map.Entry c : challenges.entrySet()) {
            Challenge challenge = (PictionaryChallenge) c.getValue();
            LatLng cLatLong = new LatLng(challenge.getLatitude(), challenge.getLongitude());
            Marker m = mMap.addMarker(new MarkerOptions().position(cLatLong)
                    .title("Marker for a Pictionary Challenge"));
            m.setTag(challenge.getId());
            // set the icon of the marker
            int height = 150;
            int width = 150;
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pictionary_marker);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(smallMarker);
            m.setIcon(icon);
        }
    }

//    private void insertPictionaryChallengToDatabase(double lat, double lon, String secret_word, String picture) {
//        Document newItem = new Document()
//                .append("lat", lat)
//                .append("long", lon)
//                .append("secret_word", secret_word)
//                .append("picture", picture);
//
//
//        final Task <RemoteInsertOneResult> insertTask = pictionaryCollection.insertOne(newItem);
//        insertTask.addOnCompleteListener(new OnCompleteListener <RemoteInsertOneResult> () {
//            @Override
//            public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d("app", String.format("successfully inserted item with id %s",
//                            task.getResult().getInsertedId()));
//                } else {
//                    Log.e("app", "failed to insert document with: ", task.getException());
//                }
//            }
//        });
//    }
}
