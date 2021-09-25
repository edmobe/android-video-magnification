package com.example.videomagnification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.databinding.ActivityMainBinding;

import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

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
    private String extract;
    private int algorithmRadioButtonId;
    private int roiX;
    private int roiY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Intent intent = getIntent(); // gets the previously created intent
//        videoPath = intent.getStringExtra(getString(R.string.video_file_path));
//        alpha = intent.getIntExtra(getString(R.string.alpha), 1);
//        lambdaC = intent.getIntExtra(getString(R.string.lambda_c), 1);
//        level = intent.getIntExtra(getString(R.string.level), 1);
//        fl = intent.getFloatExtra(getString(R.string.low_frequency), 0.1f);
//        fh = intent.getFloatExtra(getString(R.string.high_frequency), 0.2f);
//        sampling = intent.getFloatExtra(getString(R.string.sampling_rate), 0.1f);
//        chromAtt = intent.getFloatExtra(getString(R.string.chrom_attenuation), 0.1f);
//        r1 = intent.getFloatExtra(getString(R.string.r1), 0.1f);
//        r2 = intent.getFloatExtra(getString(R.string.r2), 0.1f);
//        extract = intent.getStringExtra(getString(R.string.extract));
//        algorithmRadioButtonId = intent.getIntExtra(
//                getString(R.string.select_an_algorithm), -1);
//        roiX = intent.getIntExtra(getString(R.string.roi_x), 1);
//        roiY = intent.getIntExtra(getString(R.string.roi_y), 1);
//
//        int state = amplifySpatialLpyrTemporalIdeal(videoPath, FilenameUtils.getPath(videoPath),
//                100, 10, 100, 120, 600, 0);
//        ((App) getApplication()).displayShortToast(String.valueOf(state));

        videoPath = "/storage/emulated/0/Pictures/video-magnification/face.avi";

        int state = amplifySpatialLpyrTemporalIdeal(videoPath, FilenameUtils.getPath(videoPath),
                100, 10, 100, 120, 600, 0);

    }

    public native int amplifySpatialLpyrTemporalIdeal(String videoIn, String outDir,
                                                      double alpha, double lambda_c,
                                                      double fl, double fh, double samplingRate,
                                                      double chromAttenuation);

}