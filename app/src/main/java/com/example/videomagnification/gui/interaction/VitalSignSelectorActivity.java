package com.example.videomagnification.gui.interaction;

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

public class VitalSignSelectorActivity extends AppCompatActivity {

    private Intent intent;
    private Button btnNext;
    private RadioGroup radioGroupAlgorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        intent = getIntent(); // gets the previously created intent
        String thumbnailFileName = ((App) getApplication()).getAppData().getCompressedVideoPath();

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(getApplicationContext(), Uri.parse(thumbnailFileName));
        Bitmap thumbnail = mMMR.getFrameAtTime();
        ImageView imageView = findViewById(R.id.preview_roi);
        imageView.setImageBitmap(thumbnail);

        radioGroupAlgorithm = findViewById(R.id.radio_group_algorithm);

        radioGroupAlgorithm.setOnCheckedChangeListener((group, checkedId) -> {
                btnNext.setEnabled(true);
        });

        btnNext = findViewById(R.id.btn_next_editor);
        btnNext.setOnClickListener(v -> {
            int selectedAlgorithmOption = radioGroupAlgorithm.getCheckedRadioButtonId();

            if (selectedAlgorithmOption == -1) {
                ((App) getApplication()).displayShortToast("Please select an algorithm.");
            } else if (selectedAlgorithmOption == R.id.radio_gaussian_ideal ||
                    selectedAlgorithmOption == R.id.radio_laplacian_butterworth) {
                ((App) getApplication())
                        .getAppData().setSelectedAlgorithmOption(selectedAlgorithmOption);
                startActivity(
                        new Intent(getApplicationContext(), VideoEditorParametersActivity.class));
            } else {
                ((App) getApplication()).displayShortToast("Unknown error!");
            }
        });
    }
}