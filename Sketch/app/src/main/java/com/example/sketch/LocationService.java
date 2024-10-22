package com.example.sketch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationService extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView mapView;
    private GoogleMap googleMap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set the content view
        setContentView(R.layout.activity_location_service); // Ensure you have a layout file

        // Initialize MapView
        mapView = findViewById(R.id.mapView); // Ensure this matches your layout

        // Important: Call onCreate for the MapView
        mapView.onCreate(savedInstanceState);

        Button selectLocationButton = findViewById(R.id.selectLocationButton);
        selectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleMap != null) {
                    LatLng selectedLocation = googleMap.getCameraPosition().target; // Get the selected location
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", selectedLocation.latitude);
                    resultIntent.putExtra("longitude", selectedLocation.longitude);
                    setResult(Activity.RESULT_OK, resultIntent);
                   // finish(); // Close the activity and return to the previous one
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        // Check and request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true); // Enable user location on the map
            // You can also set the initial camera position here
            LatLng defaultLocation = new LatLng(-34, 151); // Example coordinates
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        }

        // Add a marker and move the camera
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear(); // Clear previous markers
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                Log.d("LocationService", "Location permission denied");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}
