package com.example.videomagnification;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RegionOfInterest extends AppCompatActivity {

    private Intent intent;
    private String videoPath;
    private String extract;
    private int roiX;
    private int roiY;

    private void getIntentDetails() {
        intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra(getString(R.string.video_file_path));
        extract = intent.getStringExtra(getString(R.string.extract));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_of_interest);
    }
}