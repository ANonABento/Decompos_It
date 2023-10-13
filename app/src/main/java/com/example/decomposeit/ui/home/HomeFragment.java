package com.example.decomposeit.ui.home;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.decomposeit.databinding.ActivityMainBinding;
import com.example.decomposeit.databinding.FragmentHomeBinding;
import com.example.decomposeit.R;

import com.google.common.util.concurrent.ListenableFuture;



public class HomeFragment extends Fragment {

    private Button imageCaptureButton;
    private ImageCapture imageCapture;
    private TextureView viewFinder;
    private ExecutorService cameraExecutor;
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Request camera permissions
        if (allPermissionsGranted()) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                // Permission already granted
                startCamera();
            }
        } else {
            requestPermissions();
        }

        imageCaptureButton = root.findViewById(R.id.imageCaptureButton);

        imageCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto(view);
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        return root;
    }

    private void takePhoto(View view) {
        Log.d(TAG, "takePhotoed");

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            return;
        }

        // Get a stable reference of the modifiable image capture use case
        if (this.imageCapture == null) {
            return;
        }

        // Create a time-stamped name and MediaStore entry
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onError(@NonNull ImageCaptureException exc) {
                Toast.makeText(requireContext(), "Photo capture failed: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                String msg = "Photo capture succeeded: " + output.getSavedUri();
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                if (cameraProvider == null) {
                    Toast.makeText(requireContext(), "Failed to initialize camera", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Initialize viewFinder from the existing binding object
                PreviewView viewFinder = binding.viewFinder; // Use the existing binding object instead of inflating a new one

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // ImageCapture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to the camera
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }






    private void requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        List<String> permissionsToCheck = new ArrayList<>();
        permissionsToCheck.add(Manifest.permission.CAMERA);
        permissionsToCheck.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
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

    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
}
