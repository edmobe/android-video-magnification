package com.example.videomagnification.gui.interaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;
import com.example.videomagnification.gui.processing.NativeLibManagerActivity;
import com.example.videomagnification.utils.seekbar.AccurateSeekBar;

public class VideoEditorParametersActivity extends AppCompatActivity {

    private final float FLOAT_DIVIDER = 100f;

    private TextView videoPathTextView;
    private TextView algorithmTextView;
    private TextView extractTextView;
    private TextView roiTextView;

    AccurateSeekBar[] integerSeeks;
    AccurateSeekBar[] floatSeeks;
    private AccurateSeekBar seekAlpha;
    private AccurateSeekBar seekLambda;
    private AccurateSeekBar seekLevel;
    private AccurateSeekBar seekFl;
    private AccurateSeekBar seekFh;
    private AccurateSeekBar seekSampling;
    private AccurateSeekBar seekChromAtt;
    private AccurateSeekBar seekR1;
    private AccurateSeekBar seekR2;

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

    private void getAllParameters() {
        // SeekBars
        seekAlpha = findViewById(R.id.seek_alpha);
        seekLambda = findViewById(R.id.seek_lambda);
        seekLevel = findViewById(R.id.seek_level);
        seekFl = findViewById(R.id.seek_fl);
        seekFh = findViewById(R.id.seek_fh);
        seekSampling = findViewById(R.id.seek_sampling);
        seekChromAtt = (AccurateSeekBar) findViewById(R.id.seek_chrom_att);
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
        textFl.setText(String.format("%.02f", 60 * getFl()));
        textFh = findViewById(R.id.text_fh);
        textFh.setText(String.format("%.02f", 60 * getFh()));
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
        videoPathTextView.setText(((App) getApplication()).getAppData().getMp4VideoPath());
        extractTextView = findViewById(R.id.text_extract);
        if (((App) getApplication()).getAppData().getSelectedAlgorithmOption() ==
                R.id.radio_gaussian_ideal)
            extractTextView.setText("Heart rate");
        else if (((App) getApplication()).getAppData().getSelectedAlgorithmOption() ==
                R.id.radio_laplacian_butterworth)
            extractTextView.setText("Respiratory rate");
        else {
            extractTextView.setText("Unable to get data");
            start.setEnabled(false);
        }
        roiTextView = findViewById(R.id.text_roi);
        String roi = "X = " + ((App) getApplication()).getAppData().getRoiX() + " Y = " +
                ((App) getApplication()).getAppData().getRoiY();
        roiTextView.setText(roi);

        // Arrays
        integerSeeks = new AccurateSeekBar[]{seekAlpha, seekLambda, seekLevel};
        floatSeeks = new AccurateSeekBar[]{seekSampling, seekChromAtt, seekR1, seekR2};
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
        int algorithmRadioButtonId =
                ((App) getApplication()).getAppData().getSelectedAlgorithmOption();

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
                    new Intent(getApplicationContext(), VideoEditorParametersActivity.class);
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
                    textFh.setText(String.format("%.02f", 60 * getFh()));
                }
                textFl.setText(String.format("%.02f", 60 * progress / FLOAT_DIVIDER));
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
                    textFl.setText(String.format("%.02f", 60 * getFl()));
                }
                textFh.setText(String.format("%.02f", 60 * progress / FLOAT_DIVIDER));
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

        getAllParameters();
        setParametersBasedOnAlgorithm();
        setSeekBarListeners();

        start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            ((App) getApplication()).getAppData().setAlpha(getAlpha());
            ((App) getApplication()).getAppData().setLambda(getLambda());
            ((App) getApplication()).getAppData().setLevel(getLevel());
            ((App) getApplication()).getAppData().setFl(getFl());
            ((App) getApplication()).getAppData().setFh(getFh());
            ((App) getApplication()).getAppData().setSampling(getSampling());
            ((App) getApplication()).getAppData().setChromAtt(getChromAtt());
            ((App) getApplication()).getAppData().setR1(getR1());
            ((App) getApplication()).getAppData().setR2(getR2());

            startActivity(new Intent(getApplicationContext(), NativeLibManagerActivity.class));
        });
    }

    private int getAlpha() { return seekAlpha.getProgress(); }
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
