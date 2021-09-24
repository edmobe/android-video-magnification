package com.example.videomagnification;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class VideoConverter extends AppCompatActivity {

    private static final String outputDir = "/video-magnification/";
    private String fileDir;
    private Uri inputVideoUri;
    private String midVideoPath;
    private Uri outputVideoUri;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_converter);

        Intent intent = getIntent(); // gets the previously created intent
        String inputFileName = intent.getStringExtra(getString(R.string.video_file_path));

        inputVideoUri = Uri.parse(inputFileName);

        progressBar = findViewById(R.id.progress_convert);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onNext(@NonNull Integer integer) {
                    // TODO: Error handling
                    ((App) getApplication()).logDebug("Observable", integer.toString());
                    switch (integer) {
                        case 1: {
                            createDirectoryIfNeeded();
                            progressBar.setProgress(20);
                            break;
                        }
                        case 2: {
                            midVideoPath = convertMp4ToMjpeg(inputVideoUri);
                            progressBar.setProgress(60);
                            break;
                        }
                        case 3: {
                            outputVideoUri = convertMjpegToAvi(midVideoPath);
                            progressBar.setProgress(100);
                            break;
                        }
                    }
                }

            @Override
            public void onError(@NonNull Throwable e) {
                ((App) getApplication()).displayShortToast(
                        "Error while converting the video!"
                );
                ((App) getApplication()).logError("Converting video",
                        e.getLocalizedMessage());
            }

            @Override
            public void onComplete() {
                ((App) getApplication()).logDebug("Observable", "Completed!");
                Intent roiActivity = new Intent(getApplicationContext(),
                        RegionOfInterest.class);
                roiActivity.putExtra(getString(R.string.video_file_path),
                        outputVideoUri.toString());
                roiActivity.putExtra(getString(R.string.video_file_path_thumbnail),
                        inputVideoUri.toString());
                startActivity(roiActivity);
            }
        });
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
                ((App)getApplication()).displayShortToast(
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
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String midVideoPath = fileDir + inputBaseName + ".mjpeg";
        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -q:v 2 -vcodec mjpeg " + midVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath  +
                " to " + midVideoPath);

        return midVideoPath;
    }

    private Uri convertMjpegToAvi(String inputVideoPath) {
        // TODO: Error handling
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String outputVideoPath = fileDir + inputBaseName + ".avi";
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath+ " -q:v 2 -vcodec mjpeg " + outputVideoPath);
        ((App)getApplication()).logDebug(
                "Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        ((App)getApplication()).logDebug(
                "Native lib", "Converted video from " + inputVideoPath +
                " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }


}