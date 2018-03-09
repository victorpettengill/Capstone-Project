package br.com.victorpettengill.hawk_eyedcitizen.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import br.com.victorpettengill.hawk_eyedcitizen.R;

public class FindLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "FindLocation";
    private Marker marker;

    private double latitude;
    private double longitude;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;

                location = place.getAddress().toString();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        place.getLatLng(),
                        13));

                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getAddress().toString()));
                marker.setDraggable(true);

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error has occured: " + status);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.save) {

            Intent i = new Intent();
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("locationButton", location);

            setResult(RESULT_OK, i);
            finish();

        } else {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_done, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                latitude = marker.getPosition().latitude;
                longitude = marker.getPosition().longitude;

                new AddressTask().execute(marker.getPosition().latitude, marker.getPosition().longitude);

            }
        });

    }

    private class AddressTask extends AsyncTask<Double, Void, List<Address>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Address> doInBackground(Double... doubles) {

            Geocoder geocoder = new Geocoder(FindLocationActivity.this);

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(doubles[0], doubles[1], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);

            if(addresses != null && addresses.size() > 0) {

                StringBuilder builder = new StringBuilder();

                for(int i=0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                    builder.append(addresses.get(0).getAddressLine(i));
                }

                marker.setTitle(builder.toString());
                location = builder.toString();
                marker.showInfoWindow();

            }

        }

    }

}
