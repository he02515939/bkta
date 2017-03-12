package com.tom.mapexample;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    String id = "id";

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

    //手機旋轉不重開
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
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

    public void b(View view) {

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
        Location location = locationManager.getLastKnownLocation("network");
        if (location != null) {
            LatLng Now = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(Now)
                    .title("Current Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Now, 12));
        }
        else {
            Log.d("SOCO", "COCO95");
        }

        String url = "rtmp://140.115.158.81:1935/live/123";
        int random = (int) (Math.random()*100);
        String rand = Integer.toString(random);
        String method="register";
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        BackgroundTask backgroundTask=new BackgroundTask(this);
        backgroundTask.execute(method,id,rand,longitude,latitude,url);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作

        //連結到camera
        Intent intent = getPackageManager().getLaunchIntentForPackage("net.ossrs.yasea.demo");
        intent.putExtra("url", url);
        startActivity(intent);
    }

    public void sync_b(View view) {


//
        TransTask transTask=new TransTask();
        transTask.execute();//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作

    }

    //解析JSON資料
    private void parseJSON(String s) {
        ArrayList<Transaction> trans = new ArrayList<>();
        try {
            Log.d("COCO", "COCO1");
            JSONArray array = new JSONArray(s);
            for (int i = 0; i < array.length(); i++) {
                Log.d("COCO", "COCO3");
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("id");
                String rand = obj.getString("rand");
                String latitude = obj.getString("latitude");
                String longitude = obj.getString("longitude");
                String url = obj.getString("url");
                Log.d("COCO", "COCO4");

                double latitude_in= Double.parseDouble(latitude);
                double longitude_in= Double.parseDouble(longitude);
                Log.d("COCO1", "COCO5");
                LatLng Now = new LatLng(latitude_in,longitude_in);
                Log.d("COCO1", "COCO6");
                mMap.addMarker(new MarkerOptions()
                        .position(Now)
                        .title(id));

                Log.d("COCO", id + "/" + rand + "/" + latitude + "/" + longitude + "/" + url);
                Transaction t = new Transaction(id, rand, latitude, longitude, url);
                trans.add(t);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("COCO", "COCO2");
    }
    public static String getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        Log.d("HIHI", "SSS");
        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);
        Log.d("HIHI", "SSS1");
        urlConnection.connect();
        Log.d("HIHI", "SSS2");
        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
        Log.d("HIHI", "SSS3");
        char[] buffer = new char[1024];
        Log.d("HIHI", "SSS4");
        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
            Log.d("HIHI", line);
        }
        br.close();

        return sb.toString();

    }

    class TransTask extends AsyncTask<Void, Void, String> {

        String json_url="http://192.168.2.59/project/getjson.php";
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            try {
                 result = getJSONObjectFromURL(json_url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
           return result;
        }

        public TransTask(){super();}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            parseJSON(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }
}
