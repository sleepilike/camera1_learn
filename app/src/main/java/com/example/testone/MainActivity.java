package com.example.testone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.testone.utils.SystemUtil;
import com.example.testone.utils.ToastUtil;
import com.example.testone.utils.permissionUtil.Permissions;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    //调用系统相机
    private Button btn_system_camera;
    //调用自定义相机
    private Button btn_self_camera;

    //调用系统拍照返回的uri
    private Uri uri;
    //拍照照片路径
    private File cameraSavePath;
    //展示照片的View
    private ImageView iv_photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBind();
        initListener();
        checkNeedPermissions();


    }

    private void initBind(){
        btn_system_camera = (Button)findViewById(R.id.btn_system_camera);
        btn_self_camera = (Button)findViewById(R.id.btn_self_camera);
        iv_photo = (ImageView)findViewById(R.id.iv_photo);
    }

    private void initListener(){
        btn_system_camera.setOnClickListener(this);
        btn_self_camera.setOnClickListener(this);
    }
    /**
     * 检查所需要的权限
     */
    private void checkNeedPermissions(){


        //Android6.0 以上 需要动态申请
        //Build.VERSION.SDK_INT 获取手机的操作系统版本号
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", "com.zhengyuan.emcarsend"));
        if (permission) {
            //"有这个权限"
        } else {
            //"没有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
            }
        }
        if(Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    !=  PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                },1);
            }
        }

    }

    /**
     * 获取权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: //获取权限  验证
                if(grantResults.length > 2){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                            if (grantResults[2] == PackageManager.PERMISSION_GRANTED){

                            }else
                                //拒绝则弹出提示框
                                Permissions.showPermissionSettingDialog(this,Manifest.permission.RECORD_AUDIO);
                        }else {
                            Permissions.showPermissionSettingDialog(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                    }else {
                        Permissions.showPermissionSettingDialog(this,Manifest.permission.CAMERA);
                    }
                }else{
                    ToastUtil.showShortToast(this,"请重试~~");
                }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_system_camera:
                Log.d("点击按钮", "onClick: dianji ");
                goSystemCamera();
                break;
            case R.id.btn_self_camera:
                startActivity(new Intent(this,SelfCameraActivity.class));
        }
    }


    private void goSystemCamera(){
        //在根目录创建jpg文件
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() +".jpg");
        //指定跳到系统拍照
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //适配Android 7.0以上版本应用私有目录限制被访问
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            uri = FileProvider.getUriForFile(this, SystemUtil.getPackageName(getApplicationContext()) + ".fileprovider",cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            //7.0以下
            uri = Uri.fromFile(cameraSavePath);
        }
        //指定ACTION为MediaStore.EXTRA_OUTPUT
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        //请求码赋值为1
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        String photoPath;
        if(requestCode == 1 && resultCode == RESULT_OK){
            //Android7.0以上
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                photoPath = String.valueOf(cameraSavePath);
            }else {
                photoPath = uri.getEncodedPath();
            }
            Log.d("拍照返回图片的路径:",photoPath);
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
            .override(iv_photo.getWidth(),iv_photo.getHeight())
            .error(R.drawable.ic_launcher_background)).into(iv_photo);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}