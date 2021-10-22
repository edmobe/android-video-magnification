package com.example.videomagnification.gui.processing;

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
import com.example.videomagnification.gui.input.HomeActivity;
import com.example.videomagnification.gui.input.VideoConverterActivity;
import com.example.videomagnification.processing.magnificators.MagnificationTask;

public class NativeLibManagerActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static TextView magnifierLog;
    public static ProgressBar progress;

    private Button buttonConvert;
    private Button buttonNewVideo;

    private ActivityMainBinding binding;
    private MagnificationTask magnificationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        magnifierLog = findViewById(R.id.text_magnify_log);
        progress = findViewById(R.id.progress_magnify);

        buttonConvert = findViewById(R.id.btn_convert_output);
        buttonConvert.setOnClickListener(v -> {
            startActivity(new Intent(NativeLibManagerActivity.this,
                            VideoConverterActivity.class));
        });

        buttonNewVideo = findViewById(R.id.btn_new_video);
        buttonNewVideo.setOnClickListener(v -> {
            startActivity(new Intent(NativeLibManagerActivity.this,
                    HomeActivity.class));
        });

        magnificationTask = new MagnificationTask(this);
        magnificationTask.execute();

    }

    public Button getButtonNewVideo() { return buttonNewVideo; }
    public Button getButtonConvert() {
        return buttonConvert;
    }

    public static void updateMagnifierLog(String string) {
        new Handler(Looper.getMainLooper()).post(() -> {
            NativeLibManagerActivity.magnifierLog.setText(string);
        });
    }

    public static void updateProgress(int progress) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (progress >= 100) {
                NativeLibManagerActivity.progress.setVisibility(View.INVISIBLE);
            } else {
                NativeLibManagerActivity.progress.setProgress(progress);
            }
        });
    }
}
