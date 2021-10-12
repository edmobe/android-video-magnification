package com.example.videomagnification.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private static AppData appData;
    private static Context context;
    // TODO: Define thread count
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public void onCreate() {
        super.onCreate();
        appData = new AppData(getContentResolver());
        App.context = getApplicationContext();
    }

    public static AppData getAppData() { return appData; }

    public static Context getAppContext() {
        return App.context;
    }

    public static void displayShortToast(String string) {
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
