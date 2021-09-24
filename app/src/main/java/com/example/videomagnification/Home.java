package com.example.videomagnification;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Home extends AppCompatActivity {

    private Button btnOpen;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int PICK_AVI_VIDEO = 2;
    private Uri inputVideoUri;

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(v -> {
            if (arePermissionsGranted()) {
                // You can use the API that requires the permission.
                videoPicker();
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissions(new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSIONS);
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
                    inputVideoUri = resultData.getData();
                    Intent videoConverterActivity = new Intent(getApplicationContext(),
                            VideoConverter.class);
                    videoConverterActivity.putExtra(getString(R.string.video_file_path),
                            inputVideoUri.toString());
                    startActivity(videoConverterActivity);
                } else {
                    ((App)getApplication()).displayShortToast(
                            "Please select a video file.");
                }
            } else {
                ((App)getApplication()).displayShortToast(
                        "Unknown request code: " + requestCode + ".");
            }

        } else if (resultCode == 0) {
            ((App)getApplication()).displayShortToast(
                    "Please select a video file.");
        } else {
            ((App)getApplication()).displayShortToast(
                    "File opener error. Result code: " + resultCode + ".");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    videoPicker();
                } else {
                    ((App)getApplication()).displayShortToast(
                            "Error: all the requested permissions are needed.");
                }
                return;
        }
    }

    private void videoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/mp4");
        startActivityForResult(intent, PICK_AVI_VIDEO);
    }

}