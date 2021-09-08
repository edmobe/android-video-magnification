package com.example.videomagnification;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
    private Uri inputVideoPath;
    private String midVideoPath;
    private Uri outputVideoPath;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_converter);

        Intent intent = getIntent(); // gets the previously created intent
        String inputFileName = intent.getStringExtra(getString(R.string.video_file_path));
        inputVideoPath = Uri.parse(inputFileName);

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
                            midVideoPath = convertMp4ToMjpeg(inputVideoPath);
                            progressBar.setProgress(60);
                            break;
                        }
                        case 3: {
                            outputVideoPath = convertMjpegToAvi(midVideoPath);
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
                Intent videoEditorActivity = new Intent(getApplicationContext(),
                        VideoEditor.class);
                videoEditorActivity.putExtra(getString(R.string.video_file_path),
                        outputVideoPath.toString());
                startActivity(videoEditorActivity);
            }
        });
    }

    private boolean createDirectoryIfNeeded() {
        File outputsFolder = new File(
                Environment.getExternalStorageDirectory().getPath() + outputDir);
        if (!outputsFolder.exists()) {
            try {
                outputsFolder.mkdir();
                return true;
            } catch (Exception e) {
                ((App)getApplication()).displayShortToast(
                        "Error creating the output video directory!");
                Log.e("Native lib", "Error creating the folder: " +
                        e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    private String convertMp4ToMjpeg(Uri inputVideoUri) {
        Log.d("Native lib", "Output video URI: " + outputVideoPath);
        String inputVideoPath = FFmpegKitConfig.getSafParameterForRead(
                this, inputVideoUri);
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String midVideoPath = Environment.getExternalStorageDirectory().getPath() +
                outputDir + inputBaseName + ".mjpeg";
        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -vcodec mjpeg " + midVideoPath);
        Log.d("Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        Log.d("Native lib", "Converted video from " + inputVideoPath  +
                " to " + midVideoPath);

        return midVideoPath;
    }

    private Uri convertMjpegToAvi(String inputVideoPath) {
        String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
        String outputVideoPath = Environment.getExternalStorageDirectory().getPath() +
                outputDir + inputBaseName + ".avi";
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath+ " -vcodec mjpeg " + outputVideoPath);
        Log.d("Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        Log.d("Native lib", "Converted video from " + inputVideoPath +
                " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }


}