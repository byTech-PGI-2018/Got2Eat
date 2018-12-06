package bytech.got2eat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Restaurantes extends AppCompatActivity{

    MyAdapter adapter;

    public static final String TAG = Restaurantes.class.getSimpleName();
    private FusedLocationProviderClient mFusedLocationClient;
    RecyclerView recyclerView;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    double latitude;
    double longitude;
    double latitudeFinal;
    double longitudeFinal;
    String localFinal;
    boolean alreadyGotData = false;
    private String locationName = "--";
    private List<Address> list;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] permissionsGranted){
        super.onRequestPermissionsResult(requestCode, permissions, permissionsGranted);
        Log.d(TAG, "PASSED THROUGH ONREQUESTPERMISSIONSRESULT\n\n");
        getForecast(); /*Main method*/
        Log.d(TAG, "Main UI thread is running");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_restaurants);

        sharedPreferences = getPreferences(MODE_PRIVATE);

        //Check if coordinates were saved
        Log.i(TAG, "Checking if values were saved");
        if (sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude") && sharedPreferences.contains("locationName")){
            //Get the data then
            latitude = Double.longBitsToDouble(sharedPreferences.getLong("latitude", 0));
            longitude = Double.longBitsToDouble(sharedPreferences.getLong("longitude", 0));
            locationName = sharedPreferences.getString("locationName", "Null");
            Log.i(TAG, "Getting values from SharedPreferences \n(Lat: " + latitude + ", long: " + longitude + ", name: " + locationName + ")");
            alreadyGotData = false;
        }
        else Log.i(TAG, "Values were not saved");

        //Check if permissions were already granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED){
            Log.i(TAG, "No permission yet");
            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            //Wait for the permission request
            onRequestPermissionsResult(1, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, new int[] {1});
        }
        else {
            Log.i(TAG, "Already have necessary permission");
            getForecast();
        }
    }

    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Location permission denied");
        }
        else{
            Log.i(TAG, "Location permission granted");
            //Get location
            try{
                getLocation();
            }catch (SecurityException e) {
                Log.e(TAG, "SecurityException caught: " + e);
            }
        }
    }

    private void getLocation() throws SecurityException{
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final Geocoder geocoder = new Geocoder(this);

        Log.d(TAG, "Getting ready to get last location");

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d(TAG, "Location is: " + location);
                        if (location != null){
                            Log.d(TAG, "getLastLocation() returned location object, lat: "
                                    + String.valueOf(location.getLatitude()) + " lon: "
                                    + String.valueOf(location.getLongitude()));
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            Log.i(TAG, "Latitude was set to: " + latitude);
                            Log.i(TAG, "Longitude was set to: " + longitude);

                            try{
                                list = geocoder.getFromLocation(latitude, longitude, 1);
                                if (list != null & list.size() > 0) {
                                    Log.d(TAG, "Geocoder returned something");
                                    Address address = list.get(0);
                                    if (address.getLocality() != null) locationName = address.getLocality();
                                    Log.d(TAG, "Address found is: " + address.getLocality());
                                    Log.d(TAG, "Location name is: " + locationName);
                                }
                            }catch(IOException e){
                                Log.e(TAG, "Caught IOException: " + e);
                            }
                            latitudeFinal = latitude;
                            longitudeFinal = longitude;
                            localFinal = locationName;
                            HerePlaces h = new HerePlaces();
                            ArrayList<Restaurant> rest = new ArrayList<>();
                            try {
                                rest = h.returnRestaurant(latitude,longitude);

                                /*TextView name = findViewById(R.id.restaurant_name);
                                TextView address = findViewById(R.id.restaurant_address);
                                TextView morada = findViewById(R.id.distance);
                                //View v = findViewById(R.id.view);
                                name.setText(rest.get(0).nome);
                                address.setText(rest.get(0).morada);
                                morada.setText("A cerca de " + rest.get(0).distancia + " metros");*/
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            RecyclerView recyclerView = findViewById(R.id.restaurants);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.setHasFixedSize(true);
                            adapter = new MyAdapter(rest,getApplicationContext());
                            //adapter.setClickListener(getApplicationContext());
                            recyclerView.setAdapter(adapter);


                            alreadyGotData = true;
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "No location could be returned");
                    }
                });
        Log.v(TAG, "Went past getlastlocation");
    }

    private void getForecast() {

        Log.i(TAG, "BEFORE CHECK PERMISSION AND GET LOCATION\n");
        if (!alreadyGotData) checkPermissionAndGetLocation();
        Log.i(TAG, "AFTER CHECK PERMISSION AND GET LOCATION\n");

        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Save values
        Log.i(TAG, "Saving values to SharedPreferences \n(Lat: " + latitude + ", long: " + longitude + ", name: " + locationName + ")");
        editor.putLong("latitude", Double.doubleToRawLongBits(latitude));
        editor.putLong("longitude", Double.doubleToRawLongBits(longitude));
        editor.putString("locationName", locationName);
        editor.apply();

        if(isNetworkAvailable()) {

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) isAvailable = true;
        else{
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        return isAvailable;
    }




}
