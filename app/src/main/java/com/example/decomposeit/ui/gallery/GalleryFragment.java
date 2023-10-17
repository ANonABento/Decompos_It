package com.example.decomposeit.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.decomposeit.R;
import com.example.decomposeit.databinding.FragmentGalleryBinding;
import java.util.List;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;

   // public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    binding = FragmentGalleryBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        recyclerView = root.findViewById(R.id.recyclerView); // Make sure you have a RecyclerView in your layout
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        // Create a list of ImageData (custom class representing images with captions)
//        List<ImageData> imageDataList = createImageDataList();
//
//        galleryAdapter = new GalleryAdapter(imageDataList); // Create your custom adapter
//        recyclerView.setAdapter(galleryAdapter);
//
//        // Set a click listener for items in the RecyclerView
//        galleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(ImageData imageData) {
//                // Handle item click (e.g., show the image and caption)
//                // You can navigate to a detailed view, display a larger image, etc.
//            }
//        });

        //return root;
  //  }
}
