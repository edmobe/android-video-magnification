package com.example.videomagnification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VideoEditor extends AppCompatActivity {

    private String videoPath;
    private Button btnNext;

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

        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            Intent videoEditorLaplacianIdealButterActivity =
                    new Intent(getApplicationContext(), VideoEditorLaplacianIdealButter.class);
            videoEditorLaplacianIdealButterActivity.putExtra("videoPath", videoPath);
            startActivity(videoEditorLaplacianIdealButterActivity);
        });


    }

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}