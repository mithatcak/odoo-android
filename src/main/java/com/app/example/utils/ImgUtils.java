package com.app.example.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImgUtils {

    private static String imgPath;

    /**
     * save img
     */
    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // create Directory
        //String storePath = Environment.getExternalStorageDirectory() + "/my_img";
        File imgPath = context.getExternalFilesDir("img");

        if (!imgPath.exists()) {
            imgPath.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(imgPath, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            // IO stream is used to compress and save images
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            // Insert the file into the system gallery
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            // Save the image and send a broadcast notification to update the database
            Uri uri = Uri.fromFile(file);
            // context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            SPUtils.put(context, "path", uri.getPath());
            if (isSuccess) {
                notifySystemToScan(imgPath.getPath(), context);
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * The Bitmap object is obtained based on the url path of the image
     *
     * @param url
     * @return
     */
    public static Bitmap decodeUriAsBitmapFromNet(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap strAsBitmapFromNet(String imgPath) {
        Bitmap bitmap = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(imgPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap = BitmapFactory.decodeStream(fs);
        return bitmap;
    }


    public static void notifySystemToScan(String filePath, Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);

        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
