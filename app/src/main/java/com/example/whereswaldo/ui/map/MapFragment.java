package com.example.whereswaldo.ui.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.whereswaldo.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MapFragment extends Fragment {

    GoogleMap mGoogleMap;
    DatabaseReference coordinatesRef; // Firebase reference

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        // Get the Firebase coordinates reference
        coordinatesRef = FirebaseDatabase.getInstance().getReference();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getContext());
                mGoogleMap = googleMap;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                // Fetch coordinates from Firebase and add markers on the map
                coordinatesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        googleMap.clear(); // Clear existing markers
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            double latitude = snapshot.child("Latitude").getValue(Double.class);
                            double longitude = snapshot.child("Longitude").getValue(Double.class);
                            String title = snapshot.child("Name").getValue(String.class);
                            LatLng position = new LatLng(latitude, longitude);

                            googleMap.addMarker(new MarkerOptions().position(position).title(title));
                            boundsBuilder.include(position);
                        }

                        LatLngBounds bounds = boundsBuilder.build();
                        int padding = 50; // Adjust the padding as needed

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        googleMap.animateCamera(cameraUpdate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error if needed
                    }
                });

                // Set marker click listener
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        LatLng position = marker.getPosition();
                        double latitude = position.latitude;
                        double longitude = position.longitude;

                        // Query the Firebase database based on the latitude and longitude
                        DatabaseReference coordinatesRef = FirebaseDatabase.getInstance().getReference();
                        Query query = coordinatesRef.orderByChild("Latitude").equalTo(latitude);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String location = snapshot.child("Location").getValue(String.class);
                                    String name = snapshot.child("Name").getValue(String.class);
                                    String details = snapshot.child("Details").getValue(String.class);
                                    String artist = snapshot.child("Artist").getValue(String.class);

                                    // Create a BottomSheetDialog
                                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                                    // Inflate the layout for the bottom sheet
                                    View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_landmark, null);

                                    // Set the title of the location in the TextView
                                    TextView locationNameTextView = bottomSheetView.findViewById(R.id.location_name);
                                    locationNameTextView.setText(name);

                                    // Set the location, artist, and details in the TextViews
                                    TextView locationTextView = bottomSheetView.findViewById(R.id.location);
                                    locationTextView.setText(location != null ? location : "");

                                    TextView artistTextView = bottomSheetView.findViewById(R.id.artist);
                                    artistTextView.setText(artist); // Set the artist value if applicable

                                    TextView detailsTextView = bottomSheetView.findViewById(R.id.details);
                                    detailsTextView.setText(details != null ? details : "");

                                    // Add a "GO" button to the bottom sheet
                                    Button goButton = bottomSheetView.findViewById(R.id.go_button);
                                    goButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Start the navigation to the selected location
                                            startNavigation(latitude, longitude);
                                            bottomSheetDialog.dismiss();
                                        }
                                    });

                                    // Set the view of the BottomSheetDialog
                                    bottomSheetDialog.setContentView(bottomSheetView);

                                    // Show the BottomSheetDialog
                                    bottomSheetDialog.show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle the error
                            }
                        });

                        return true;
                    }
                });



            }
        });

        return view;
    }

    // Method to start the navigation to the selected location
    private void startNavigation(double latitude, double longitude) {
        // Use the latitude and longitude to start the navigation using the desired navigation library or API.
        // For example, if you are using Google Maps, you can launch the Google Maps app with the destination location.
        String uri = "google.navigation:q=" + latitude + "," + longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
}
