package com.example.videomagnification.application;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private static boolean handler;
    private static Context context;
    // TODO: Define thread count
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }

    public static ExecutorService getExecutorService() { return executorService; }

    public static void displayShortToast(String string) {
        handler = new Handler(Looper.getMainLooper()).post(() -> {
            Toast toast = Toast.makeText(getAppContext(),
                    string, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public String getNameFromUri(Uri contentUri) {
        Cursor c = getContentResolver().query(contentUri,
                null, null, null, null);
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        c.close();
        return FilenameUtils.getBaseName(name);
    }

    public String getFullPathFromUri(Uri contentUri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Video.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Video.Media._ID + "=?";

        Cursor cursor = this.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public void logDebug(String label, String text) {
        Log.d("Video Magnification", label + ": " + text);
    }

    public void logError(String label, String text) {
        Log.e("Video Magnification", label + ": " + text);
    }
}