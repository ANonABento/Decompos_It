package com.example.decomposeit.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.decomposeit.R;

public class HomeFragment extends Fragment {

    private TextureView myTextureView;
    private Button imageCaptureButton;

    // CameraX's variables
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }

        imageCaptureButton = root.findViewById(R.id.imageCaptureButton);

        imageCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        return root;
    }

    private void takePhoto() {
        // Implement your take photo logic here
        // For example, start a camera activity, show a dialog, etc.
    }

    private void startCamera() {
        // Implement your camera initialization logic here
    }

    private void requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        List<String> permissionsToCheck = new ArrayList<>();
        permissionsToCheck.add(Manifest.permission.CAMERA);
        permissionsToCheck.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionsToCheck.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    ActivityResultLauncher<String[]> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String, Boolean> permissions) {
                            // Handle Permission granted/rejected
                            boolean permissionGranted = true;
                            for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                                if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
                                    permissionGranted = false;
                                }
                            }
                            if (!permissionGranted) {
                                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_SHORT).show();
                            } else {
                                startCamera();
                            }
                        }
                    });

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
}
