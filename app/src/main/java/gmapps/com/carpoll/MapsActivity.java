package gmapps.com.carpoll;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pubnub.api.*;
import com.yayandroid.locationmanager.LocationBaseActivity;
import com.yayandroid.locationmanager.LocationConfiguration;
import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.constants.FailType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import android.view.Window;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends LocationBaseActivity implements OnMapReadyCallback {

    private static final String TAG ="MapsActivity" ;
    private static final String PUBNUB_TAG ="PubNub Acitivity" ;
    private GoogleMap mMap;
    private Pubnub mPubnub;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;
    private LatLng mLatLngPresent=new LatLng(17.2231, 78.2827);;
    private LocationRequest mLocationRequest;
    private ProgressDialog progressDialog;
    private TextView locationText;
    private  String userType;
    private  String userUID;
    private  String placeID;
    private  String[] channelType=new String[2];
    private  Bitmap iconBitmap;
    HashMap<String,JSONObject> h = new HashMap<String,JSONObject>();
    HashMap<String, LatLng> h1 = new HashMap<>();
    private String userLatLang;
    private Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        userUID=getIntent().getStringExtra("id");
        userType=getIntent().getStringExtra("typeUser");
        placeID=getIntent().getStringExtra("placeId");
        userLatLang= getIntent().getStringExtra("latLang");
        Log.e("latlang",userLatLang);

        channelType=getSubscribeChannel(userType);
        mPubnub = new Pubnub("YOUR-PUBNUB-API-KEY");
        Log.e("sub channel",channelType[0]);
        Log.e("pub channel",channelType[1]);

        locationText = (TextView) findViewById(R.id.locationText);

        LocationManager.setLogType(LogType.GENERAL);
        getLocation();
        LocationManager.setLogType(LogType.IMPORTANT);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private String[] getSubscribeChannel(String userType) {
        String[] chanelType = new String[2];
        switch (userType){
            case "Lift Seeker":
                chanelType[0]="giverChannel";
                chanelType[1]="seekerChannel";
                iconBitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.car);
                break;
            case "Lift Giver":
                chanelType[0]="seekerChannel";
                chanelType[1]="giverChannel";
                iconBitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.man);
                break;

        }
                return chanelType;
    }

    Callback subscribeCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            Log.e("sub",jsonMessage.toString());

            try {
                JSONObject totalOBJ = null;

                 totalOBJ=jsonMessage.getJSONObject("location");
                String userGetUID=jsonMessage.getString("UID");
                LatLng subLatLang =new LatLng(totalOBJ.getDouble("lat"), totalOBJ.getDouble("lng"));
                h1.remove(userGetUID);
                h1.put(userGetUID,subLatLang);
                Log.e("hashmap id",userGetUID);
                updateMarker(h1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    Callback publisherCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            JSONArray jsonMessage = (JSONArray) message;
            Log.e("pub",jsonMessage.toString());
//            Log.e(TAG,"publish call back"+jsonMessage);
//            try {
//                double mLat = jsonMessage.getDouble("lat");
//                double mLng = jsonMessage.getDouble("lng");
//                Log.e(PUBNUB_TAG, String.valueOf(mLat));
//                Log.e(PUBNUB_TAG, String.valueOf(mLng));
//                mLatLng = new LatLng(mLat, mLng);
//            } catch (JSONException e) {
//                Log.e(TAG, e.toString());
//            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    updatePolyline();
                    updateCameraPresent();
                    updateMarkerPresent();
                }
            });
        }
    };
    private void updatePolyline() {
        mMap.clear();
        mMap.addPolyline(mPolylineOptions.add(mLatLng));
    }
    private void updateMarker(HashMap<String, LatLng> newhh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(mLatLngPresent).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).flat(true));
            }
        });
        //subMarker = mMap.addMarker(new MarkerOptions().position(subLatLang).icon(BitmapDescriptorFactory.fromResource(R.drawable.carpng)).flat(true));
        for (Object o : newhh.entrySet()) {
            final Map.Entry pair = (Map.Entry) o;
            Log.e("dssd",pair.getKey() + " = " + pair.getValue());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(userType.equals("Lift Seeker")){
                        mMap.addMarker(new MarkerOptions().position((LatLng) pair.getValue()).icon(BitmapDescriptorFactory.fromResource(R.drawable.carpng)).flat(true));
//             marker = mMap.addMarker(new MarkerOptions().position(mLatLngPresent).icon(BitmapDescriptorFactory.fromResource(R.drawable.manpng)).flat(true));
                               }
                    else if(userType.equals("Lift Giver")){
                        mMap.addMarker(new MarkerOptions().position((LatLng) pair.getValue()).icon(BitmapDescriptorFactory.fromResource(R.drawable.manpng)).flat(true));

                    }

                }
            });
        }


    }
    private void updateCameraPresent() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLngPresent, 16));
    }
    private void updateMarkerPresent() {
        marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(mLatLngPresent).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).flat(true));


    }
    private void updateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
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
        LatLng sydney = new LatLng(17.2231, 78.2827);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Hyderbbad"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        initializeMap();
    }
    private void initializeMap() {
        try {
            mPubnub.subscribe(channelType[0], subscribeCallback);
        } catch (PubnubException e) {
            Log.e(TAG, e.toString());
        }
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
    }
    @Override
    public LocationConfiguration getLocationConfiguration() {
        //Criteria criteria = new Criteria();
        return new LocationConfiguration()
                .keepTracking(true)
                .useOnlyGPServices(false)
                .askForGooglePlayServices(true)
                .askForSettingsApi(true)
                .failOnConnectionSuspended(true)
                .failOnSettingsApiSuspended(false)
                .doNotUseGooglePlayServices(false)
                .askForEnableGPS(true)
                .setMinAccuracy(Criteria.ACCURACY_HIGH)
                .setWithinTimePeriod(60 * 1000)
                .setTimeInterval(500)
               // .setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 5 * 1000)
                .setWaitPeriod(ProviderType.GPS, 30 * 1000)
                //.setWaitPeriod(ProviderType.NETWORK, 10 * 1000)
                .setGPSMessage("Would you mind to turn GPS on?")
                .setRationalMessage("Gimme the permission!");
    }

    @Override
    public void onLocationChanged(Location location) {
        dismissProgress();

        setText(location);
    }

    @Override
    public void onLocationFailed(int failType) {
        dismissProgress();
        Log.e(TAG,"location Failed:" +failType);
        switch (failType) {
            case FailType.PERMISSION_DENIED: {
                locationText.setText("Couldn't get location, because user didn't give permission!");
                break;
            }
            case FailType.GP_SERVICES_NOT_AVAILABLE:
            case FailType.GP_SERVICES_CONNECTION_FAIL: {
                locationText.setText("Couldn't get location, because Google Play Services not available!");
                break;
            }
            case FailType.NETWORK_NOT_AVAILABLE: {
                locationText.setText("Couldn't get location, because network is not accessible!");
                break;
            }
            case FailType.TIMEOUT: {
                locationText.setText("Couldn't get location, and timeout!");
                break;
            }
            case FailType.GP_SERVICES_SETTINGS_DENIED: {
                locationText.setText("Couldn't get location, because user didn't activate providers via settingsApi!");
                break;
            }
            case FailType.GP_SERVICES_SETTINGS_DIALOG: {
                locationText.setText("Couldn't display settingsApi dialog!");
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mPubnub.subscribe(channelType[0],subscribeCallback);
        } catch (PubnubException e) {
            e.printStackTrace();
        }
        if (getLocationManager().isWaitingForLocation()
                && !getLocationManager().isAnyDialogShowing()) {
            displayProgress();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPubnub.unsubscribe(channelType[0]);
        dismissProgress();
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(true);
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setText(Location location) {
        JSONObject jsonobj=new JSONObject();
        JSONObject Totaljsonobj=new JSONObject();

        try {

            jsonobj.put("lat",location.getLatitude());
            jsonobj.put("lng",location.getLongitude());
            Totaljsonobj.put("UID",userUID);
            Totaljsonobj.put("location",jsonobj);
            mLatLngPresent = new LatLng(location.getLatitude(), location.getLongitude());
            mPubnub.publish(channelType[1],Totaljsonobj,publisherCallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String appendValue = location.getLatitude() + ", " + location.getLongitude() + "\n";
        String newValue;
        CharSequence current = locationText.getText();

        if (!TextUtils.isEmpty(current)) {
            //newValue = current + appendValue;
            newValue = current+"";
        } else {
            newValue = appendValue;
        }

        locationText.setText(newValue);
    }

}
