package com.example.videomagnification;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Home extends AppCompatActivity {

    private Button btnOpen;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int PICK_AVI_VIDEO = 2;
    private static final String outputDir = "/video-magnification/";
    private Uri videoPath;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(v -> {
            if (arePermissionsGranted()) {
                // You can use the API that requires the permission.
                videoPicker();
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissions(new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSIONS);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                // The result data contains a URI for the document or directory that
                // the user selected.
                if (resultData != null) {
                    videoPath = resultData.getData();
                    Intent videoEditorActivity = new Intent(getApplicationContext(),
                            VideoEditor.class);
                    String outputFileName = getFullPathFromUri(videoPath);
                    //outputFileName = "/" + FilenameUtils.getPath(outputFileName);
                    //outputFileName += "baby.avi";
                    outputFileName = Environment.getExternalStorageDirectory().toString() +
                            "/baby.avi";
                    //outputFileName = FilenameUtils.removeExtension(outputFileName);
                    //outputFileName += ".avi";
                    Log.d("Native lib", "Root dir: " +
                            Environment.getExternalStorageDirectory().toString());
                    Log.d("Native lib", "Output file name: " + outputFileName);
                    convertVideo(videoPath, Uri.fromFile(new File(outputFileName)));
                    videoEditorActivity.putExtra(getString(R.string.video_file_path),
                            getFullPathFromUri(videoPath));
                            //"/storage/6531-3531/vid_avi/baby.avi");
                            //"/storage/self/primary/Download/sample.txt");
                            //videoPath.getPath());
                            //videoPath.toString());
                    startActivity(videoEditorActivity);

                } else {
                    displayShortToast("Please select a video file.");
                }
            } else {
                displayShortToast("Unknown request code: " + requestCode + ".");
            }

        } else if (resultCode == 0) {
            displayShortToast("Please select a video file.");
        } else {
            displayShortToast("File opener error. Result code: " + resultCode + ".");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    videoPicker();
                } else {
                    displayShortToast("Error: all the requested permissions are needed.");
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    private Uri convertVideo(Uri inputVideoUri, Uri outputVideoUri) {
        try {
            boolean createdFolder = false;
            File outputsFolder = new File(
                    Environment.getExternalStorageDirectory().getPath() + outputDir);
            if (!outputsFolder.exists()) {
                createdFolder = outputsFolder.mkdir();
            }
            if (!createdFolder) {
                displayShortToast("Error creating the output video directory!");
                Log.e("Native lib", "Error creating the folder");
                return null;
            }
            Log.d("Native lib", "Output video URI: " + outputVideoUri.getPath());
            String inputVideoPath = FFmpegKitConfig.getSafParameterForRead(
                    this, inputVideoUri);
            String inputBaseName = FilenameUtils.getBaseName(inputVideoPath);
            String midVideoPath = Environment.getExternalStorageDirectory().getPath() +
                    outputDir + inputBaseName + ".mjpeg";
            FFmpegSession session1 = FFmpegKit.execute(
                    "-i " + inputVideoPath + " -vcodec mjpeg " + midVideoPath);
            Log.d("Native lib", "Session 1 info: " + session1.getAllLogsAsString());
            Log.d("Native lib", "Converted video from " + inputVideoPath  +
                    " to " + midVideoPath);
            String outputVideoPath = Environment.getExternalStorageDirectory().getPath() +
                    outputDir + inputBaseName + ".avi";
            FFmpegSession session2 = FFmpegKit.execute(
                    "-i " + midVideoPath+ " -vcodec mjpeg " + outputVideoPath);
            Log.d("Native lib", "Session 2 info: " + session2.getAllLogsAsString());
            Log.d("Native lib", "Converted video from " + midVideoPath +
                    " to " + outputVideoPath);
            return outputVideoUri;
        } catch (Error e) {
            Log.d("Native lib", e.getLocalizedMessage());
            return null;
        }
    }

    private void videoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/mp4");
        startActivityForResult(intent, PICK_AVI_VIDEO);
    }

    private String getFullPathFromUri(Uri contentUri) {
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

    private void displayShortToast(String string) {
        Toast.makeText(getApplicationContext(),
                string,
                Toast.LENGTH_SHORT).show();
    }
}