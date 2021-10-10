package com.example.videomagnification.activities;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.example.videomagnification.R;
import com.example.videomagnification.application.App;

import org.apache.commons.io.FilenameUtils;

import java.io.File;


public class VideoConverter extends AppCompatActivity {

    private static final String outputDir = "/video-magnification/";
    private String fileDir;
    String inputFileName;
    private Uri inputVideoUri;
    private String compressedVideoPath;
    private String midVideoPath;
    private Uri outputVideoUri;
    private ProgressBar progressBar;
    private TextView textConversionInfo;
    private int conversionType;

    private class ConversionTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO: ERROR HANDLING
            try {
                createDirectoryIfNeeded();
                progressBar.setProgress(20);
                if (conversionType == 0)
                    midVideoPath = convertMp4ToMjpeg(inputVideoUri);
                else
                    midVideoPath = convertAviToMjpeg(inputFileName);
                progressBar.setProgress(60);
                ((App) getApplication()).logDebug(
                        "Converting - Mid video path", midVideoPath);
                if (conversionType == 0)
                    outputVideoUri = convertMjpegToAvi(midVideoPath);
                else {
                    outputVideoUri = convertMjpegToMp4(midVideoPath);
                    deleteFiles();
                }

                ((App) getApplication()).logDebug(
                        "Converting - Output video path",
                        outputVideoUri.getPath());
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
                ((App) getApplication()).logDebug("Observable", "Completed!");
                if (conversionType == 0) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(VideoConverter.this.getMainLooper());

                    Runnable myRunnable = () -> {
                        Intent roiActivity = new Intent(getApplicationContext(),
                                RegionOfInterest.class);
                        roiActivity.putExtra(getString(R.string.video_file_path),
                                outputVideoUri.toString());
                        roiActivity.putExtra(getString(R.string.video_file_path_thumbnail),
                                compressedVideoPath);
                        startActivity(roiActivity);
                    };

                    mainHandler.post(myRunnable);

                } else if (conversionType == 1) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(VideoConverter.this.getMainLooper());

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

        Intent intent = getIntent(); // gets the previously created intent
        inputFileName = intent.getStringExtra(getString(R.string.video_file_path));
        // 0: input, 1: output, -1: error
        conversionType = intent.getIntExtra(getString(R.string.conversion_type), -1);

        progressBar = findViewById(R.id.progress_convert);
        textConversionInfo = findViewById(R.id.text_conversion_info);

        if (conversionType == 0) {
            textConversionInfo.setText(getString(R.string.why_convert_input));
            inputVideoUri = Uri.parse(inputFileName);
        } else if (conversionType == 1) {
            textConversionInfo.setText(getString(R.string.why_convert_output));
        } else {
            // CONVERSION TYPE ERROR
            // TODO
        }

        new ConversionTask().execute();

    }

    public void deleteFiles() {
        File dir = new File(fileDir);
        //Checking the directory exists
        if (!dir.exists())
            return;
        //Getting the list of all the files in the specific  direcotry
        File fList[] = dir.listFiles();

        for (File f : fList) {
           //checking the extension of the file with endsWith method.
            if (
                    f.getName().endsWith(".avi") ||
                    f.getName().endsWith(".mjpeg") ||
                    f.getName().endsWith("_compressed.mp4")) {
                f.delete();
            }

        }

    }

    private boolean createDirectoryIfNeeded() {
        fileDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath() + outputDir;
        File outputsFolder = new File(fileDir);
        if (!outputsFolder.exists()) {
            try {
                outputsFolder.mkdir();
                ((App)getApplication()).logDebug("Create directory",
                        "Created directory for the first time: " + fileDir);
                return true;
            } catch (Exception e) {
                App.displayShortToast(
                        "Error creating the output video directory!");
                ((App)getApplication()).logError(
                        "Native lib", "Error creating the folder: " +
                        e.getLocalizedMessage());
                return false;
            }
        }
        ((App)getApplication()).logDebug("Create directory",
                "No need to create the directory");
        return true;
    }

    private String convertMp4ToMjpeg(Uri inputVideoUri) {
        // TODO: Error handling
        String inputVideoPath = FFmpegKitConfig.getSafParameterForRead(
                this, inputVideoUri);

        ((App)getApplication()).logDebug(
                "Native lib", "Input video path: " + inputVideoPath);

        String inputBaseName = FilenameUtils.removeExtension(
                ((App) getApplication()).getFileNameFromUri(inputVideoUri));
        String midVideoPath = fileDir + inputBaseName + ".mjpeg";

        // Compress video
        // TODO: validate that the video is less than 20 seconds long
        // TODO: put conversion in another method
        // TODO: display progress bar to user more accurately
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getApplicationContext(), inputVideoUri);
        int width = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        compressedVideoPath = fileDir + inputBaseName + "_compressed.mp4";
        String scale;

        // Compress
        if (width * height > 640 * 640) {
            // Must resize the video
            if (width > height) {
                // Horizontal
                scale = " -vf scale=640:-2 ";
            } else {
                // Vertical or squared
                scale = " -vf scale=-2:640 ";
            }
        } else {
            // Just avoid divisible by 2 error
            // https://stackoverflow.com/questions/20847674/ffmpeg-libx264-height-not-divisible-by-2
            scale = " -vf \"crop=trunc(iw/2)*2:trunc(ih/2)*2\" ";
        }

        // Horizontal
        FFmpegSession resizeSession = FFmpegKit.execute(
                "-y -i " + inputVideoPath + scale + " -q:v 2 " + compressedVideoPath);

        ((App)getApplication()).logDebug(
                "Native lib", "Resize session info: " +
                        resizeSession.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Successfully resized video");

        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + compressedVideoPath + " -q:v 2 -vcodec mjpeg " + midVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath  +
                " to " + midVideoPath);

        return midVideoPath;
    }

    private String convertAviToMjpeg(String inputVideoPath) {
        // TODO: Error handling
        ((App)getApplication()).logDebug(
                "Native lib", "Input video path: " + inputVideoPath);
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String midVideoPath = fileDir + inputBaseName + ".mjpeg";
        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -q:v 2 -vcodec libx265 -acodec aac " + midVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath  +
                        " to " + midVideoPath);

        return midVideoPath;
    }

    private Uri convertMjpegToAvi(String inputVideoPath) {
        // TODO: Error handling
        // TODO: remove conversion files when finished
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String outputVideoPath = fileDir + inputBaseName + ".avi";
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -q:v 2 -r 30 -vcodec mjpeg " + outputVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath +
                " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }



    private Uri convertMjpegToMp4(String inputVideoPath) {
        // TODO: Error handling
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String outputVideoPath = fileDir + inputBaseName + ".mp4";
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath+ " -q:v 2 -vcodec libx265 -acodec aac " + outputVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath +
                        " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }


}