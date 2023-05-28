package com.example.whereswaldo.ui.camera;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.whereswaldo.R;
import com.bumptech.glide.Glide;

public class ClassifiedImageActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView nameTextView;
    private TextView locationTextView;
    private TextView artistTextView;
    private TextView detailsTextView;
    private Button goButton;
    private ImageView cancelIcon;

    private double latitude; // Your latitude value
    private double longitude; // Your longitude value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Log.d("SCREEN", "IN CLASSIFED ACTICITY ON CREATE");

        // Retrieve the information passed from the previous screen
        String imageUri = getIntent().getStringExtra("imageUri");
        String name = getIntent().getStringExtra("name");
        String location = getIntent().getStringExtra("location");
        String artist = getIntent().getStringExtra("artist");
        String details = getIntent().getStringExtra("details");


        // Initialize and populate the ImageView and TextViews
        imageView = findViewById(R.id.image_view);
        nameTextView = findViewById(R.id.name_text_view);
        locationTextView = findViewById(R.id.location_text_view);
        artistTextView = findViewById(R.id.artist_text_view);
        detailsTextView = findViewById(R.id.details_text_view);
        goButton = findViewById(R.id.go_button_2);
        cancelIcon = findViewById(R.id.back_icon);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Load the image into the ImageView using an image loading library like Glide or Picasso
//        Glide.with(this).load(imageUri).into(imageView);
        Glide.with(this)
                .load(imageUri)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Glide", "Image loading failed", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);

        // Set the text for the TextViews
        nameTextView.setText(name);
        locationTextView.setText(location);
        artistTextView.setText(artist);
        detailsTextView.setText(details);

        // Set onClickListener for the "Go" button
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Google Maps with the specified coordinates
                String uri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        // Set onClickListener for the "Cancel" button
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect back to the camera fragment
                Log.d("CANCCEL ICON", "CLICCCCCCCCCKKKKED");
                finish();
            }
        });

    }

}
