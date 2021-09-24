package com.example.videomagnification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegionOfInterest extends AppCompatActivity {

    private SeekBar seekBarX;
    private SeekBar seekBarY;
    private TextView textViewX;
    private TextView textViewY;

    private Button buttonNext;

    private Uri inputVideoUri;
    private Uri thumbnailUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_of_interest);

        Intent intent = getIntent(); // gets the previously created intent
        String inputFileName = intent.getStringExtra(getString(R.string.video_file_path));
        String thumbnailFileName = intent.getStringExtra(
                getString(R.string.video_file_path_thumbnail));

        inputVideoUri = Uri.parse(inputFileName);
        thumbnailUri = Uri.parse(thumbnailFileName);

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(getApplicationContext(), thumbnailUri);
        Bitmap thumbnail = mMMR.getFrameAtTime();
        ImageView imageView = findViewById(R.id.preview_roi);
        imageView.setImageBitmap(thumbnail);

        seekBarX = findViewById(R.id.seek_roi_x);
        seekBarY = findViewById(R.id.seek_roi_y);

        textViewX = findViewById(R.id.text_view_roi_x);
        textViewY = findViewById(R.id.text_view_roi_y);

        textViewX.setText(String.valueOf(seekBarX.getProgress()));
        textViewY.setText(String.valueOf(seekBarY.getProgress()));

        buttonNext = findViewById(R.id.btn_next_roi);

        seekBarX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewX.setText(String.valueOf(seekBarX.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        seekBarY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewY.setText(String.valueOf(seekBarY.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        buttonNext.setOnClickListener(v -> {
            Intent videoEditorActivity = new Intent(getApplicationContext(), VideoEditor.class);
            videoEditorActivity.putExtra(getString(R.string.video_file_path),
                    inputVideoUri.toString());
            videoEditorActivity.putExtra(getString(R.string.video_file_path_thumbnail),
                    thumbnailUri.toString());
            videoEditorActivity.putExtra(getString(R.string.roi_x), seekBarX.getProgress());
            videoEditorActivity.putExtra(getString(R.string.roi_y), seekBarY.getProgress());
            startActivity(videoEditorActivity);
        });
    }
}