package com.example.sketch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class LocationView extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private  MapView mapView;
    private LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);

        setContentView(R.layout.activity_location_service);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = findViewById(R.id.map);

        SharedPreferences sharedPref = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);
        int canvasId = sharedPref.getInt("canvasId", 0);

        DatabaseOperations operate = new DatabaseOperations(this);

        List<User> allUsers = operate.getDataBaseUsers();

        User current_user;
        DrawingView userCanvas = null;
        GeoPoint point = null;

        SearchView searchBar = findViewById(R.id.searchBar);
        Button recnterButton = findViewById(R.id.recenterButton);
        Button setLocation = findViewById(R.id.locationSetButton);
        LinearLayout layout = findViewById(R.id.locationLayout);
        ImageView gobackArrow = findViewById(R.id.gobackview);

        layout.setVisibility(View.GONE);
        setLocation.setVisibility(View.GONE);

        setLocation.setText("Change Location");
        setLocation.setOnClickListener(change_screen ->{

            Intent intent = new Intent(LocationView.this,LocationService.class);
            startActivity(intent);

        });

        gobackArrow.setOnClickListener(goback -> {
            Intent intent = new Intent(LocationView.this, MainActivity.class);
            startActivity(intent);
        });


        double[] cordinates = {34.1862,-103.3344};

        List<DrawingView> allCanvas = new ArrayList<>();
        List<String> suggestions  = new ArrayList<>();

        List<Marker> markers = new ArrayList<>();
        if (username != null && password != null) {
            for (User user : allUsers) {
                assert allCanvas != null;
                    allCanvas.addAll(user.getMyCanavas());

                if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {

                    for (DrawingView canvas : user.getMyCanavas()) {
                        Marker mark = new Marker(mapView);

                        float latitude = canvas.getLatitude();
                        float longitude = canvas.getLongitude();

                        if (latitude != 0 || longitude != 0) {
                             point = new GeoPoint(latitude,longitude);
                            mark.setPosition(point);


                            mark.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            String canvasTitle = canvas.getTitle();
                            suggestions.add(canvasTitle);
                            mark.setTitle(canvasTitle);
                            mapView.getController().setCenter(point);
                            // Set a drawable resource
                            Drawable drawable = new BitmapDrawable(canvas.getFullcanvas());
                            mark.setImage(drawable); // Set the image

                            //only the canvases with cordinates will be shown
                            markers.add(mark);
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

        MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "suggestion"});
        for (int i = 0; i < suggestions.size(); i++) {
            cursor.addRow(new Object[]{i, suggestions.get(i)});
        }

        SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                0
        );

        searchBar.setSuggestionsAdapter(suggestionAdapter);
        searchBar.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {

                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor cursor = (Cursor) suggestionAdapter.getItem(i);
                String suggestion = cursor.getString(cursor.getColumnIndex("suggestion"));
                searchBar.setQuery(suggestion, true); // Set selected suggestion to the query
                // get the suggestion linked to the canvas
                DrawingView canvas  = allCanvas.get(i);

                GeoPoint point = new GeoPoint(canvas.getLatitude(),canvas.getLongitude());

                mapView.getController().setCenter(point); // direct them to the location
                return true;
            }
        });


        GeoPoint finalPoint = point;
        recnterButton.setOnClickListener(center ->{
            try {

                mapView.getController().setCenter(finalPoint);
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }
        });

//        Marker testmark = markers.get(0);
//        GestureDetector gestureDetector;
//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public void onLongPress(MotionEvent e) {
//                // Perform action on marker hold
//                boolean lomngpressed =  testmark.onLongPress(e,mapView);
//                Log.d("Long preesed","pressed for a long time");
//
//                // Action for holding the marker
//                if(lomngpressed) {
//
//                    Toast.makeText(LocationView.this, "Marker Held!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapView.getController().setZoom(17);
        mapView.getOverlays().addAll(markers);
        mapView.invalidate();

        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Add a marker at the specified location
                Marker marker = new Marker(mapView);
               // marker.setPosition(startPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
;

                mapView.getOverlays().add(marker);
                mapView.invalidate();
            }
        });
        checkLocationPermission();

//        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
//            @Override
//            public void onLocationChanged(@NonNull Location location) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//
//                GeoPoint userLocation = new GeoPoint(35.0844, -106.6504); //ABQ for example
//                mapView.getController().setCenter(userLocation);
//                mapView.getController().setZoom(15);
//
//
//                // Add a marker to represent the user's location
//                Marker userMarker = new Marker(mapView);
//                userMarker.setPosition(userLocation);
//                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//               // userMarker.setTitle("You are here");
//              //  mapView.getOverlays().add(userMarker);
//
//                manager.removeUpdates(this);
//
//                mapView.invalidate();
//            }
//
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//                Toast.makeText(LocationService.this, "Please enable GPS", Toast.LENGTH_SHORT).show();
//            }
//        });



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
