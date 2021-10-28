package com.example.videomagnification.processing.conversion;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import com.example.videomagnification.application.App;
import com.example.videomagnification.gui.input.VideoConverterActivity;
import com.example.videomagnification.gui.interaction.RoiSelectorActivity;

public class ConversionTask extends AsyncTask<String, Void, String> {

    private VideoConverter videoConverter;
    private Activity context;
    private int conversionType;

    public ConversionTask(Activity context, int conversionType) {
        this.context = context;
        videoConverter = new VideoConverter(context);
        this.conversionType = conversionType;
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO: OUTPUT VIDEO NOT WORKING ON WINDOWS 10
        try {
            // ======= CREATE DIRECTORY IF NEEDED =======
            videoConverter.createDirectoryIfNeeded();
            ((VideoConverterActivity) context).getProgressBar().setProgress(20);

            String midVideoPath;

            // ======= GENERATE MJPEG =======
            if (conversionType == 0)
                midVideoPath = videoConverter.convertMp4ToMjpeg(
                        ((App) context.getApplication()).getAppData().getInputVideoUri());
            else
                midVideoPath = videoConverter.convertAviToMjpeg(
                        ((App) context.getApplication()).getAppData().getProcessedVideoPath());
            ((VideoConverterActivity) context).getProgressBar().setProgress(60);
            App.logDebug("Converting - Mid video path", midVideoPath);

            // ======= GENERATE OUTPUT =======
            if (conversionType == 0) {
                // AVI VIDEO
                videoConverter.convertMjpegToAvi(midVideoPath);
                App.logDebug("Converting - Output video path",
                        ((App) context.getApplication()).getAppData().getAviVideoPath());
            }
            else {
                // MP4 VIDEO
                ((App) context.getApplication()).getAppData().setFinalMp4VideoUri(
                        videoConverter.convertMjpegToMp4(midVideoPath));
                videoConverter.deleteFiles();
                ((App) context.getApplication()).displayShortToast(
                        "Converted - Output video path: " + ((App) context.getApplication())
                                .getAppData().getFinalMp4VideoUri().getPath());
            }

            ((VideoConverterActivity) context).getProgressBar().setProgress(100);
            return "success";
        } catch (Exception e) {
            ((VideoConverterActivity) context).getProgressBar().setVisibility(View.GONE);
            // TODO: Make string resource
            ((VideoConverterActivity) context).getTextConversionInfo().setText(
                    e.getMessage());
            return "error";
        }

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (!result.equals("error")) {
            App.logDebug("Observable", "Completed!");
            if (conversionType == 0) {
                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = () -> {
                    context.startActivity(new Intent(context.getApplicationContext(),
                            RoiSelectorActivity.class));
                };

                mainHandler.post(myRunnable);

            } else if (conversionType == 1) {
                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = () -> {
                    ((VideoConverterActivity) context).getProgressBar().setVisibility(View.GONE);
                    ((VideoConverterActivity) context).getButtonNewVideo()
                            .setVisibility(View.VISIBLE);
                    ((VideoConverterActivity) context).getButtonViewConvertedVideos()
                            .setVisibility(View.VISIBLE);
                    ((VideoConverterActivity) context).getTextConversionInfo()
                            .setText("Successfully converted video to MP4");
                };

                mainHandler.post(myRunnable);

            } else {
                ((VideoConverterActivity) context).getProgressBar().setVisibility(View.GONE);
                // TODO: Make string resource
                ((VideoConverterActivity) context).getTextConversionInfo().setText(
                        "Error converting video");
            }
        }
    }
}
