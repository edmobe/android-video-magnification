package com.example.videomagnification.utils.paths;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class PathManager {

    static ContentResolver resolver;

    public PathManager(ContentResolver resolver) {
        PathManager.resolver = resolver;
    }

    public String getFileNameFromUri(Uri inputVideoUri) {
        String name = null;

        Cursor metadataCursor = resolver.query(
                inputVideoUri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null, null, null);

        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    name = metadataCursor.getString(0);
                }
            } finally {
                metadataCursor.close();
            }
        }

        return name;
    }

    //    public String getNameFromUri(Uri contentUri) {
//        Cursor c = resolver.query(contentUri,
//                null, null, null, null);
//        c.moveToFirst();
//        String name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//        c.close();
//        return FilenameUtils.getBaseName(name);
//    }
//
//    public String getRealPathFromURI(Context context, Uri contentUri) {
//        Cursor cursor = null;
//        try {
//            String[] proj = { MediaStore.Images.Media.DATA };
//            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }
//
//    /**
//     * Gets the corresponding path to a file from the given content:// URI
//     * @param selectedVideoUri The content:// URI to find the file path from
//     * @param contentResolver The content resolver to use to perform the query.
//     * @return the file path as a string
//     */
//    public String getFilePathFromContentUri(Uri selectedVideoUri,
//                                            ContentResolver contentResolver) {
//        String filePath;
//        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
//
//        Cursor cursor = contentResolver.query(
//                selectedVideoUri, filePathColumn, null, null, null);
//        cursor.moveToFirst();
//
//        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//        filePath = cursor.getString(columnIndex);
//        cursor.close();
//        return filePath;
//    }

//    public String getFullPathFromUri(Uri contentUri) {
//        String filePath = "";
//        String wholeID = DocumentsContract.getDocumentId(contentUri);
//
//        // Split at colon, use second item in the array
//        String id = wholeID.split(":")[1];
//
//        String[] column = { MediaStore.Video.Media.DATA };
//
//        // where id is equal to
//        String sel = MediaStore.Video.Media._ID + "=?";
//
//        Cursor cursor = resolver.query(
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                column, sel, new String[]{ id }, null);
//
//        int columnIndex = cursor.getColumnIndex(column[0]);
//
//        if (cursor.moveToFirst()) {
//            filePath = cursor.getString(columnIndex);
//        }
//        cursor.close();
//        return filePath;
//    }

}
