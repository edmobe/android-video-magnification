package com.example.videomagnification.processing.magnificators;

import android.app.Activity;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;

public class MagnificatorFactory {

    private Activity context;

    public MagnificatorFactory(Activity context) {
        this.context = context;
    }

    public Magnificator createMagnificator() {
        int algorithmId = ((App) context.getApplication()).getAppData().getSelectedAlgorithmOption();
        if (algorithmId == R.id.radio_gaussian_ideal) {
            // TODO: Reproduce results for baby2 in S8:
            // 150, 6, 136.8, 163.8, 25.22, 1, 292, 139, heart rate
            // Baby 2: 150, 6, 2.33, 2.66, 30, 1, 294, 170
            // Face 2: 150, 6, 1, 1.66, 30, 1, 294, 170
            // Baby: 30, 16, 0.4, 3, 30, 0.1 -----> 24 bpm to 180 bpm
            return new MagnificatorGdownIdeal(
                    ((App) context.getApplication()).getAppData().getAviVideoPath(),
                    ((App) context.getApplication()).getAppData().getVideoDir(),
                    ((App) context.getApplication()).getAppData().getAlpha(),
                    ((App) context.getApplication()).getAppData().getLevel(),
                    ((App) context.getApplication()).getAppData().getFl(),
                    ((App) context.getApplication()).getAppData().getFh(),
                    ((App) context.getApplication()).getAppData().getSampling(),
                    ((App) context.getApplication()).getAppData().getChromAtt(),
                    ((App) context.getApplication()).getAppData().getRoiX(),
                    ((App) context.getApplication()).getAppData().getRoiY());
        } else if (algorithmId == R.id.radio_laplacian_butterworth) {
//            return new MagnificatorLpyrButter(
//                    ((App) context.getApplication()).getAppData().getAviVideoPath(),
//                    ((App) context.getApplication()).getAppData().getVideoDir(),
//                    ((App) context.getApplication()).getAppData().getAlpha(),
//                    ((App) context.getApplication()).getAppData().getLambda(),
//                    ((App) context.getApplication()).getAppData().getFl(),
//                    ((App) context.getApplication()).getAppData().getFh(),
//                    ((App) context.getApplication()).getAppData().getSampling(),
//                    ((App) context.getApplication()).getAppData().getChromAtt(),
//                    ((App) context.getApplication()).getAppData().getRoiX(),
//                    ((App) context.getApplication()).getAppData().getRoiY()
//            );
            return new MagnificatorLpyrButter(
                    ((App) context.getApplication()).getAppData().getAviVideoPath(),
                    ((App) context.getApplication()).getAppData().getVideoDir(),
                    30,
                    16,
                    0.4,
                    3,
                    30,
                    0.1,
                    ((App) context.getApplication()).getAppData().getRoiX(),
                    ((App) context.getApplication()).getAppData().getRoiY()
            );
        }
        // Unknown algorithm

        return null;
    }

}
