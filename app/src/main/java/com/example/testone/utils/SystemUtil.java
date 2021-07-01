package com.example.testone.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemUtil {


    /**
     * 获取包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context){
        try{
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(),0
            );
            return packageInfo.packageName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化时间
     * @param time
     * @return "yyyy-MM-dd HH:mm:ss"
     */
    public static String formatTime(long time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(time);
        return formatter.format(curDate);

    }

    /**
     * 格式化时间
     * @param time 时间
     * @param file 文件命名
     * @return
     */
    public static String formatTime(long time,String file){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(time);
        return formatter.format(curDate) + file;

    }

    /**
     * 将图片保存在相册
     * @param path 路径
     * @param name 文件名称
     * @param context
     */
    public static void saveAlbum(String path,String name,Context context){
        //把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),path,name,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }


}
