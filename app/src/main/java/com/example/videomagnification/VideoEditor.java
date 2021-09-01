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
    private RadioGroup radioGroup;

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

        radioGroup = findViewById(R.id.myRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> btnNext.setEnabled(true));

        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            int selectedOption = radioGroup.getCheckedRadioButtonId();

            if (selectedOption == -1) {
                displayShortToast("Please select an algorithm.");
            } else if (selectedOption == findViewById(R.id.radio_gaussian_ideal).getId()) {
                displayShortToast("Not yet implemented!");
            } else if (selectedOption == findViewById(R.id.radio_laplacian_ideal).getId() ||
                    selectedOption == findViewById(R.id.radio_laplacian_butterworth).getId()) {
                Intent videoEditorLaplacianIdealButterActivity =
                        new Intent(getApplicationContext(), VideoEditorLaplacianIdealButter.class);
                videoEditorLaplacianIdealButterActivity.putExtra("videoPath", videoPath);
                videoEditorLaplacianIdealButterActivity.putExtra("radioButtonId",
                        selectedOption);
                startActivity(videoEditorLaplacianIdealButterActivity);
            } else if (selectedOption == findViewById(R.id.radio_laplacian_iir).getId()) {
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