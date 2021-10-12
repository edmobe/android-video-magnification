package com.example.videomagnification.gui.input;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.gui.interaction.RoiSelectorActivity;
import com.example.videomagnification.application.App;
import com.example.videomagnification.utils.conversion.VideoConverter;


public class VideoConverterActivity extends AppCompatActivity {

    private VideoConverter videoConverter;
    private ProgressBar progressBar;
    private TextView textConversionInfo;
    private int conversionType;

    private class ConversionTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO: ERROR HANDLING
            // TODO: FIX OUTPUT VIDEO NOT WORKING ON WINDOWS 10
            try {
                // ======= CREATE DIRECTORY IF NEEDED =======
                videoConverter.createDirectoryIfNeeded();
                progressBar.setProgress(20);

                String midVideoPath;

                // ======= GENERATE MJPEG =======
                if (conversionType == 0)
                    midVideoPath = videoConverter.convertMp4ToMjpeg(
                            App.getAppData().getInputVideoUri());
                else
                    midVideoPath = videoConverter.convertAviToMjpeg(
                            App.getAppData().getProcessedVideoPath());
                progressBar.setProgress(60);
                App.logDebug("Converting - Mid video path", midVideoPath);

                // ======= GENERATE OUTPUT =======
                if (conversionType == 0) {
                    // AVI VIDEO
                    videoConverter.convertMjpegToAvi(midVideoPath);
                    App.logDebug("Converting - Output video path",
                            App.getAppData().getAviVideoPath());
                    }
                else {
                    // MP4 VIDEO
                    App.getAppData().setFinalMp4VideoUri(
                            videoConverter.convertMjpegToMp4(midVideoPath));
                    videoConverter.deleteFiles();
                    App.displayShortToast("Converted - Output video path: " +
                            App.getAppData().getFinalMp4VideoUri().getPath());
                }

                progressBar.setProgress(100);
                return "success";
            } catch (Exception e) {
                return "error";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO: ERROR HANDLING
            super.onPostExecute(result);
            if (!result.equals("error")) {
                App.logDebug("Observable", "Completed!");
                if (conversionType == 0) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(VideoConverterActivity.this.getMainLooper());

                    Runnable myRunnable = () -> {
                        startActivity(new Intent(getApplicationContext(),
                                RoiSelectorActivity.class));
                    };

                    mainHandler.post(myRunnable);

                } else if (conversionType == 1) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(VideoConverterActivity.this.getMainLooper());

                    Runnable myRunnable = () -> {
                        progressBar.setVisibility(View.GONE);
                        textConversionInfo.setText("Successfully converted video to MP4");
                    };

                    mainHandler.post(myRunnable);

                } else {
                    // TODO
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_converter);

        // 0: input, 1: output
        conversionType = App.getAppData().getConversionType();

        progressBar = findViewById(R.id.progress_convert);
        textConversionInfo = findViewById(R.id.text_conversion_info);

        videoConverter = new VideoConverter(getApplicationContext());

        if (conversionType == 0) {
            textConversionInfo.setText(getString(R.string.why_convert_input));
        } else if (conversionType == 1) {
            textConversionInfo.setText(getString(R.string.why_convert_output));
        } else {
            // CONVERSION TYPE ERROR
            // TODO
        }

        new ConversionTask().execute();

    }




}