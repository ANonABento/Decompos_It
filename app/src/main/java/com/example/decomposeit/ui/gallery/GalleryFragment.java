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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.decomposeit.R;
import com.example.decomposeit.databinding.FragmentGalleryBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import android.content.ContentResolver;

public class GalleryFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("gallery receiver", "GalleryFragment onCreateView called");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        ImageView imageView = root.findViewById(R.id.imageView);

        // Retrieve the image URI from the arguments
        Bundle bundle = requireArguments();
        if (bundle != null) {
            String imageUriString = bundle.getString("imageUri");
            if (imageUriString != null) {
                // Load the image into the ImageView using the image URI
                Uri imageUri = Uri.parse(imageUriString);
                Log.d("gallery receiver", "Received Image URI: " + imageUriString);

                // Obtain the ContentResolver using the requireContext() method
                ContentResolver contentResolver = requireContext().getContentResolver();

                // Use the ContentResolver to open an InputStream and decode the bitmap
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

                // Calculate the new dimensions by applying the scaling factor
                int newWidth = (int) (originalWidth * 0.5);
                int newHeight = (int) (originalHeight * 0.5);

                // Resize the original bitmap using createScaledBitmap()
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

                imageView.setImageBitmap(resizedBitmap);
            }
            else {
                Log.d("gallery receiver", "Null imageUriString!");
            }
        }
        else {
            Log.d("gallery receiver", "Null Bundle!");
        }


        return root;
    }
}
