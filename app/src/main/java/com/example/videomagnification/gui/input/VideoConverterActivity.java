package com.example.videomagnification.gui.input;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;
import com.example.videomagnification.utils.conversion.ConversionTask;


public class VideoConverterActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textConversionInfo;
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

        if (conversionType == 0) {
            textConversionInfo.setText(getString(R.string.why_convert_input));
        } else if (conversionType == 1) {
            textConversionInfo.setText(getString(R.string.why_convert_output));
        } else {
            // CONVERSION TYPE ERROR
            // TODO
        }

        conversionTask = new ConversionTask(this, conversionType);
        conversionTask.execute();
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTextConversionInfo() {
        return textConversionInfo;
    }
}