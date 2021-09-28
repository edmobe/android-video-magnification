package com.example.videomagnification;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.databinding.ActivityMainBinding;

import org.apache.commons.io.FilenameUtils;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static TextView magnifierLog;
    public static ProgressBar progress;

    private ActivityMainBinding binding;
    private String videoPath;
    private int alpha;
    private int lambdaC;
    private int level;
    private float fl;
    private float fh;
    private float sampling;
    private float chromAtt;
    private float r1;
    private float r2;
    private int extractRadioButtonId;
    private int algorithmRadioButtonId;
    private int roiX;
    private int roiY;

    private int gaussianId;
    private int laplacianIdealId;
    private int laplacianButterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        magnifierLog = findViewById(R.id.text_magnify_log);
        progress = findViewById(R.id.progress_magnify);

        Intent intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra(getString(R.string.video_file_path));
        alpha = intent.getIntExtra(getString(R.string.alpha), 1);
        lambdaC = intent.getIntExtra(getString(R.string.lambda_c), 1);
        level = intent.getIntExtra(getString(R.string.level), 1);
        fl = intent.getFloatExtra(getString(R.string.low_frequency), 0.1f);
        fh = intent.getFloatExtra(getString(R.string.high_frequency), 0.2f);
        sampling = intent.getFloatExtra(getString(R.string.sampling_rate), 0.1f);
        chromAtt = intent.getFloatExtra(getString(R.string.chrom_attenuation), 0.1f);
        r1 = intent.getFloatExtra(getString(R.string.r1), 0.1f);
        r2 = intent.getFloatExtra(getString(R.string.r2), 0.1f);
        extractRadioButtonId = intent.getIntExtra(getString(R.string.extract), -1);
        algorithmRadioButtonId = intent.getIntExtra(
                getString(R.string.select_an_algorithm), -1);

        gaussianId = R.id.radio_gaussian_ideal;
        laplacianIdealId = R.id.radio_laplacian_ideal;
        laplacianButterId = R.id.radio_laplacian_butterworth;

//        roiX = intent.getIntExtra(getString(R.string.roi_x), 1);
//        roiY = intent.getIntExtra(getString(R.string.roi_y), 1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
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
                        if (integer == 1) {
                            int state = -1;

                            if (algorithmRadioButtonId == gaussianId) {
                                // Gaussian Ideal
                                // ============= FOR TESTING =================
                                App.displayShortToast("Gaussian ideal");
                                videoPath =
                                        "/storage/emulated/0/Pictures/video-magnification/face.avi";
                                state = amplifySpatialGdownTemporalIdeal(videoPath,
                                        FilenameUtils.getPath(videoPath),
                                        50,4,  (double)5 / (double)6, 1,
                                        30, 1);
                                // ============= FOR RELEASE =================
//                            state = amplifySpatialLpyrTemporalIdeal(videoPath,
//                                    FilenameUtils.getPath(videoPath),
//                                    alpha, lambdaC, fl, fh, sampling, chromAtt);
                            } else if (algorithmRadioButtonId == laplacianIdealId) {
                                // Laplacian Ideal
                                // ============= FOR TESTING =================
                                App.displayShortToast("Laplacian ideal");
                                videoPath =
                                        "/storage/emulated/0/Pictures/video-magnification/guitar.avi";
                                state = amplifySpatialLpyrTemporalIdeal(videoPath,
                                        FilenameUtils.getPath(videoPath),
                                        100, 10, 100, 120, 600,
                                        0);
                                // ============= FOR RELEASE =================
//                            state = amplifySpatialLpyrTemporalIdeal(videoPath,
//                                    FilenameUtils.getPath(videoPath),
//                                    alpha, lambdaC, fl, fh, sampling, chromAtt);
                                // ===========================================
                            } else if (algorithmRadioButtonId == laplacianButterId) {
                                // Butter
                                App.displayShortToast("Butter");
                            } else {
                                App.displayShortToast("Unknown algorithm");
                            }
                            //App.displayShortToast(String.valueOf(state));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        App.displayShortToast(
                                "Error while processing the video!"
                        );
                        ((App) getApplication()).logError("Video processing - Error",
                                e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {
                        ((App) getApplication()).logDebug(
                                "Video processing - Observable", "Completed!");
                    }
                });

    }

    public static void updateMagnifierLog(String string) {
        boolean handler = new Handler(Looper.getMainLooper()).post(() -> {
            MainActivity.magnifierLog.setText(string);
        });
    }

    public static void updateProgress(int progress) {
        boolean handler = new Handler(Looper.getMainLooper()).post(() -> {
            if (progress >= 100) {
                MainActivity.progress.setVisibility(View.INVISIBLE);
            } else {
                MainActivity.progress.setProgress(progress);
            }
        });
    }

    public native int amplifySpatialLpyrTemporalIdeal(String videoIn, String outDir,
                                                      double alpha, double lambda_c,
                                                      double fl, double fh, double samplingRate,
                                                      double chromAttenuation);

    public native int amplifySpatialGdownTemporalIdeal(String videoIn, String outDir,
                                                      double alpha, int level,
                                                      double fl, double fh, double samplingRate,
                                                      double chromAttenuation);

}