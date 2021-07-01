package com.example.testone.utils;

import android.os.Environment;

public class Configuration {

    //app内部存储
    public static String insidePath = "data/data/ com.example.testone/pic/";

   //外部路径
    public static String outPath = Environment.getExternalStorageDirectory()+"/takePic";
}
