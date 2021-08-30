package com.example.videomagnification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.videomagnification.databinding.ActivityMainBinding;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnOpen;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(v -> {
            // Check condition
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted => request permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                // Permission granted => create file picker method
                videoPicker();
            }
        });
    }

    private void videoPicker() {
        // Initialize intent
        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
        // Put extra
        intent.putExtra(FilePickerActivity.CONFIGS,
                new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(false)
                        .setShowVideos(true)
                        .enableVideoCapture(true)
                        .setMaxSelection(1)
                        .setSkipZeroSizeFiles(true)
                        .build());
        // Start activity result
        startActivityForResult(intent, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check condition
        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            // Permission granted
            // Check condition
            if (requestCode == 1) {
                videoPicker();
            } else {
                displayShortToast("Unknown error while checking permissions.");
            }
        } else {
            displayShortToast("Permission denied!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (resultCode == RESULT_OK && data != null) {
            // Initialize array list
            ArrayList<MediaFile> mediaFiles = data.getParcelableArrayListExtra(
                    FilePickerActivity.MEDIA_FILES
            );
            // Get path string
            String path = mediaFiles.get(0).getPath();
            // Check condition
            if (requestCode == 101) {
                displayShortToast("Video path: " + path);
            } else {
                displayShortToast("Unknown error while picking the video.");
            }
        } else if(data == null) {
            displayShortToast("Error: video is null.");
        } else {
            displayShortToast("Unknown error while picking the video. Result code: "
                    + resultCode + ".");
        }
    }

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}