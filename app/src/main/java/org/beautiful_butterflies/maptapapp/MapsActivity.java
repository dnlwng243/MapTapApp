package org.beautiful_butterflies.maptapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText editSearch;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private static final float MY_LOC_ZOOM_FACTOR = 15f;
    private boolean isTracking = false;

    private Location loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        editSearch = (EditText) findViewById(R.id.editText_Search);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng home = new LatLng(32.927470, -117.202985);
        mMap.addMarker(new MarkerOptions().position(home).title("Location of Birth"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));


    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled)
                Log.d("MapsActivity", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled)
                Log.d("MapsActivity", "getLocation: Network is enabled");

            if (!isGPSenabled && !isNetworkEnabled)
                Log.d("MapsActivity", "getLocation: No Provider is enabled");
            else {
                canGetLocation = true;
                if (isGPSenabled) {
                    Log.d("MapsActivity", "getLocation: GPS enabled, requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);
                    Toast.makeText(this, "GPS Enabled, requesting location updates", Toast
                            .LENGTH_SHORT).show();
                    ;
                }
                if (isNetworkEnabled) {
                    Log.d("MapsActivity", "getLocation: Network enabled, requesting location " +
                            "updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Toast.makeText(this, "Network enabled, requesting location updates", Toast
                            .LENGTH_SHORT).show();
                    ;

                }
            }
        } catch (Exception e) {

            Log.d("MapsActivity", "Exception in getLocation:" + e.toString());

        }
    }
        android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dropMarker(location);
                loc = location;
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(locationListenerNetwork);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

                switch (status) {

                    case LocationProvider.AVAILABLE:
                        Log.d("MapsActivity", "Location Provider is available");

                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.d("MapsActivity", "Location Provider is temporarily unavailable");
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.d("MapsActivity", "Location Provider is out of service");
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                        break;
                    default:
                        Log.d("MapsActivity", "Default");
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                        break;

                }

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

        android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dropMarker(location);
                loc = location;
                Log.d("MapsActivity", "locationListenerNetwork: Location has Changed");
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(locationListenerNetwork);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    public void dropMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        if (isGPSenabled) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory
                    .HUE_CYAN));
            Log.d("MapsActivity", "GPS Marker Dropped");
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory
                    .HUE_MAGENTA));
            Log.d("MapsActivity", "Network Marker Dropped" + location.getAccuracy());

        }
        mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MY_LOC_ZOOM_FACTOR));
    }

    public void trackMe(View view) {
        if (isTracking) {
            isTracking = false;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            Log.d("MapsActivity", "Tracking Disabled");
            Toast.makeText(this.getApplicationContext(), "Tracking Disabled", Toast.LENGTH_SHORT)
                    .show();;
        }
        else {
            getLocation();
            isTracking = true;
            Log.d("MapsActivity", "Tracking Enabled");
            Toast.makeText(this.getApplicationContext(), "Tracking Enabled", Toast.LENGTH_SHORT)
                    .show();;

        }
    }
    public void searchPOI(View view) throws IOException {
        Geocoder myGeo = new Geocoder(this.getApplicationContext());

        if(loc != null && editSearch.getText() !=null) {
            List<Address> holder = myGeo.getFromLocationName(editSearch.getText().toString(), 3, loc.getLatitude() - .07246, loc.getLongitude() - .07246, loc.getLatitude() + .07246, loc.getLongitude() + .07246);
            for (int i = 0; i < holder.size(); i++) {
                LatLng poi = new LatLng(holder.get(i).getLatitude(), holder.get(i).getLongitude());
                mMap.addMarker(new MarkerOptions().position(poi).title(holder.get(i).getAddressLine(0)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi, MY_LOC_ZOOM_FACTOR));

            }
            Toast.makeText(this.getApplicationContext(), "Search Completed; Markers added", Toast.LENGTH_SHORT).show();
        }
    }
    public void toggle(View view) {
        if (mMap.getMapType() == 1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }
    public void clear(View view){
        mMap.clear();
        LatLng birthplace = new LatLng(32.7157, -117.1611);
        mMap.addMarker(new MarkerOptions().position(birthplace).title("Born here"));
    }
}