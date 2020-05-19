package creativeuiux.loginuikit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.koushikdutta.ion.Ion;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    Location mLastLocation;
    Marker mCurrentMarker;
    Location homeLocation;

    public static String url;
    public static String email;
    public static int themeValue;
    public static int distanceValue;
    BottomNavigationView bottomNavigationView;
    Bitmap bmImg;
    BitmapDrawable icon;
    public static String name;
    FirebaseAuth mAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        bottomNavigationView = findViewById(R.id.navigation);

        final EditText cor = (EditText) findViewById(R.id.cor);
        final EditText cor2 = (EditText) findViewById(R.id.cor2);
        Button home = (Button) findViewById(R.id.set_home);

        setBitmap();

     final CoordinateView coordinateView= ViewModelProviders.of(this).get(CoordinateView.class);

     coordinateView.getLatitude().observe(this, new Observer<Double>() {
         @Override
         public void onChanged(Double aDouble) {
             cor.setText(String.valueOf(aDouble));
         }
     });

     coordinateView.getLongitude().observe(this, new Observer<Double>() {
         @Override
         public void onChanged(Double aDouble) {
             cor2.setText(String.valueOf(aDouble));
         }
     });



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {

                    case R.id.nav_home:
                        break;

                    case R.id.nav_person:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        builder.setTitle(name)
                                .setMessage("E-mail :" + email)
                                .setIcon(icon)
                                .setCancelable(true).setNeutralButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                logOut();

                            }
                        })
                                .show();
                        break;
                    case R.id.nav_settings:
                        settingDialog();
                        break;


                }


                return true;
            }
        });


        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                homeLocation = mLastLocation;
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("Succesful request");
                builder.setMessage(" Your location has been set.");
                builder.setCancelable(true);
                builder.show();


            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    mLastLocation = location;


                    if (mLastLocation != null && homeLocation != null) {

                        distancecheck(location.getAccuracy());
                    }


                    if (mCurrentMarker != null) {
                        mCurrentMarker.remove();

                    }


                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
                        String s = addressList.get(0).getLocality();


                        try {


                            mCurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(s)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bmImg)));


                        } catch (Exception e) {

                            mCurrentMarker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(s));

                            e.printStackTrace();
                        }



                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.2f));

                        Double cord = mLastLocation.getLatitude();
                        Double cord2 = mLastLocation.getLongitude();
                        coordinateView.setLatitude(cord);
                        coordinateView.setLongitude(cord2);




                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    mLastLocation = location;

                    if (mLastLocation != null && homeLocation != null) {

                        distancecheck(location.getAccuracy());
                    }


                    if (mCurrentMarker != null) {
                        mCurrentMarker.remove();

                    }


                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
                        String s = addressList.get(0).getLocality();



                        try {
                            Bitmap bmImg = Ion.with(MapActivity.this)
                                    .load(url).asBitmap().get();

                            mCurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(s)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bmImg)));



                        } catch (Exception e) {

                            mCurrentMarker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(s));

                            e.printStackTrace();
                        }


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.2f));

                        Double cord = mLastLocation.getLatitude();
                        Double cord2 = mLastLocation.getLongitude();
                        coordinateView.setLatitude(cord);
                        coordinateView.setLongitude(cord2);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });


        }


    }

    private void logOut() {

        FirebaseUser user = mAuth.getInstance().getCurrentUser();

        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    final Intent intent = new Intent(MapActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {

                    Log.d("11", "logout failed.");
                }
            }
        });


    }

    private void setBitmap() {


        try {
            bmImg = Ion.with(MapActivity.this)
                    .load(url).asBitmap().get();

            Resources res = getResources();
            icon = new BitmapDrawable(res, bmImg);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void distancecheck(float accuracy) {

        float distanceInMeters = mLastLocation.distanceTo(homeLocation);
        float dialogDistance =distanceInMeters;

        /*
         - emin olmak için en son lokasyonun sapme değerini (accuracy)  mesafe değerinden çıkarıyorum.

          - Daha iyi sonuç için set edilen başlangıç noktasınında sapma değeri de dikkate alınabilirdi

          - kullanıcıyı uğraştırmamak için o kısmı eklemedim ama eğer yapılmak istenirse
          set as home coordinates butonunu basıldığında eğer sapma değerim örneğin 10 dan küçükse set et yoksa etme.



        *

        /
         */
        distanceInMeters -= accuracy;


        if (distanceInMeters > distanceValue) {

            int dist= (int) distanceInMeters;

            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("GO BACK !");
            builder.setMessage("distance to your starting location  :" + dialogDistance);
            builder.setCancelable(true);
            builder.show();


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("Welcome " + name);
        builder.setMessage("Make sure Your location service is enabled.");
        builder.setCancelable(true);
        builder.show();


        //theme value comes from firebase remote config
        if (themeValue == 2) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    MapActivity.this, R.raw.dark));

        }


        mMap.getUiSettings().setMyLocationButtonEnabled(false);


    }

    private GoogleMap setMap(GoogleMap map) {


        if (themeValue == 2) {

            try {

                themeValue = 1;
                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.light));
                if (!success) {
                    Log.e("23", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("24", "Can't find style. Error: ", e);
            }


        } else if (themeValue == 1) {

            try {
                themeValue = 2;
                boolean success = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.dark));
                if (!success) {
                    Log.d("25", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.d("26", "Can't find style. Error: ", e);
            }


        }

        return map;
    }


    private void settingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MapActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Do you want to change the theme?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        mMap = setMap(mMap);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


}
