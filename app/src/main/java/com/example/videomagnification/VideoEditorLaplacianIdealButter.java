package com.example.videomagnification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoEditorLaplacianIdealButter extends AppCompatActivity {

    private String videoPath;
    private TextView videoPathTextView;

    private SeekBar seekAlpha;
    private SeekBar seekLambda;
    private SeekBar seekFl;
    private SeekBar seekFh;
    private SeekBar seekSampling;
    private SeekBar seekChromAtt;

    private TextView textAlpha;
    private TextView textLambda;
    private TextView textFl;
    private TextView textFh;
    private TextView textSampling;
    private TextView textChromAtt;

    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor_laplacian_ideal_butter);

        Intent intent = getIntent(); // gets the previously created intent
        videoPath = intent.getStringExtra("videoPath");

        videoPathTextView = findViewById(R.id.videoPath);
        videoPathTextView.setText(videoPath);

        seekAlpha = findViewById(R.id.seek_alpha);
        seekLambda = findViewById(R.id.seek_lambda);
        seekFl = findViewById(R.id.seek_fl);
        seekFh = findViewById(R.id.seek_fh);
        seekSampling = findViewById(R.id.seek_sampling);
        seekChromAtt = findViewById(R.id.seek_chrom_att);

        textAlpha = findViewById(R.id.text_alpha);
        textAlpha.setText(String.valueOf(getAlpha()));
        textLambda = findViewById(R.id.text_lambda);
        textLambda.setText(String.valueOf(getLambda()));
        textFl = findViewById(R.id.text_fl);
        textFl.setText(String.valueOf(getFl()));
        textFh = findViewById(R.id.text_fh);
        textFh.setText(String.valueOf(getFh()));
        textSampling = findViewById(R.id.text_sampling);
        textSampling.setText(String.valueOf(getSampling()));
        textChromAtt = findViewById(R.id.text_chrom_att);
        textChromAtt.setText(String.valueOf(getChromAtt()));

        SeekBar[] integerSeeks = {seekAlpha, seekLambda};
        SeekBar[] floatSeeks = {seekSampling, seekChromAtt};
        TextView[] integerTextViews = {textAlpha, textLambda};
        TextView[] floatTextViews = {textSampling, textChromAtt};

        start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.putExtra("videoPath", videoPath);
            startActivity(mainActivityIntent);
        });

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
                    floatTextViews[textViewIndex].setText(Float.valueOf(progress / 10f).toString());
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
                    textFh.setText(Float.valueOf(getFh()).toString());
                }
                textFl.setText(Float.valueOf(progress / 10f).toString());
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
                    textFl.setText(Float.valueOf(getFl()).toString());
                }
                textFh.setText(Float.valueOf(progress / 10f).toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int getAlpha() {
        return seekAlpha.getProgress();
    }
    private int getLambda() {
        return seekLambda.getProgress();
    }
    private float getFl() {
        return seekFl.getProgress() / 10f;
    }
    private float getFh() {
        return seekFh.getProgress() / 10f;
    }
    private float getSampling() {
        return seekSampling.getProgress() / 10f;
    }
    private float getChromAtt() {
        return seekChromAtt.getProgress() / 10f;
    }
}
