package com.example.videomagnification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VideoEditor extends AppCompatActivity {

    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        Intent intent = getIntent(); // gets the previously created intent
        videoUrl = intent.getStringExtra("videoUrl");

        TextView path = (TextView) findViewById(R.id.previewPath);
        path.setText(videoUrl);

        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoUrl,
                MediaStore.Images.Thumbnails.MINI_KIND);
        ImageView imageView = (ImageView) findViewById(R.id.videoPreview);
        imageView.setImageBitmap(thumbnail);

    }

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}