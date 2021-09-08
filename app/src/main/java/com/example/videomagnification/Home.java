package com.example.videomagnification;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Home extends AppCompatActivity {

    private Button btnOpen;
    private static final int PICK_AVI_VIDEO = 2;
    private Uri videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(v -> {
            // Check condition
            if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted => request permission
                ActivityCompat.requestPermissions(Home.this,
                        new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                // Permission granted => create file picker method
                videoPicker();
            }
        });
    }

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
                            videoPath.toString());
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

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}