package com.example.videomagnification;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.videomagnification.databinding.ActivityMainBinding;

import org.apache.commons.io.FilenameUtils;

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

    private VideoMagnificator magnificator;

//    private WorkRequest videoMagnificationRequest;
//    private Data algorithmData;
//    private Constraints constraints;
//    private String finalState;

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
        roiX = intent.getIntExtra(getString(R.string.roi_x), 1);
        roiY = intent.getIntExtra(getString(R.string.roi_y), 1);

        magnificator = new ViewModelProvider(this).get(VideoMagnificator.class);

//        Data.Builder dataBuilder = new Data.Builder()
//                .putString("videoIn", videoPath)
//                .putString("outDir", FilenameUtils.getPath(videoPath))
//                .putDouble("alpha", alpha)
//                .putDouble("fl", fl)
//                .putDouble("fh", fh)
//                .putDouble("samplingRate", sampling)
//                .putDouble("chromAttenuation", chromAtt);


        boolean gaussianIdeal = algorithmRadioButtonId == gaussianId;
        boolean laplacianIdeal = algorithmRadioButtonId == laplacianIdealId;
        boolean laplacianButter = algorithmRadioButtonId == laplacianButterId;
        boolean knownAlgorithm = gaussianIdeal || laplacianIdeal || laplacianButter;


        if (!knownAlgorithm) {
            // UNKNOWN ALGORITHM
            // TODO
        } else {
            gaussianIdealInBackground(result -> {
                if (result instanceof Result.Success) {
                    finish(((Result.Success<String>) result).data);
                } else {
                    // ERROR
                    App.displayShortToast("Error al finalizar");
                    // TODO
                }
            });

//            App.getExecutorService().execute(() -> {
//
//
//                    } else if (laplacianIdeal) {
            // LAPLACIAN IDEAL
            // ============= FOR TESTING =================
//                        videoPath = "/storage/emulated/0/Pictures/video-magnification/guitar.avi";
//                dataBuilder = new Data.Builder()
//                        .putInt("algorithm", 1)
//                        .putString("videoIn", videoPath)
//                        .putString("outDir", FilenameUtils.getPath(videoPath))
//                        .putDouble("alpha", 100)
//                        .putDouble("lambdaC", 10)
//                        .putDouble("fl", 100)
//                        .putDouble("fh", 120)
//                        .putDouble("samplingRate", 600)
//                        .putDouble("chromAttenuation", 0);
//                algorithmData = dataBuilder.build();
            // ============= FOR RELEASE =================
//                algorithmData = dataBuilder
//                        .putInt("algorithm", 1)
//                        .putDouble("lambdaC", lambdaC)
//                        .build();
            // ===========================================
//                    } else {
            // LAPLACIAN BUTTER
            // ============= FOR TESTING =================
//                        videoPath = "/storage/emulated/0/Pictures/video-magnification/baby.avi";
//                dataBuilder = new Data.Builder()
//                        .putInt("algorithm", 2)
//                        .putString("videoIn", videoPath)
//                        .putString("outDir", FilenameUtils.getPath(videoPath))
//                        .putDouble("alpha", 30)
//                        .putDouble("lambdaC", 16)
//                        .putDouble("fl", 0.4)
//                        .putDouble("fh", 3)
//                        .putDouble("samplingRate", 30)
//                        .putDouble("chromAttenuation", 0.1);
//                algorithmData = dataBuilder.build();
            // ============= FOR RELEASE =================
//                algorithmData = dataBuilder
//                        .putInt("algorithm", 2)
//                        .putDouble("lambdaC", lambdaC)
//                        .build();
            // ===========================================
        }
    }

    public void gaussianIdealInBackground(final Callback <String> callback) {
        App.getExecutorService().execute(() -> {
            try {
                // ============= FOR TESTING =================
                videoPath = "/storage/emulated/0/Pictures/video-magnification/baby2.avi";
                String state = magnificator.amplify_spatial_gdown_temporal_ideal(
                        videoPath, FilenameUtils.getPath(videoPath), 150, 6,
                        (double) 14 / (double) 16, (double) 16 / (double) 6, 30,
                        1);
                if (state.equals("error")) {
                    callback.onComplete(new Result.Error<>(
                            new Exception("Error in magnification")));
                } else {
                    callback.onComplete(new Result.Success<>(state));
                }
                // ============= FOR RELEASE =================
                // TODO
                //
                //                dataBuilder = new Data.Builder()
                //                        .putInt("algorithm", 0)
                //                        .putString("videoIn", videoPath)
                //                        .putString("outDir", FilenameUtils.getPath(videoPath))
                //                        .putDouble("alpha", 150)
                //                        .putInt("level", 6)
                //                        .putDouble("fl", (double) 14 / (double) 6)
                //                        .putDouble("fh", (double) 16 / (double) 6)
                //                        .putDouble("samplingRate", 30)
                //                        .putDouble("chromAttenuation", 1);
                //                algorithmData = dataBuilder.build();

                //                algorithmData = dataBuilder
                //                        .putInt("algorithm", 0)
                //                        .putInt("level", level)
                //                        .build();
                // ===========================================
            } catch (Exception e) {
                // ERROR
                // TODO
            }
        });
    }




//            constraints = new Constraints.Builder()
//                    .setRequiresStorageNotLow(true)
//                    .build();
//
//            videoMagnificationRequest =
//                    new OneTimeWorkRequest.Builder(VideoMagnificator.class)
//                            .setInputData(algorithmData)
//                            .setConstraints(constraints)
//                            .build();
//
//            WorkManager
//                    .getInstance(getApplicationContext())
//                    .enqueue(videoMagnificationRequest);

//    public void processFinalState() {
//        if (!finalState.equals("error")) {
//
//        } else {
//            // Final state error
//            // TODO
//        }
//    }

    public void finish(String state) {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

        Runnable myRunnable = () -> {
            Intent videoConverterActivity = new Intent(MainActivity.this,
                    VideoConverter.class);
            videoConverterActivity.putExtra(MainActivity.this.getString(
                    R.string.video_file_path), state);
            videoConverterActivity.putExtra(MainActivity.this.getString(
                    R.string.conversion_type), 1);
            MainActivity.this.startActivity(videoConverterActivity);
        };

        mainHandler.post(myRunnable);

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

}
//
