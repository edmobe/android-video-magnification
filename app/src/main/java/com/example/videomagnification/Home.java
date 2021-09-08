package com.example.videomagnification;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Home extends AppCompatActivity {

    private Button btnOpen;
    private static final int PICK_AVI_VIDEO = 2;
    private Uri videoPath;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(v -> {
            // Check condition
            int permission1 = ContextCompat.checkSelfPermission(Home.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int permission2 = ContextCompat.checkSelfPermission(Home.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission1 == PackageManager.PERMISSION_GRANTED &&
                permission2 == PackageManager.PERMISSION_GRANTED) {
                // Permission granted => create file picker method
                videoPicker();
            } else {
                // Permission not granted => request permission
                ActivityCompat.requestPermissions(Home.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                // The result data contains a URI for the document or directory that
                // the user selected.
                if (resultData != null) {
                    videoPath = resultData.getData();
                    Intent videoEditorActivity = new Intent(getApplicationContext(),
                            VideoEditor.class);
                    videoEditorActivity.putExtra(getString(R.string.video_file_path),
                            "/storage/6531-3531/vid_avi/baby.avi");
                            //"/storage/self/primary/Download/sample.txt");
                            //videoPath.getPath());
                            //videoPath.toString());
                    //displayShortToast("Video Path: " + videoPath.toString());
                    displayShortToast(getApplicationInfo().dataDir);
                    startActivity(videoEditorActivity);

                } else {
                    displayShortToast("Please select a video file.");
                }
            } else {
                displayShortToast("Unknown request code: " + requestCode + ".");
            }

        } else if (resultCode == 0) {
            displayShortToast("Please select a video file.");
        } else {
            displayShortToast("File opener error. Result code: " + resultCode + ".");
        }
    }

    private void videoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/avi");
        startActivityForResult(intent, PICK_AVI_VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull @org.jetbrains.annotations.NotNull String[] permissions,
            @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check condition
        if ((grantResults.length > 0)) {
            for (int permission : grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    displayShortToast("Not all permissions are granted!");
                    return;
                }
            }
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

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}