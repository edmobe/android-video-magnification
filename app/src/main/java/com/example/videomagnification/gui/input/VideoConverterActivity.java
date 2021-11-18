package com.example.videomagnification.gui.input;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;
import com.example.videomagnification.processing.conversion.ConversionTask;


public class VideoConverterActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textConversionInfo;
    private Button buttonNewVideo;
    private Button buttonViewConvertedVideos;
    private int conversionType;
    private ConversionTask conversionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_converter);

        // 0: input, 1: output
        conversionType = ((App) getApplication()).getAppData().getConversionType();

        progressBar = findViewById(R.id.progress_convert);
        textConversionInfo = findViewById(R.id.text_conversion_info);

        buttonNewVideo = findViewById(R.id.btn_new_video_convert);
        buttonNewVideo.setOnClickListener(v -> {
            startActivity(new Intent(VideoConverterActivity.this,
                    HomeActivity.class));
        });

        buttonViewConvertedVideos = findViewById(R.id.btn_view_converted_videos);
        buttonViewConvertedVideos.setOnClickListener(v -> {
            Intent intent =  new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(((App) getApplication()).getAppData().getFinalMp4VideoUri(),
                    "video/*");
            startActivity(intent);
        });

        if (conversionType == 0) {
            textConversionInfo.setText(getString(R.string.why_convert_input));
        } else if (conversionType == 1) {
            textConversionInfo.setText(getString(R.string.why_convert_output));
        } else {
            // TODO: Make string resource
            textConversionInfo.setText("Unknown error.");
            progressBar.setVisibility(View.GONE);
        }

        conversionTask = new ConversionTask(this, conversionType);
        conversionTask.execute();
    }

    public Button getButtonViewConvertedVideos() { return buttonViewConvertedVideos; }

    public Button getButtonNewVideo() {
        return buttonNewVideo;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTextConversionInfo() {
        return textConversionInfo;
    }
}