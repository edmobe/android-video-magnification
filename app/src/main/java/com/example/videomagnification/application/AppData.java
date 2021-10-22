package com.example.videomagnification.application;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;

import com.example.videomagnification.utils.paths.PathManager;

import org.apache.commons.io.FilenameUtils;

public class AppData {

    // INPUT AND OUTPUT PATHS AND URI'S
    private Uri inputVideoUri;
    private final String videoDir;
    private String videoName;
    private Uri finalMp4VideoUri;
    private String processedVideoPath;

    // CONVERSION
    private int conversionType;

    // VIDEO METADATA
    private int imageWidth;
    private int imageHeight;

    // ROI
    private int roiX;
    private int roiY;

    // VIDEO MAGNIFICATION PARAMETERS
    private int selectedAlgorithmOption;
    private int alpha;
    private int lambda;
    private int level;
    private float fl;
    private float fh;
    private float sampling;
    private float chromAtt;
    private float r1;
    private float r2;

    // FILENAMES OBJECT
    private PathManager pathManager;

    AppData(ContentResolver resolver) {
        // TODO: VALIDATE INITIAL NULL VALUES
        this.videoDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + "/video-magnification/";
        pathManager = new PathManager(resolver);
    }

    // ================= PATH OPERATIONS =================

    public String getVideoName() {
        if (videoName == null) {
            videoName =
                    FilenameUtils.removeExtension(pathManager.getFileNameFromUri(inputVideoUri));
        }
        return videoName;
    }

    public String getInputVideoPathWithoutExtension() {
        return getVideoDir() + getVideoName();
    }

    private String getVideoPathFromExtension(String extension) {
        return getInputVideoPathWithoutExtension() + extension;
    }

    public String getAviVideoPath() {
        return getVideoPathFromExtension(".avi");
    }

    public String getMjpegVideoPath() {
        return getVideoPathFromExtension(".mjpeg");
    }

    public String getMp4VideoPath() {
        return getVideoPathFromExtension(".mp4");
    }

    public String getCompressedVideoPath() {
        return getVideoPathFromExtension("_compressed.mp4");
    }

    // ================= GETTERS AND SETTERS =================

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getFl() {
        return fl;
    }

    public void setFl(float fl) {
        this.fl = fl;
    }

    public float getFh() {
        return fh;
    }

    public void setFh(float fh) {
        this.fh = fh;
    }

    public float getSampling() {
        return sampling;
    }

    public void setSampling(float sampling) {
        this.sampling = sampling;
    }

    public float getChromAtt() {
        return chromAtt;
    }

    public void setChromAtt(float chromAtt) {
        this.chromAtt = chromAtt;
    }

    public float getR1() {
        return r1;
    }

    public void setR1(float r1) {
        this.r1 = r1;
    }

    public float getR2() {
        return r2;
    }

    public void setR2(float r2) {
        this.r2 = r2;
    }

    public int getSelectedAlgorithmOption() {
        return selectedAlgorithmOption;
    }

    public void setSelectedAlgorithmOption(int selectedAlgorithmOption) {
        this.selectedAlgorithmOption = selectedAlgorithmOption;
    }

    public int getRoiX() {
        return roiX;
    }

    public void setRoiX(int roiX) {
        this.roiX = roiX;
    }

    public int getRoiY() {
        return roiY;
    }

    public void setRoiY(int roiY) {
        this.roiY = roiY;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getProcessedVideoPath() {
        return processedVideoPath;
    }

    public void setProcessedVideoPath(String processedVideoUri) {
        this.processedVideoPath = processedVideoUri;
    }

    public String getVideoDir() {
        return videoDir;
    }

    public Uri getFinalMp4VideoUri() {
        return finalMp4VideoUri;
    }

    public void setFinalMp4VideoUri(Uri finalMp4VideoUri) {
        this.finalMp4VideoUri = finalMp4VideoUri;
    }

    public Uri getInputVideoUri() {
        return inputVideoUri;
    }

    public void setInputVideoUri(Uri inputVideoUri) {
        this.inputVideoUri = inputVideoUri;
    }

    public int getConversionType() {
        return conversionType;
    }

    public void setConversionType(int conversionType) {
        this.conversionType = conversionType;
    }
}
