package com.example.sketch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class LocationService extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private  MapView mapView;
    private LocationManager manager;
    private  float latitude;
    private  float longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);

        setContentView(R.layout.activity_location_service);

        mapView = findViewById(R.id.map);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);
        int canvasId = sharedPref.getInt("canvasId", 0);

        DatabaseOperations operate = new DatabaseOperations(this);

        List<User> allUsers = operate.getDataBaseUsers();

        User current_user;
        DrawingView userCanvas = null;

        SearchView searchBar = findViewById(R.id.searchBar);
        Button recnterButton = findViewById(R.id.recenterButton);
        Button setLocation = findViewById(R.id.locationSetButton);
        ImageView gobackArrow = findViewById(R.id.gobackview);

        gobackArrow.setOnClickListener(goback -> {
            Intent intent = new Intent(LocationService.this, MainActivity.class);
            startActivity(intent);
        });



        GeoPoint point = null;
        double[] cordinates = {34.1862,-103.3344};


        if (username != null && password != null) {
            for (User user : allUsers) {
                if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {

                    for (DrawingView canvas : user.getMyCanavas()) {
                        if (canvas.getCanvasid() == canvasId) {
                            userCanvas = canvas;


                            Marker mark = new Marker(mapView);

                            float latitude = canvas.getLatitude();
                            float longitude = canvas.getLongitude();

                            if (latitude != 0 || longitude != 0) {
                                point = new GeoPoint(latitude, longitude);
                                mark.setPosition(point);
                            }else {

                                point = new GeoPoint(cordinates[0], cordinates[1]);//defualt for now
                                mark.setPosition(point);
                            }
                                mark.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                String canvasTitle = canvas.getTitle();
                                mark.setTitle(canvasTitle);
                                mapView.getController().setCenter(point);
                                // Set a drawable resource
                                Drawable drawable = new BitmapDrawable(canvas.getFullcanvas());
                                mark.setImage(drawable); // Set the image
                                mapView.setTileSource(TileSourceFactory.MAPNIK);
                                mapView.setBuiltInZoomControls(true);
                                mapView.setMultiTouchControls(true);

                                mapView.getController().setZoom(17);
                                mapView.getOverlays().add(mark); // only show the one we want to edit
                                mapView.invalidate();

                        }
                    }
                    current_user = user;

                    // Check if there is a profile picture
                    if (user.getProfilepic() != null && user.getProfilepic().length > 0) {
                        // Use ViewTreeObserver to ensure the layout is done before getting width/height

                    }
                }
            }
        }
        DrawingView finalUserCanvas = userCanvas;

        setLocation.setOnClickListener(local ->{

            if(latitude != 0 || longitude != 0){
                operate.addLocation(latitude,longitude, finalUserCanvas.getCanvasid());
                Intent intent = new Intent(LocationService.this,MainActivity.class);
                startActivity(intent);
            }

        });

        GeoPoint finalPoint = point;
        recnterButton.setOnClickListener(center ->{
            try {
                // canter back to the orignal location if there is one
                mapView.getController().setCenter(finalPoint);
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }
        });


        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapView.getController().setZoom(17);



        checkLocationPermission();

        Marker markAdder = new Marker(mapView);


        mapView.setOnTouchListener((v, event) -> {
            GeoPoint geoPoint = (GeoPoint)
            mapView.getProjection().fromPixels((int)
            event.getX(), (int) event.getY());

            markAdder.setPosition(geoPoint);
            markAdder.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            String canvasTitle = finalUserCanvas.getTitle();
            markAdder.setTitle(canvasTitle);
            mapView.getController().setCenter(geoPoint);
            // Set a drawable resource
            Drawable drawable = new BitmapDrawable(finalUserCanvas.getFullcanvas());
            markAdder.setImage(drawable); // Set the image

             latitude = (float) geoPoint.getLatitude();
            longitude = (float)  geoPoint.getLongitude();

            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            mapView.getOverlays().add(markAdder);
            mapView.invalidate();

            Toast.makeText(this, "Location selected: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();


            return false; });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE ) {
//            if (resultCode == RESULT_OK) {
//                Place place = Autocomplete.getPlaceFromIntent(data);
//                GeoPoint selectedPoint = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
//
//                // Add marker at selected place
//                Marker marker = new Marker(mapView);
//                marker.setPosition(selectedPoint);
//                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            //    marker.setTitle(place.getName());
//                mapView.getOverlays().add(marker);
//                mapView.getController().setCenter(selectedPoint);
//                mapView.invalidate();
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }


    private void checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Enable map view
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Disable map view
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach(); // Clean up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle location-related tasks here
                Log.d("LocationService", "Location permission granted.");
            } else {
                // Permission denied, handle accordingly
                Log.d("LocationService", "Location permission denied.");
            }
        }
    }
}
