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
import com.example.videomagnification.gui.input.VideoConverterActivity;
import com.example.videomagnification.application.App;
import com.example.videomagnification.databinding.ActivityMainBinding;
import com.example.videomagnification.magnificators.Magnificator;
import com.example.videomagnification.magnificators.MagnificatorGdownIdeal;

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

    private int gaussianId;
    private int laplacianIdealId;
    private int laplacianButterId;

    private class MagnificationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO: ERROR HANDLING
            try {
                Magnificator magnificator;
                String result = "error";
                int algorithmId = Integer.parseInt(params[0]);
                if (algorithmId == R.id.radio_gaussian_ideal) {
                    // TODO: Reproduce results for baby2 in S8:
                    // 150, 6, 136.8, 163.8, 25.22, 1, 292, 139, heart rate
                    // Baby 2: 150, 6, 2.33, 2.66, 30, 1, 294, 170
                    // Face 2: 150, 6, 1, 1.66, 30, 1, 294, 170
                    // Baby: 30, 16, 0.4, 3, 30, 0.1 -----> 24 bpm to 180 bpm
                    magnificator = new MagnificatorGdownIdeal(
                            App.getAppData().getAviVideoPath(),
                            App.getAppData().getVideoDir(),
                            App.getAppData().getAlpha(),
                            App.getAppData().getLevel(),
                            App.getAppData().getFl(),
                            App.getAppData().getFh(),
                            App.getAppData().getSampling(),
                            App.getAppData().getChromAtt(),
                            App.getAppData().getRoiX(),
                            App.getAppData().getRoiY());

//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 150, 6, 2.33, 2.66,
//                            30, 1, 294, 170);
                    // SAMSUNG PHONE
//                    // TEST 1
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 100, 6,
//                            60.0 / 60.0, 100.0 / 60.0, 30, 1, 132, 23);
//                    // TEST 2
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 80, 6,
//                            70.0 / 60.0, 90.0 / 60.0, 30, 1, 132, 23);
//                    // TEST 3
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 150, 6,
//                            80.0 / 60.0, 90.0 / 60.0, 30, 1, 132, 23);
                    // BLUESTACKS
                    // TEST 1
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 50, 6,
//                            60.0 / 60.0, 100.0 / 60.0, 30, 1, 132, 23);
//                    // TEST 2
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 80, 6,
//                            70.0 / 60.0, 90.0 / 60.0, 30, 1, 132, 23);
//                    // TEST 3
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 150, 6,
//                            80.0 / 60.0, 90.0 / 60.0, 30, 1, 132, 23);
                    // TEST 4
//                    magnificator = new MagnificatorGdownIdeal(
//                            videoPath, FilenameUtils.getPath(videoPath), 150, 6,
//                            70.0 / 60.0, 90.0 / 60.0, 30, 1, 538, 551);
                    result = magnificator.call();
                } else if (algorithmId == R.id.radio_laplacian_butterworth) {
//                    magnificator = new MagnificatorLpyrButter(
//                            videoPath, FilenameUtils.getPath(videoPath), 30, 16,
//                            0.4, 3, 30, 0.1, 308, 142);
//                    result = magnificator.call();
                } else {
                    // Unknown algorithm

                }
                return result;

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

        gaussianId = R.id.radio_gaussian_ideal;
        laplacianButterId = R.id.radio_laplacian_butterworth;

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
