package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import br.com.victorpettengill.hawk_eyedcitizen.R;
import br.com.victorpettengill.hawk_eyedcitizen.beans.Problem;
import br.com.victorpettengill.hawk_eyedcitizen.dao.ProblemDao;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private GoogleMap mMap;

    private final int LOCATION_PERMISSION = 11;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private Location currentLocation;
    private final int REQUEST_CREATE_PROBLEM = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                    Log.i("result", "locationButton result");

                    if(locationResult != null) {

                        ProblemDao.getInstance().getProblemsAtBounds();

                        Location location = locationResult.getLastLocation();

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()),
                                13));

                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

                    }

            }
        };


    }

    @OnClick(R.id.fab) void addProblem() {

        Intent i = new Intent(MainActivity.this, RegisterProblemActivity.class);

        if(currentLocation != null) {

            i.putExtra("latitude", currentLocation.getLatitude());
            i.putExtra("longitude", currentLocation.getLongitude());

        }

        startActivityForResult(i, REQUEST_CREATE_PROBLEM);

    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {


            }

        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION);

            }

        } else {

            requestLocation();

        }

    }

    private void addProblemIntheMap(Problem problem, boolean animateTo) {

        Marker marker = mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(problem.getGeoLocation().latitude,problem.getGeoLocation().longitude)
                ).title(problem.getCategory()));

        if(animateTo) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(problem.getGeoLocation().latitude,problem.getGeoLocation().longitude),
                    13));

        }

    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {

        mMap.setMyLocationEnabled(true);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Log.i("fused", "sucesss");

                if(location != null) {

                    currentLocation = location;

                    ProblemDao.getInstance().getProblemsAtBounds();

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            13));
                } else {

                    Log.i("fused", "sucesss - null");

                    LocationRequest mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(10000);
                    mLocationRequest.setFastestInterval(0);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, null);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.e("fused", "error", e);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == LOCATION_PERMISSION && grantResults.length > 0) {

            requestLocation();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CREATE_PROBLEM) {

            addProblemIntheMap((Problem) data.getParcelableExtra("problem"), true);

        }

    }

}
