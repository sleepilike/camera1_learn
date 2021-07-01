package com.example.testone.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast mToast = null;

    /**
     *  提示轮子
     * @param context
     * @param msg
     */
    public static void showShortToast(Context context,String msg){
        showToastMessage(context,msg,Toast.LENGTH_SHORT);
    }

    /**
     * 提示
     * @param context
     * @param msg
     * @param duration
     */
    public static void showToastMessage(Context context,String msg,int duration){
        if(mToast == null){
            mToast = Toast.makeText(context,msg,duration);
        }else {
            mToast.setText(msg);
            mToast.setDuration(duration);
        }
        mToast.show();
    }
}
