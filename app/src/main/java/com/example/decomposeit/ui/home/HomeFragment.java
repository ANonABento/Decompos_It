package com.example.decomposeit.ui.home;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.IOException;
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

import com.example.decomposeit.ui.gallery.GalleryFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;


public class HomeFragment extends Fragment implements LifecycleOwner {

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

        if (allPermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permission already granted
                    startCamera();
                }
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

        // Create a time-stamped name
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());

        // Define the collection where the image will be saved (Pictures/CameraX-Image)
        String relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + "CameraX-Image";

        // Set up content values for the new image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        // Insert a new record into the MediaStore and get the content URI
        Uri imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        // Create output options object which contains the content URI
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onError(@NonNull ImageCaptureException exc) {
                Toast.makeText(requireContext(), "Photo capture failed: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Uri savedUri = output.getSavedUri();
                if (savedUri != null) {
                    String msg = "Photo capture succeeded: " + savedUri.toString();
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

                    // Pass the image URI to the GalleryFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("imageUri", savedUri.toString());
                    GalleryFragment galleryFragment = new GalleryFragment();
                    galleryFragment.setArguments(bundle);

                    // Navigate to the GalleryFragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_galleryFragment);

                    // Create an InputImage from the saved image
                    InputImage image = createInputImage(savedUri);

                    // Now you can use 'image' for ML Kit Vision processing
                    processImageWithMLKit(image);
                } else {
                    Toast.makeText(requireContext(), "Photo capture succeeded, but the URI is null.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private InputImage createInputImage(Uri imageUri) {
        try {
            Context context = requireContext();
            return InputImage.fromFilePath(context, imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface LumaListener {
        void onLumaCalculated(double luma);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            ProcessCameraProvider cameraProvider;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return;
            }

            // Preview
            Preview preview = new Preview.Builder()
                    .build();
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder()
                    .build();

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .build();
            imageAnalysis.setAnalyzer(cameraExecutor, new LuminosityAnalyzer(new LumaListener() {
                @Override
                public void onLumaCalculated(double luma) {
                    // Update the UI on the main thread
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textLumin = getView().findViewById(R.id.textLumin);
                            textLumin.setText(""+luma);
                        }
                    });
                }
            }));


            // Select back camera as a default
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);



            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
            }

        }, ContextCompat.getMainExecutor(requireContext()));

        // Create an instance of ImageLabeler
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        // Or, if you want to set a custom confidence threshold:
        // ImageLabelerOptions options = new ImageLabelerOptions.Builder()
        //     .setConfidenceThreshold(0.7f)
        //     .build();
        // ImageLabeler labeler = ImageLabeling.getClient(options);
    }

    private void processImageWithMLKit(InputImage image) {
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        // Process labels inside this block
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            // Do something with the label information
                            // Log the detected label to Logcat
                            Log.d(TAG, "Detected label: " + text + " (Confidence: " + confidence + ")");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure inside this block
                    }
                });

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
