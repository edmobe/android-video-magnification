package com.example.videomagnification.gui.processing;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;
import com.example.videomagnification.databinding.ActivityMainBinding;
import com.example.videomagnification.gui.input.VideoConverterActivity;
import com.example.videomagnification.magnificators.Magnificator;
import com.example.videomagnification.magnificators.MagnificatorFactory;

public class NativeLibManagerActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static TextView magnifierLog;
    public static ProgressBar progress;
    private Button buttonConvert;
    private String finalState;

    private ActivityMainBinding binding;

    private MagnificatorFactory magnificatorFactory;

    private class MagnificationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO: ERROR HANDLING
            try {
                Magnificator magnificator = magnificatorFactory.createMagnificator();
                return magnificator.magnify();

            } catch (Exception e) {
                return "error";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!result.equals("error")) {
                finalState = result;
                buttonConvert.setVisibility(View.VISIBLE);
            }
        }
    }


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
            App.getAppData().setConversionType(1);
            App.getAppData().setProcessedVideoPath(finalState);
            NativeLibManagerActivity.this.startActivity(
                    new Intent(NativeLibManagerActivity.this,
                            VideoConverterActivity.class));
        });

        magnificatorFactory = new MagnificatorFactory();

        new MagnificationTask().execute(String.valueOf(
                App.getAppData().getSelectedAlgorithmOption()));

    }

    public static void updateMagnifierLog(String string) {
        boolean handler = new Handler(Looper.getMainLooper()).post(() -> {
            NativeLibManagerActivity.magnifierLog.setText(string);
        });
    }

    public static void updateProgress(int progress) {
        boolean handler = new Handler(Looper.getMainLooper()).post(() -> {
            if (progress >= 100) {
                NativeLibManagerActivity.progress.setVisibility(View.INVISIBLE);
            } else {
                NativeLibManagerActivity.progress.setProgress(progress);
            }
        });
    }
}
