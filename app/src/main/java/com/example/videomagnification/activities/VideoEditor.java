package com.example.videomagnification.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.application.App;
import com.example.videomagnification.R;

public class VideoEditor extends AppCompatActivity {

    private Intent intent;
    private String videoPath;
    private String thumbnailFileName;

    private Button btnNext;
    private RadioGroup radioGroupAlgorithm;
    private RadioGroup radioGroupExtract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra(getString(R.string.video_file_path));
        thumbnailFileName = intent.getStringExtra(
                getString(R.string.video_file_path_thumbnail));

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(getApplicationContext(), Uri.parse(thumbnailFileName));
        Bitmap thumbnail = mMMR.getFrameAtTime();
        ImageView imageView = findViewById(R.id.preview_roi);
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

        btnNext = findViewById(R.id.btn_next_editor);
        btnNext.setOnClickListener(v -> {
            int selectedAlgorithmOption = radioGroupAlgorithm.getCheckedRadioButtonId();
            int selectedMagnificationOption = radioGroupExtract.getCheckedRadioButtonId();

            if (selectedAlgorithmOption == -1) {
                App.displayShortToast("Please select an algorithm.");
            } else if (selectedMagnificationOption == -1) {
                App.displayShortToast("Please select what you want to extract.");
            } else if (selectedAlgorithmOption == findViewById(R.id.radio_gaussian_ideal).getId() ||
                    selectedAlgorithmOption == findViewById(R.id.radio_laplacian_ideal).getId() ||
                    selectedAlgorithmOption == findViewById(R.id.radio_laplacian_butterworth)
                            .getId()) {
                Intent videoEditorParametersActivity =
                        new Intent(getApplicationContext(), VideoEditorParameters.class);
                videoEditorParametersActivity.putExtra(getString(R.string.video_file_path), videoPath);
                videoEditorParametersActivity.putExtra(
                        getString(R.string.select_an_algorithm),
                        selectedAlgorithmOption);
                videoEditorParametersActivity.putExtra(getString(R.string.roi_x),
                        intent.getIntExtra(getString(R.string.roi_x), 1));
                videoEditorParametersActivity.putExtra(getString(R.string.roi_y),
                        intent.getIntExtra(getString(R.string.roi_y), 1));

                if (selectedMagnificationOption ==
                        findViewById(R.id.radio_respiratory_rate).getId() ||
                    selectedMagnificationOption ==
                            findViewById(R.id.radio_heart_rate).getId()) {
                    videoEditorParametersActivity.putExtra(
                            getString(R.string.extract), selectedMagnificationOption);
                } else {
                    App.displayShortToast(
                            "Please select what you want to extract.");
                    return;
                }
                startActivity(videoEditorParametersActivity);
            } else {
                App.displayShortToast("Unknown error!");
            }
        });
    }
}