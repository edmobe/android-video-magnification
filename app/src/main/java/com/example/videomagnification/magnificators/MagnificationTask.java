package com.example.videomagnification.magnificators;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.example.videomagnification.application.App;
import com.example.videomagnification.gui.processing.NativeLibManagerActivity;

public class MagnificationTask extends AsyncTask<String, Void, String> {

    private Activity context;
    private MagnificatorFactory magnificatorFactory;

    public MagnificationTask(Activity context) {
        this.context = context;
        magnificatorFactory = new MagnificatorFactory(context);
    }

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
            ((App) context.getApplication()).getAppData().setProcessedVideoPath(result);
            ((App) context.getApplication()).getAppData().setConversionType(1);
            ((NativeLibManagerActivity) context).getButtonConvert().setVisibility(View.VISIBLE);
        }
    }
}
