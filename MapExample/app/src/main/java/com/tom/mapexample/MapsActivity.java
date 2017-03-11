package com.tom.mapexample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    Location location;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            setupMyLocation();
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney")
                .snippet("雪梨"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        //台北101的位置
        LatLng taipei101 = new LatLng(25.033408, 121.564099);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                taipei101, 15));
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(taipei101)
                .title("101")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble2))
                .snippet("這是台北101"));

//        marker.showInfoWindow();
        mMap.setInfoWindowAdapter(
                new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        View view = getLayoutInflater().inflate(
                                R.layout.info_window, null);
                        TextView title =
                                (TextView) view.findViewById(R.id.info_title);
                        title.setText("Title: "+marker.getTitle());
                        TextView snippet =
                                (TextView) view.findViewById(R.id.info_snippet);
                        snippet.setText(marker.getTitle());
                        return view;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        return null;
                    }
                }
        );
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //連結到撥放器
                Intent intent = getPackageManager().getLaunchIntentForPackage("tcking.github.com.giraffeplayer");
                startActivity(intent);

                //new AlertDialog.Builder(MapsActivity.this)
                        //.setTitle(marker.getTitle())
                        //.setMessage(marker.getSnippet())
                        //.setPositiveButton("OK", null)
                        //.show();
                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 使用者允許權限
                    //noinspection MissingPermission
                    setupMyLocation();
                } else {
                    // 使用者拒絕授權 , 停用 MyLocation 功能
                }
                break;
        }
    }

    private void setupMyLocation() {
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(
                new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {

                        // 透過位置服務，取得目前裝置所在
                        LocationManager locationManager =
                                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        // 設定標準為存取精確
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
                        String provider = locationManager.getBestProvider(criteria, true);
                        //noinspection MissingPermission
                        Location location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            Log.i("LOCATION", location.getLatitude() + "/" +
                                    location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude())
                                    , 15));
                        }
                        return false;
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission

        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (location != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 15));
        }
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("LOCATION", location.getLatitude() + "," +
                    location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 15));
        }
    }

    void buttonOnClick(View view) {

        //連結到camera
        Intent intent = getPackageManager().getLaunchIntentForPackage("net.ossrs.yasea.demo");
        startActivity(intent);

        Toast toast = Toast.makeText(this, "Add a new marker", Toast.LENGTH_SHORT);
        toast.show();

        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // 設定標準為存取精確
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
        String provider = locationManager.getBestProvider(criteria, true);
        //noinspection MissingPermission
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            LatLng Now = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(Now)
                    .title("Current Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Now, 12));
        }

    }
}
