package bytech.got2eat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Restaurantes extends AppCompatActivity{

    RestaurantsAdapter adapter;

    public static final String TAG = "Restaurantes";
    private FusedLocationProviderClient mFusedLocationClient;

    double latitude;
    double longitude;
    double latitudeFinal;
    double longitudeFinal;
    String localFinal;
    boolean alreadyGotData = false;
    private String locationName = "--";
    private List<Address> list;
    private LottieAnimationView animationView;
    private TextView noRecipesText;
    private ArrayList<Restaurant> rest;
    private RecyclerView recyclerView;
    private HerePlaces h;

    private final int REQUEST_PERMISSION_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] permissionsGranted){
        //super.onRequestPermissionsResult(requestCode, permissions, permissionsGranted);

        switch (requestCode){
            case REQUEST_PERMISSION_LOCATION:
                if (permissionsGranted.length > 0 && permissionsGranted[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }
                else if (permissionsGranted.length > 0 && permissionsGranted[0] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, getString(R.string.refuse_location), Toast.LENGTH_LONG).show();
                    finish();
                }
                else Log.d(TAG, "Something else");
                break;
            default:
                Log.e(TAG, "Unrecognized request code");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_restaurants);

        //Check if permissions were already granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED){
            Log.i(TAG, "No permission yet");
            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);

            //Wait for the permission request
            onRequestPermissionsResult(REQUEST_PERMISSION_LOCATION, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, new int[] {1});
        }
        else {
            Log.i(TAG, "Already have necessary permission");
            getLocation();
        }
    }

    private void getLocation() throws SecurityException{
        Log.d(TAG, "Getting mFusedLocationClient");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final Geocoder geocoder = new Geocoder(this);
        Log.d(TAG, "Got mFusedLocationClient");

        Log.d(TAG, "Getting ready to get last location");

        animationView = findViewById(R.id.animation_view);
        noRecipesText = findViewById(R.id.no_recipes_here);
        noRecipesText.setVisibility(View.GONE);

        animationView.setRepeatCount(ValueAnimator.INFINITE);
        animationView.playAnimation();

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
                            h = new HerePlaces();
                            recyclerView = findViewById(R.id.restaurants);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.setHasFixedSize(true);
                            rest = new ArrayList<>();
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Log.d(TAG, "Getting ready to returnRestaurant");
                                        rest = h.returnRestaurant(latitude,longitude);
                                        Log.d(TAG, "Got all restaurants");

                                        adapter = new RestaurantsAdapter(rest,getApplicationContext());
                                        //adapter.setClickListener(getApplicationContext());

                                        recyclerView.setAdapter(adapter);
                                        alreadyGotData = true;

                                        if (!rest.isEmpty()){
                                            animationView.pauseAnimation();
                                            animationView.setVisibility(View.GONE);
                                        }
                                        else{
                                            animationView.pauseAnimation();
                                            animationView.setVisibility(View.GONE);
                                            noRecipesText.setText(R.string.no_saved_recipes);
                                            noRecipesText.setVisibility(View.VISIBLE);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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
}
