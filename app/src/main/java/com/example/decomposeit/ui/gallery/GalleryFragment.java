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
    private GalleryAdapter galleryAdapter;

    public static class ImageModel {
        private String filePath;

        public ImageModel(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        galleryAdapter = new GalleryAdapter(capturedImages); // Pass the list of captured images
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private List<ImageModel> imageList;

        public GalleryAdapter(List<ImageModel> imageList) {
            this.imageList = imageList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ImageModel imageModel = imageList.get(position);
            // Load and display the image using an image loading library like Glide or Picasso.
            // You can also use a custom ImageView to display the images.
            // holder.imageView.setImageURI(Uri.parse(imageModel.getFilePath()));
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // Define your ImageView here for displaying the image
            // ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Initialize your ImageView here
                // imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
