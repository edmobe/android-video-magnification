package com.example.videomagnification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class VideoEditor extends AppCompatActivity {

    private String videoPath;
    private Button btnNext;
    private RadioGroup radioGroupAlgorithm;
    private RadioGroup radioGroupExtract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        Intent intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra(getString(R.string.video_file_path));

        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        ImageView imageView = (ImageView) findViewById(R.id.videoPreview);
        imageView.setImageBitmap(thumbnail);

        radioGroupAlgorithm = findViewById(R.id.radio_group_algorithm);
        radioGroupExtract = findViewById(R.id.radio_group_extract);

        radioGroupAlgorithm.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroupExtract.getCheckedRadioButtonId() != -1) {
                btnNext.setEnabled(true);
            }
        });


        radioGroupExtract.setOnCheckedChangeListener((group, checkedId) -> {
            if (radioGroupAlgorithm.getCheckedRadioButtonId() != -1) {
                btnNext.setEnabled(true);
            }
        });

        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            int selectedAlgorithmOption = radioGroupAlgorithm.getCheckedRadioButtonId();
            int selectedMagnificationOption = radioGroupExtract.getCheckedRadioButtonId();

            if (selectedAlgorithmOption == -1) {
                ((App) getApplication()).displayShortToast("Please select an algorithm.");
            } else if (selectedMagnificationOption == -1) {
                ((App) getApplication()).displayShortToast("Please select what you want to extract.");
            } else if (selectedAlgorithmOption == findViewById(R.id.radio_gaussian_ideal).getId() ||
                    selectedAlgorithmOption == findViewById(R.id.radio_laplacian_ideal).getId() ||
                    selectedAlgorithmOption == findViewById(R.id.radio_laplacian_butterworth).getId() ||
                    selectedAlgorithmOption == findViewById(R.id.radio_laplacian_iir).getId()) {
                Intent videoEditorParametersActivity =
                        new Intent(getApplicationContext(), VideoEditorParameters.class);
                videoEditorParametersActivity.putExtra(getString(R.string.video_file_path), videoPath);
                videoEditorParametersActivity.putExtra(
                        getString(R.string.select_an_algorithm),
                        selectedAlgorithmOption);
                if (selectedMagnificationOption ==
                        findViewById(R.id.radio_respiratory_rate).getId()) {
                    videoEditorParametersActivity.putExtra(
                            getString(R.string.extract), getString(R.string.respiratory_rate));
                } else if (selectedMagnificationOption ==
                        findViewById(R.id.radio_heart_rate).getId()) {
                    videoEditorParametersActivity.putExtra(
                            getString(R.string.extract), getString(R.string.heart_rate));
                } else {
                    ((App) getApplication()).displayShortToast(
                            "Please select what you want to extract.");
                    return;
                }
                startActivity(videoEditorParametersActivity);
            } else {
                ((App) getApplication()).displayShortToast("Unknown error!");
            }
        });
    }
}