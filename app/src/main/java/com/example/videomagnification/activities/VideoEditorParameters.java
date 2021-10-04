package com.example.videomagnification.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;

public class VideoEditorParameters extends AppCompatActivity {

    private final float FLOAT_DIVIDER = 100f;

    private Intent intent;
    private String videoPath;
    private int extract;
    private int roiX;
    private int roiY;

    private TextView videoPathTextView;
    private TextView algorithmTextView;
    private TextView extractTextView;
    private TextView roiTextView;
    private int algorithmRadioButtonId;

    SeekBar[] integerSeeks;
    SeekBar[] floatSeeks;
    private SeekBar seekAlpha;
    private SeekBar seekLambda;
    private SeekBar seekLevel;
    private SeekBar seekFl;
    private SeekBar seekFh;
    private SeekBar seekSampling;
    private SeekBar seekChromAtt;
    private SeekBar seekR1;
    private SeekBar seekR2;

    TextView[] integerTextViews;
    TextView[] floatTextViews;
    private TextView textAlpha;
    private TextView textLambda;
    private TextView textLevel;
    private TextView textFl;
    private TextView textFh;
    private TextView textSampling;
    private TextView textChromAtt;
    private TextView textR1;
    private TextView textR2;

    private Button start;

    private void getIntentDetails() {
        intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra(getString(R.string.video_file_path));
        extract = intent.getIntExtra(getString(R.string.extract), 0);
        roiX = intent.getIntExtra(getString(R.string.roi_x), 1);
        roiY = intent.getIntExtra(getString(R.string.roi_y), 1);
    }

    private void getAllParameters() {
        // SeekBars
        seekAlpha = findViewById(R.id.seek_alpha);
        seekLambda = findViewById(R.id.seek_lambda);
        seekLevel = findViewById(R.id.seek_level);
        seekFl = findViewById(R.id.seek_fl);
        seekFh = findViewById(R.id.seek_fh);
        seekSampling = findViewById(R.id.seek_sampling);
        seekChromAtt = findViewById(R.id.seek_chrom_att);
        seekR1 = findViewById(R.id.seek_r1);
        seekR2 = findViewById(R.id.seek_r2);

        // TextViews
        textAlpha = findViewById(R.id.text_alpha);
        textAlpha.setText(String.valueOf(getAlpha()));
        textLambda = findViewById(R.id.text_lambda);
        textLambda.setText(String.valueOf(getLambda()));
        textLevel = findViewById(R.id.text_level);
        textLevel.setText(String.valueOf(getLevel()));
        textFl = findViewById(R.id.text_fl);
        textFl.setText(String.valueOf(getFl()));
        textFh = findViewById(R.id.text_fh);
        textFh.setText(String.valueOf(getFh()));
        textSampling = findViewById(R.id.text_sampling);
        textSampling.setText(String.valueOf(getSampling()));
        textChromAtt = findViewById(R.id.text_chrom_att);
        textChromAtt.setText(String.valueOf(getChromAtt()));
        textR1 = findViewById(R.id.text_r1);
        textR1.setText(String.valueOf(getR1()));
        textR2 = findViewById(R.id.text_r2);
        textR2.setText(String.valueOf(getR2()));

        // General data
        videoPathTextView = findViewById(R.id.text_video_path);
        videoPathTextView.setText(videoPath);
        extractTextView = findViewById(R.id.text_extract);
        if (extract == R.id.radio_gaussian_ideal)
            extractTextView.setText("Heart rate");
        else if (extract == R.id.radio_laplacian_butterworth)
            extractTextView.setText("Respiratory rate");
        else {
            extractTextView.setText("Unable to get data");
            start.setEnabled(false);
        }
        roiTextView = findViewById(R.id.text_roi);
        String roi = "X = " + roiX + " Y = " + roiY;
        roiTextView.setText(roi);

        // Arrays
        integerSeeks = new SeekBar[]{seekAlpha, seekLambda, seekLevel};
        floatSeeks = new SeekBar[]{seekSampling, seekChromAtt, seekR1, seekR2};
        integerTextViews = new TextView[]{textAlpha, textLambda, textLevel};
        floatTextViews = new TextView[]{textSampling, textChromAtt, textR1, textR2};
    }

    private void hideGaussianIdealElements() {
        // Lambda
        findViewById(R.id.text_lambda).setVisibility(View.GONE);
        findViewById(R.id.textViewLambda).setVisibility(View.GONE);
        findViewById(R.id.seek_lambda).setVisibility(View.GONE);

        // R1
        findViewById(R.id.text_r1).setVisibility(View.GONE);
        findViewById(R.id.textViewR1).setVisibility(View.GONE);
        findViewById(R.id.seek_r1).setVisibility(View.GONE);

        // R2
        findViewById(R.id.text_r2).setVisibility(View.GONE);
        findViewById(R.id.textViewR2).setVisibility(View.GONE);
        findViewById(R.id.seek_r2).setVisibility(View.GONE);
    }

    private void hideLaplacianIdealButterElements() {
        // Level
        findViewById(R.id.text_level).setVisibility(View.GONE);
        findViewById(R.id.textViewLevel).setVisibility(View.GONE);
        findViewById(R.id.seek_level).setVisibility(View.GONE);

        // R1
        findViewById(R.id.text_r1).setVisibility(View.GONE);
        findViewById(R.id.textViewR1).setVisibility(View.GONE);
        findViewById(R.id.seek_r1).setVisibility(View.GONE);

        // R2
        findViewById(R.id.text_r2).setVisibility(View.GONE);
        findViewById(R.id.textViewR2).setVisibility(View.GONE);
        findViewById(R.id.seek_r2).setVisibility(View.GONE);
    }

    private void setParametersBasedOnAlgorithm() {
        algorithmRadioButtonId = intent.getIntExtra(
                getString(R.string.select_an_algorithm), -1);

        String spatialFiltering = "";
        String temporalFiltering = "";
        if (algorithmRadioButtonId == R.id.radio_gaussian_ideal) {
            spatialFiltering = "Gaussian pyramid";
            temporalFiltering = "Ideal bandpass";
            hideGaussianIdealElements();
        } else if (algorithmRadioButtonId == R.id.radio_laplacian_butterworth) {
            spatialFiltering = "Laplacian pyramid";
            temporalFiltering = "Subtraction of two butterworth low-pass filters";
            hideLaplacianIdealButterElements();
        } else {
            Intent videoEditorActivity =
                    new Intent(getApplicationContext(), VideoEditorParameters.class);
            videoEditorActivity.putExtra(getString(R.string.video_file_path), videoPath);
            startActivity(videoEditorActivity);
        }

        // General data
        algorithmTextView = findViewById(R.id.text_spatial_filter);
        algorithmTextView.setText(spatialFiltering);
        algorithmTextView = findViewById(R.id.text_temporal_filter);
        algorithmTextView.setText(temporalFiltering);
    }

    private void setSeekBarListeners() {
        for (int i = 0; i < integerSeeks.length; i++) {
            int textViewIndex = i;
            integerSeeks[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    integerTextViews[textViewIndex].setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        for (int i = 0; i < floatSeeks.length; i++) {
            int textViewIndex = i;
            floatSeeks[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    floatTextViews[textViewIndex].setText(String.valueOf(progress / FLOAT_DIVIDER));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        seekFl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress >= seekFh.getProgress()) {
                    seekFh.setProgress(progress + 1);
                    textFh.setText(String.valueOf(getFh()));
                }
                textFl.setText(String.valueOf(progress / FLOAT_DIVIDER));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekFh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= seekFl.getProgress()) {
                    seekFl.setProgress(progress - 1);
                    textFl.setText(String.valueOf(getFl()));
                }
                textFh.setText(String.valueOf(progress / FLOAT_DIVIDER));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor_parameters);

        getIntentDetails();
        getAllParameters();
        setParametersBasedOnAlgorithm();
        setSeekBarListeners();

        start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.putExtra(getString(R.string.video_file_path), videoPath);
            mainActivityIntent.putExtra(getString(R.string.alpha), getAlpha());
            mainActivityIntent.putExtra(getString(R.string.lambda_c), getLambda());
            mainActivityIntent.putExtra(getString(R.string.level), getLevel());
            mainActivityIntent.putExtra(getString(R.string.low_frequency), getFl());
            mainActivityIntent.putExtra(getString(R.string.high_frequency), getFh());
            mainActivityIntent.putExtra(getString(R.string.sampling_rate), getSampling());
            mainActivityIntent.putExtra(getString(R.string.chrom_attenuation), getChromAtt());
            mainActivityIntent.putExtra(getString(R.string.r1), getR1());
            mainActivityIntent.putExtra(getString(R.string.r2), getR2());
            mainActivityIntent.putExtra(getString(R.string.extract), extract);
            mainActivityIntent.putExtra(getString(R.string.select_an_algorithm),
                    algorithmRadioButtonId);
            mainActivityIntent.putExtra(getString(R.string.roi_x),
                    intent.getIntExtra(getString(R.string.roi_x), 1));
            mainActivityIntent.putExtra(getString(R.string.roi_y),
                    intent.getIntExtra(getString(R.string.roi_y), 1));
            startActivity(mainActivityIntent);
        });
    }

    private int getAlpha() {
        return seekAlpha.getProgress();
    }
    private int getLambda() {
        return seekLambda.getProgress();
    }
    private int getLevel() { return seekLevel.getProgress(); }
    private float getFl() { return seekFl.getProgress() / FLOAT_DIVIDER; }
    private float getFh() { return seekFh.getProgress() / FLOAT_DIVIDER; }
    private float getSampling() {
        return seekSampling.getProgress() / FLOAT_DIVIDER;
    }
    private float getChromAtt() {
        return seekChromAtt.getProgress() / FLOAT_DIVIDER;
    }
    private float getR1() {
        return seekR1.getProgress() / FLOAT_DIVIDER;
    }
    private float getR2() {
        return seekR2.getProgress() / FLOAT_DIVIDER;
    }
}
