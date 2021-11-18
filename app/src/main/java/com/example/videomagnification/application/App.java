package com.example.videomagnification.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {

    private AppData appData;

    public void onCreate() {
        super.onCreate();
        appData = new AppData(getContentResolver());
    }

    public AppData getAppData() { return appData; }

    public Context getAppContext() {
        return super.getApplicationContext();
    }

    public void displayShortToast(String string) {
        // Run on main loop
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast toast = Toast.makeText(getAppContext(),
                    string, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void logDebug(String label, String text) {
        Log.d("Video Magnification", label + ": " + text);
    }

    public static void logError(String label, String text) {
        Log.e("Video Magnification", label + ": " + text);
    }
}
