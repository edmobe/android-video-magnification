package com.example.videomagnification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VideoEditor extends AppCompatActivity {

    private String videoPath;
    private Button btnNext;
    private RadioGroup radioGroupAlgorithm;
    private RadioGroup radioGroupMagnification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        Intent intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra("videoPath");

        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        ImageView imageView = (ImageView) findViewById(R.id.videoPreview);
        imageView.setImageBitmap(thumbnail);

        radioGroupAlgorithm = findViewById(R.id.radio_group_algorithm);
        radioGroupMagnification = findViewById(R.id.radio_group_magnify);

        radioGroupAlgorithm.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroupMagnification.getCheckedRadioButtonId() != -1) {
                btnNext.setEnabled(true);
            }
        });

        radioGroupMagnification.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroupAlgorithm.getCheckedRadioButtonId() != -1) {
                btnNext.setEnabled(true);
            }
        });

        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            int selectedAlgorithmOption = radioGroupAlgorithm.getCheckedRadioButtonId();
            int selectedMagnificationOption = radioGroupMagnification.getCheckedRadioButtonId();

            if (selectedAlgorithmOption == -1) {
                displayShortToast("Please select an algorithm.");
            } else if (selectedMagnificationOption == -1) {
                displayShortToast("Please select what you want to magnify.");
            } else if (selectedAlgorithmOption ==
                    findViewById(R.id.radio_gaussian_ideal).getId()) {
                displayShortToast("Not yet implemented!");
            } else if (selectedAlgorithmOption == findViewById(R.id.radio_laplacian_ideal).getId()
                    || selectedAlgorithmOption ==
                    findViewById(R.id.radio_laplacian_butterworth).getId()) {
                Intent videoEditorLaplacianIdealButterActivity =
                        new Intent(getApplicationContext(), VideoEditorLaplacianIdealButter.class);
                videoEditorLaplacianIdealButterActivity.putExtra("videoPath", videoPath);
                videoEditorLaplacianIdealButterActivity.putExtra("radioButtonId",
                        selectedAlgorithmOption);
                startActivity(videoEditorLaplacianIdealButterActivity);
            } else if (selectedAlgorithmOption == findViewById(R.id.radio_laplacian_iir).getId()) {
                displayShortToast("Not yet implemented!");
            } else {
                displayShortToast("Unknown error!");
            }
        });
    }

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}