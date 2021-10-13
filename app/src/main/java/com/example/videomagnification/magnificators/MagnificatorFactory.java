package com.example.videomagnification.magnificators;

import com.example.videomagnification.R;
import com.example.videomagnification.application.App;

public class MagnificatorFactory {

    public Magnificator createMagnificator() {
        int algorithmId = App.getAppData().getSelectedAlgorithmOption();
        if (algorithmId == R.id.radio_gaussian_ideal) {
            // TODO: Reproduce results for baby2 in S8:
            // 150, 6, 136.8, 163.8, 25.22, 1, 292, 139, heart rate
            // Baby 2: 150, 6, 2.33, 2.66, 30, 1, 294, 170
            // Face 2: 150, 6, 1, 1.66, 30, 1, 294, 170
            // Baby: 30, 16, 0.4, 3, 30, 0.1 -----> 24 bpm to 180 bpm
            return new MagnificatorGdownIdeal(
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
        } else if (algorithmId == R.id.radio_laplacian_butterworth) {
            return new MagnificatorLpyrButter(
                    App.getAppData().getAviVideoPath(),
                    App.getAppData().getVideoDir(),
                    App.getAppData().getAlpha(),
                    App.getAppData().getLambda(),
                    App.getAppData().getFl(),
                    App.getAppData().getFh(),
                    App.getAppData().getSampling(),
                    App.getAppData().getChromAtt(),
                    App.getAppData().getRoiX(),
                    App.getAppData().getRoiY()
            );
        }
        // Unknown algorithm

        return null;
    }

}
