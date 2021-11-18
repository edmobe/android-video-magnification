package com.example.videomagnification.gui.interaction;

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
import com.example.videomagnification.application.App;

public class RoiSelectorActivity extends AppCompatActivity {

    private SeekBar seekBarX;
    private SeekBar seekBarY;
    private TextView textViewX;
    private TextView textViewY;
    ImageView imageView;

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
         TODO: verify if preview image is compressed
         TODO: verify if it is always in range

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

        String inputFileName = ((App) getApplication()).getAppData().getAviVideoPath();
        String thumbnailFileName = ((App) getApplication()).getAppData().getCompressedVideoPath();

        inputVideoUri = Uri.parse(inputFileName);
        thumbnailUri = Uri.parse(thumbnailFileName);

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(getApplicationContext(), thumbnailUri);
        thumbnail = mMMR.getFrameAtTime();

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);

        int imageWidth = thumbnail.getWidth();
        int imageHeight = thumbnail.getHeight();

        ((App) getApplication()).getAppData().setImageWidth(imageWidth);
        ((App) getApplication()).getAppData().setImageHeight(imageHeight);
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
            ((App) getApplication()).getAppData().setRoiX(seekBarX.getProgress());
            ((App) getApplication()).getAppData().setRoiY(seekBarY.getProgress());
            startActivity(new Intent(getApplicationContext(), VitalSignSelectorActivity.class));
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