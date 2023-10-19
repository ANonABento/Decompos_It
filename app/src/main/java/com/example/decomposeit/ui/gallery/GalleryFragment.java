package com.example.decomposeit.ui.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.decomposeit.R;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.widget.TextView;

public class GalleryFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d("gallery receiver", "GalleryFragment onCreateView called");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Reference all the UI elements
        ImageView imageView = root.findViewById(R.id.imageView);
        TextView textLabel = root.findViewById(R.id.textLabel);
        TextView textDecomp = root.findViewById(R.id.textDecomp);
        TextView textNotes = root.findViewById(R.id.textNotes);

        // Receive bundle of goodies from home fragment
        Bundle bundle = requireArguments();
        // Declare variable to store item label
        String sentLabel = null;
        // If bundle has something in it
        if (bundle != null) {
            String imageUriString = bundle.getString("imageUri");
            sentLabel = bundle.getString("sentLabel");

            if (imageUriString != null) {
                // Load the image into the ImageView using the image URI
                Uri imageUri = Uri.parse(imageUriString);
                Log.d("gallery receiver", "Received Image URI: " + imageUriString);

                // Obtain the ContentResolver
                ContentResolver contentResolver = requireContext().getContentResolver();

                // Decode the bitmap
                Bitmap originalBitmap = null;
                try {
                    InputStream inputStream = contentResolver.openInputStream(imageUri);
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Obtain the original dimensions of the photo
                int originalWidth = originalBitmap.getWidth();
                int originalHeight = originalBitmap.getHeight();

                // New dimensions by applying a scaling factor
                int newWidth = (int) (originalWidth * 0.5);
                int newHeight = (int) (originalHeight * 0.5);

                // Resize the original bitmap
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

                // Set UI image element to the resized image
                imageView.setImageBitmap(resizedBitmap);
            } else {
                Log.d("gallery receiver", "Null imageUriString!");
            }

            // If received label isn't empty
            if (sentLabel != null) {
                // Set UI element to the label
                textLabel.setText("Label: " + sentLabel);
                Log.d("gallery receiver", "Received label: " + sentLabel);
            } else {
                Log.d("gallery receiver", "Null sentLabel!");
            }
        } else {
            Log.d("gallery receiver", "Null Bundle!");
        }

        // Display decomp time
        String decompTime = findDecompTime(sentLabel);
        textDecomp.setText("Decomposition Time: " + decompTime);
        // Display decomp notes
        String decompNotes = findDecompNotes(sentLabel);
        textNotes.setText("" + decompNotes);

        return root;
    }

    // 2D array acting as the database for labels and decomposition information
    private String arrTimes[][] = {
            {"glasses", "Several centuries to millennia", "Factors include materials used (plastic, metal, glass), exposure to environmental elements, and disposal methods."},
            {"hand", "Several years to several decades", "Factors include environmental conditions, temperature, moisture, presence of scavengers, and burial depth."},
            {"paper", "2 - 5 months", "Factors include paper type (coated or uncoated), moisture, temperature, and microbial activity."}
    };

    // Loops through 2D array to return decomposition time
    String findDecompTime(String label){
        String decompTime = null;
        // For loop that starts at index 0 to index length of array
        for (int i = 0; i <= arrTimes.length; i++) {
            if (arrTimes[i][0].equals(label.toLowerCase())) {
                decompTime = arrTimes[i][1];
                break;
            }
        }
        if (decompTime.equals(null)){ // If nothing matches
            decompTime = "Undefined";
        }
        return decompTime;
    }

    // Loops through 2d array to return decomposition notes
    String findDecompNotes(String label){
        String decompNotes = null;
        //For loop that goes through the entire array
        for (int i = 0; i <= arrTimes.length; i++) {
            if (arrTimes[i][0].equals(label.toLowerCase())) {
                decompNotes = arrTimes[i][2];
                break;
            }
        }
        if (decompNotes.equals(null)){ // If nothing matches
            decompNotes = "No information found.";
        }
        return decompNotes;
    }
}
