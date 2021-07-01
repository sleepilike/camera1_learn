package com.example.testone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.testone.holder.CameraPresenter;
import com.example.testone.view.CircleButtonView;

import java.util.ArrayList;
import java.util.List;

public class SelfCameraActivity extends AppCompatActivity implements CameraPresenter.CameraCallBack{

    private CircleButtonView circleButtonView;
    private ImageView iv_photo;
    //图片List
    private List<String> photoList;


    //逻辑层
    private CameraPresenter cameraPresenter;
    private SurfaceView surfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_camera);
        initBind();
        initListener();
        cameraPresenter = new CameraPresenter(this,surfaceView);
        cameraPresenter.setFrontOrBack(Camera.CameraInfo.CAMERA_FACING_BACK);
        //添加监听
        cameraPresenter.setCameraCallBack(this);
    }

    public void initBind(){
        surfaceView = findViewById(R.id.sf_camera);
        circleButtonView = findViewById(R.id.tv_takephoto);
    }
    private void initListener(){
        circleButtonView.setOnClickListener(new CircleButtonView.OnClickListener() {
            @Override
            public void onClick() {
                cameraPresenter.takePicture();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraPresenter != null){
            cameraPresenter.releaseCamera();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onTakePicture(byte[] data, Camera Camera) {

    }

    /**
     * 拍照后照片返回的路径
     * @param imagePath
     */
    @Override
    public void getPhotoFile(String imagePath) {

        Glide.with(this).load(imagePath)
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .override(iv_photo.getWidth(), iv_photo.getHeight())
                        .error(R.drawable.default_person_icon))
                .into(iv_photo);
        photoList.add(imagePath);
    }

    @Override
    public void onFaceDetect(ArrayList<RectF> rectFArrayList, Camera camera) {

    }
}