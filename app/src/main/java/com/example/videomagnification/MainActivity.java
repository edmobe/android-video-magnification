package com.example.videomagnification;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.databinding.ActivityMainBinding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        extract = intent.getStringExtra(getString(R.string.extract));
        algorithmRadioButtonId = intent.getIntExtra(
                getString(R.string.select_an_algorithm), -1);

        ((App) getApplication()).displayShortToast(stringFromJNI(videoPath));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(String videoPath);

}