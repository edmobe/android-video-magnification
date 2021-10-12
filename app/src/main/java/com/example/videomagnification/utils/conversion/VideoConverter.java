package com.example.videomagnification.utils.conversion;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.example.videomagnification.application.App;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class VideoConverter {

    private Context applicationContext;

    public VideoConverter(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void deleteFiles() {
        File dir = new File(App.getAppData().getVideoDir());
        //Checking the directory exists
        if (!dir.exists())
            return;
        //Getting the list of all the files in the specific  direcotry
        File fList[] = dir.listFiles();

        for (File f : fList) {
            //checking the extension of the file with endsWith method.
            if (
                    f.getName().endsWith(".avi") ||
                            f.getName().endsWith(".mjpeg") ||
                            f.getName().endsWith("_compressed.mp4")) {
                // TODO: ERROR HANDLING
                f.delete();
            }

        }

    }

    public boolean createDirectoryIfNeeded() {
        File outputsFolder = new File(App.getAppData().getVideoDir());
        if (!outputsFolder.exists()) {
            try {
                // TODO: ERROR HANDLING
                outputsFolder.mkdir();
                App.logDebug("Create directory",
                        "Created directory for the first time: " +
                                App.getAppData().getVideoDir());
                return true;
            } catch (Exception e) {
                App.displayShortToast(
                        "Error creating the output video directory!");
                App.logError(
                        "Native lib", "Error creating the folder: " +
                                e.getLocalizedMessage());
                return false;
            }
        }
        App.logDebug("Create directory",
                "No need to create the directory");
        return true;
    }

    public String convertMp4ToMjpeg(Uri inputVideoUri) {
        // TODO: Error handling
        String inputVideoPath = FFmpegKitConfig.getSafParameterForRead(
                applicationContext, inputVideoUri);

        App.logDebug(
                "Native lib", "Input video path: " + inputVideoPath);


        String midVideoPath = App.getAppData().getMjpegVideoPath();

        // Compress video
        // TODO: validate that the video is less than 20 seconds long
        // TODO: put conversion in another method
        // TODO: display progress bar to user more accurately
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(applicationContext, inputVideoUri);
        int width = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        String compressedVideoPath =
                App.getAppData().getCompressedVideoPath();
        String scale;

        // Compress
        if (width * height > 640 * 640) {
            // Must resize the video
            if (width > height) {
                // Horizontal
                scale = " -vf scale=640:-2 ";
            } else {
                // Vertical or squared
                scale = " -vf scale=-2:640 ";
            }
        } else {
            // Just avoid divisible by 2 error
            // https://stackoverflow.com/questions/20847674/ffmpeg-libx264-height-not-divisible-by-2
            scale = " -vf \"crop=trunc(iw/2)*2:trunc(ih/2)*2\" ";
        }

        // Horizontal
        FFmpegSession resizeSession = FFmpegKit.execute(
                "-y -i " + inputVideoPath + scale + " -q:v 2 " + compressedVideoPath);

        App.logDebug(
                "Native lib", "Resize session info: " +
                        resizeSession.getAllLogsAsString());
        App.logDebug(
                "Native lib", "Successfully resized video");

        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + compressedVideoPath + " -q:v 2 -vcodec mjpeg " + midVideoPath);
        App.logDebug(
                "Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        App.logDebug(
                "Native lib", "Converted video from " + inputVideoPath  +
                        " to " + midVideoPath);

        return midVideoPath;
    }

    public String convertAviToMjpeg(String inputVideoPath) {
        // TODO: Error handling
        App.logDebug(
                "Native lib", "Input video path: " + inputVideoPath);
        String midVideoPath = FilenameUtils.removeExtension(inputVideoPath) + ".mjpeg";
        FFmpegSession session1 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -q:v 2 -vcodec libx265 -acodec aac " + midVideoPath);
        App.logDebug(
                "Native lib", "Session 1 info: " + session1.getAllLogsAsString());
        App.logDebug(
                "Native lib", "Converted video from " + inputVideoPath  +
                        " to " + midVideoPath);

        return midVideoPath;
    }

    public Uri convertMjpegToAvi(String inputVideoPath) {
        // TODO: Error handling
        // TODO: remove conversion files when finished
        String outputVideoPath = App.getAppData().getAviVideoPath();
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath + " -q:v 2 -r 30 -vcodec mjpeg " + outputVideoPath);
        App.logDebug(
                "Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        App.logDebug(
                "Native lib", "Converted video from " + inputVideoPath +
                        " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }



    public Uri convertMjpegToMp4(String inputVideoPath) {
        // TODO: Error handling
        String outputVideoPath = FilenameUtils.removeExtension(inputVideoPath) + ".mp4";
        FFmpegSession session2 = FFmpegKit.execute(
                "-y -i " + inputVideoPath+ " -q:v 2 -vcodec libx265 -acodec aac " + outputVideoPath);
        App.logDebug(
                "Native lib", "Session 2 info: " + session2.getAllLogsAsString());
        App.logDebug(
                "Native lib", "Converted video from " + inputVideoPath +
                        " to " + outputVideoPath);
        return Uri.parse(outputVideoPath);
    }

}
