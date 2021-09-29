package com.example.videomagnification.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.databinding.ActivityMainBinding;
import com.example.videomagnification.magnificators.MagnificatorGdownIdeal;
import com.example.videomagnification.magnificators.MagnificatorLpyrButter;
import com.example.videomagnification.magnificators.MagnificatorLpyrIdeal;
import com.example.videomagnification.threading.TaskRunner;

import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static TextView magnifierLog;
    public static ProgressBar progress;
    private Button buttonConvert;
    private String finalState;

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
    
    private TaskRunner taskRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        magnifierLog = findViewById(R.id.text_magnify_log);
        progress = findViewById(R.id.progress_magnify);

        buttonConvert = findViewById(R.id.btn_convert_output);
        finalState = "error";

        buttonConvert.setOnClickListener(v -> {
            Intent videoConverterActivity = new Intent(MainActivity.this,
                    VideoConverter.class);
            videoConverterActivity.putExtra(MainActivity.this.getString(
                    R.string.video_file_path), finalState);
            videoConverterActivity.putExtra(MainActivity.this.getString(
                    R.string.conversion_type), 1);
            MainActivity.this.startActivity(videoConverterActivity);
        });

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

        taskRunner = new TaskRunner();

        boolean gaussianIdeal = algorithmRadioButtonId == gaussianId;
        boolean laplacianIdeal = algorithmRadioButtonId == laplacianIdealId;
        boolean laplacianButter = algorithmRadioButtonId == laplacianButterId;
        boolean knownAlgorithm = gaussianIdeal || laplacianIdeal || laplacianButter;


        if (!knownAlgorithm) {
            // UNKNOWN ALGORITHM
            // TODO
        } else {
            if (gaussianIdeal) {
                // GAUSSIAN IDEAL
                videoPath = "/storage/emulated/0/Pictures/video-magnification/baby2.avi";
                taskRunner.executeAsync(new MagnificatorGdownIdeal(
                        videoPath, FilenameUtils.getPath(videoPath), 150, 6,
                        (double) 14 / (double) 6, (double) 16 / (double) 6, 30,
                        1), (data) -> {
                    this.finalState = data;
                    buttonConvert.setVisibility(View.VISIBLE);
                });
            } else if (laplacianIdeal) {
                // LAPLACIAN IDEAL
                videoPath = "/storage/emulated/0/Pictures/video-magnification/guitar.avi";
                taskRunner.executeAsync(new MagnificatorLpyrIdeal(
                        videoPath, FilenameUtils.getPath(videoPath), 50, 10,
                        72, 92, 600, 0), (data) -> {
                    this.finalState = data;
                    buttonConvert.setVisibility(View.VISIBLE);
                });
            } else {
                // LAPLACIAN BUTTER
                videoPath = "/storage/emulated/0/Pictures/video-magnification/baby.avi";
                taskRunner.executeAsync(new MagnificatorLpyrButter(
                        videoPath, FilenameUtils.getPath(videoPath), 30, 16,
                        0.4, 3, 30, 0.1), (data) -> {
                    this.finalState = data;
                    buttonConvert.setVisibility(View.VISIBLE);
                });
            }
        }
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
