package com.example.videomagnification.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;

public class RegionOfInterest extends AppCompatActivity {

    private SeekBar seekBarX;
    private SeekBar seekBarY;
    private TextView textViewX;
    private TextView textViewY;
    ImageView imageView;

    private int imageWidth;
    private int imageHeight;

    private Bitmap thumbnail;
    private Bitmap preview;
    private Canvas canvas;
    private Paint paint;

    private Button buttonNext;

    private Uri inputVideoUri;
    private Uri thumbnailUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         TODO: change circle to square
         TODO: verify if preview image is compressed

             (0, 0)
             o———————————————————————————————————————————————o————————————————————>
             |            .                                  .
             |            .                                  .
             |            .                                  .
             |            .                                  .
             |            .  (roiX, roiY)                    .
             o . . . . .  x——————————————————————————————————o
             |            |                                  |
             |            |                                  |
             |            |                                  |
             |            |                                  |
             |            |                                  |
             o . . . . .  o——————————————————————————————————x (roiX + 100, roiY + 100)
             |
             |
             |
             |
             v

         */
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
        thumbnail = mMMR.getFrameAtTime();

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);

        imageWidth = thumbnail.getWidth();
        imageHeight = thumbnail.getHeight();
        imageView = findViewById(R.id.preview_roi);
        imageView.setImageBitmap(thumbnail);

        seekBarX = findViewById(R.id.seek_roi_x);
        seekBarY = findViewById(R.id.seek_roi_y);
        seekBarX.setMax(imageWidth - 100);
        seekBarY.setMax(imageHeight - 100);

        updatePreview();

        textViewX = findViewById(R.id.text_view_roi_x);
        textViewY = findViewById(R.id.text_view_roi_y);

        textViewX.setText(String.valueOf(seekBarX.getProgress()));
        textViewY.setText(String.valueOf(seekBarY.getProgress()));

        buttonNext = findViewById(R.id.btn_next_roi);

        seekBarX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewX.setText(String.valueOf(seekBarX.getProgress()));
                updatePreview();
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
                updatePreview();
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

    private void updatePreview() {
        preview = thumbnail.copy(thumbnail.getConfig(), true);
        canvas = new Canvas(preview);
        float x1 = seekBarX.getProgress();
        float x2 = seekBarY.getProgress();
        canvas.drawRect(x1, x2, x1 + 100, x2 + 100, paint);
        imageView.setImageBitmap(preview);
    }
}