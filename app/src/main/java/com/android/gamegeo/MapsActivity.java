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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.bson.Document;


import java.util.HashMap;
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
    // boundary for the camera
    private LatLngBounds BOUNDS;
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
        ((App)this.getApplication()).getPictionaryCollection().find()
        .projection(new Document().append("lat", 1).append("long", 1).append("picture", 1).append("secret_word", 1))
        .forEach(document -> {
            // Print documents to the log.
//            Log.i(TAG, "Got document: " + document.toString());
            PictionaryChallenge c = new PictionaryChallenge(document.get("picture").toString(),
                    document.get("secret_word").toString(), Double.parseDouble(document.get("lat").toString()), Double.parseDouble(document.get("long").toString()), document.get("_id").toString());
            challenges.put(c.getId(), c);
        });

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
        } else if (!mLocationPermissionGranted) {
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
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.animateCamera(CameraUpdateFactory
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

            if (mRequestingLocationUpdates) {
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
                if (mLastKnownLocation != null) {
                    setBounds();
                    mMap.setLatLngBoundsForCameraTarget(BOUNDS);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                            mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));

                    // Create markers from what is in the challenges array list
                    createMarkers();

                    lastKnownLat = mLastKnownLocation.getLatitude();
                    lastKnownLong = mLastKnownLocation.getLongitude();

                }
            } else {
                mMap.setMyLocationEnabled(false);
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
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(15);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

    }

    /**
     * Creates a LatLng Bounds for camera target.
     */
    private void setBounds() {
        LatLngBounds.Builder latLngBoundBuilder = new LatLngBounds.Builder();

        latLngBoundBuilder.include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        BOUNDS = latLngBoundBuilder.build();
    }

    /**
     * Check is a marker is near a player
     *
     * @return true if marker is within a player's camera bounds
     */
    private boolean isMarkerWithinBounds(LatLng latLng) {
        boolean isMarkerWithinBounds = false;


    if (true)  // ****THIS CONDITIONAL NEED WORk****
         {
            /* Log.i("bounds", "bounds = " + BOUNDS.toString());
             Log.i("bounds", "latLng  = " + latLng.toString());
             Log.i("bounds", "bounds.contain = " + BOUNDS.contains(latLng));*/

             isMarkerWithinBounds = true;
        }

        return isMarkerWithinBounds;
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

        for (Map.Entry c : challenges.entrySet()) {
            Challenge challenge = (PictionaryChallenge) c.getValue();
            LatLng cLatLong = new LatLng(challenge.getLatitude(), challenge.getLongitude());
            if (isMarkerWithinBounds(cLatLong)) {
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
    }


}
