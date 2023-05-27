package com.example.whereswaldo.ui.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.whereswaldo.ml.ModelVivid;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.example.whereswaldo.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CameraFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_IMAGE_PICK = 1;

    private Button cameraButton;
    private Button galleryButton;
    private ImageView imageView;
    private TextView resultTextView;
    private int imageSize = 224;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        cameraButton = view.findViewById(R.id.button);
        galleryButton = view.findViewById(R.id.button2);
        imageView = view.findViewById(R.id.imageView);
        resultTextView = view.findViewById(R.id.result);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        return view;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelVivid model = ModelVivid.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            // iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelVivid.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Central Park", "Central Station", "Chinatown", "Convergence", "Darling Harbour", "Darling Square Alley", "Darling Square Foodcourt", "Ephemeral Oceania", "Haymarket", "Macula", "Market City", "Museum Of Contemporary Art", "Queen Victoria Building", "Sydney Harbour Bridge", "Sydney Opera House", "UTS Building 10", "UTS Building 2"};
            String className = classes[maxPos];

            openBottomSheetDialog(className);

            // Set an OnClickListener to the imageView to reopen the bottom sheet dialog
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBottomSheetDialog(className);
                }
            });

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void openBottomSheetDialog(String className) {
        // Retrieve data from Firebase based on the className
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query query = databaseRef.orderByChild("Name").equalTo(className);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve the attributes "Name", "Artist", "Location", and "Details" from the snapshot
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

                    // Set the view of the BottomSheetDialog
                    bottomSheetDialog.setContentView(bottomSheetView);

                    // Show the BottomSheetDialog
                    bottomSheetDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(image); // Set the original image without scaling
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // Rescale the image to fit within the imageView
                imageView.setAdjustViewBounds(true); // Maintain the aspect ratio of the image
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image); // Set the original image without scaling
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // Rescale the image to fit within the imageView
                imageView.setAdjustViewBounds(true); // Maintain the aspect ratio of the image

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
    }
}