package com.example.decomposeit;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class SecretFragment extends Fragment {

    public SecretFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_secret, container, false);

        // Reference the image UI element
        ImageView imageView = rootView.findViewById(R.id.korone);
        // Use Glide to show the GIF in an imageView
        Glide.with(this).load(R.drawable.inugami_korone).into(imageView);

        return rootView;
    }
}