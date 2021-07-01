package com.example.testone.holder;


import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testone.utils.Configuration;
import com.example.testone.utils.SystemUtil;
import com.example.testone.utils.ThreadPoolUtil;
import com.example.testone.utils.ToastUtil;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 控制类
 */
public class CameraPresenter implements Camera.PreviewCallback {


    private Camera camera;
    private Camera.Parameters parameters;
    //自定义拍照界面
    private AppCompatActivity appCompatActivity;
    //图片预览
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    //摄像头id 前置1 后置0默认
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    //预览旋转角度
    private int orientation;


    //适配多种机型 分辨率 避免图片变形等
    //屏幕宽高
    private int screenWidth,screenHeight;

    //拍照存放的文件
    private File photosFile = null;
    private int photoNum = 0;

    private CameraCallBack cameraCallBack;
    //自定义回调
    public interface CameraCallBack{
        //预览回调
        void onPreviewFrame(byte[] data, Camera camera);
        //拍照回调
        void onTakePicture(byte[] data, Camera Camera);
        //拍照路径返回
        void getPhotoFile(String imagePath);
        //人脸检测回调
        void onFaceDetect(ArrayList<RectF> rectFArrayList,Camera camera);
        //拍照回调
    }
    Handler mHandler = new Handler(){
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cameraCallBack.getPhotoFile(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };



    public CameraPresenter(AppCompatActivity appCompatActivity, SurfaceView surfaceView){
        this.appCompatActivity = appCompatActivity;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        appCompatActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //获取手机屏幕的宽高  像素
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        Log.d("手机宽高尺寸:",screenWidth +"*"+screenHeight);
        //创建文件夹目录
        setUpFile();
        init();
    }

    private void init(){
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

                if(camera == null){
                    openCamera(cameraId);
                }
                //初始化相机参数后，开始预览
                startPreview();

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                //绘制执行
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        try{
            //根据所传入的SurfaceHolder对象来设置实时预览。
            camera.setPreviewDisplay(surfaceHolder);
            //调整预览的角度
            setCameraDisplayOrientation(appCompatActivity,cameraId,camera);
            camera.startPreview();
            //人脸检测
          //  startFaceDetect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测
     */
    private void startFaceDetect(){
        camera.startFaceDetection();
        camera.setFaceDetectionListener(new Camera.FaceDetectionListener(){

            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                cameraCallBack.onFaceDetect(transFrom(faces),camera);
                Log.d("CameraPresenter:", "检测到"+faces.length+"张人脸");
            }
        });
    }

    /**
     * 判断是否支持前/后摄像头 并打开
     * @param faceOrBack
     * @return
     */
    private boolean openCamera(int faceOrBack){
        boolean isSupportCamera  = isSupport(faceOrBack);
        if(isSupportCamera){
            try{
                camera  = Camera.open(faceOrBack);
                initParameters(camera);
                if(camera != null){
                    camera.setPreviewCallback(this);
                }
            }catch (Exception e){
                e.printStackTrace();
                ToastUtil.showShortToast(appCompatActivity,"打开相机失败！");
                return  false;
            }
        }
        return isSupportCamera;

    }

    /**
     * 判断是否支持某个相机
     * @param faceOrBack
     * @return
     */
    private boolean isSupport(int faceOrBack){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        //摄像头个数
        for(int i =0;i<Camera.getNumberOfCameras();i++){
            Camera.getCameraInfo(i,cameraInfo);
            if(cameraInfo.facing == faceOrBack){
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化相机参数
     * @param camera
     */
    private void initParameters(Camera camera){
        try{
            parameters = camera.getParameters();
            //预览格式
            parameters.setPreviewFormat(ImageFormat.NV21);
            //判断是否支持连续自动对焦图像
            if(isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }else if(isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)){
                //自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            setPreViewSize();
            setPictureSize();
            camera.setParameters(parameters);

        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showShortToast(appCompatActivity,"初始化相机失败！");
        }
    }

    /**
     * 判断是否支持某种对焦模式
     * @param focusMode
     * @return
     */
    private boolean isSupportFocus(String focusMode){
        boolean isSupport = false;
        List<String> listFocus = parameters.getSupportedFocusModes();
        for(String s : listFocus){
            if(s.equals(focusMode)){
                isSupport = true;
            }
        }
        return isSupport;
    }

    /**
     * 将相机中用于表示人脸矩阵的坐标转换为UI页面的矩阵坐标
     * @param faces 人脸数组
     * @return
     */
    private ArrayList<RectF> transFrom (Camera.Face[] faces){
        //Matrix由3*3矩阵中9个值来决定
        /*
        {MSCALE_X,MSKEW_X,MTRANS_X,
        MSKEW_Y,MSCALE_Y,MTRANS_Y,
        MPERSP_0,MPERSP_1,MPERSP_2}
         */
        Matrix matrix = new Matrix();
        //镜像
        boolean mirror;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
            mirror = true;
        }else {
            mirror = false;
        }

        if(mirror){
            //set重置 post pre后乘
            matrix.setScale(-1f,1f);
        }else {
            matrix.setScale(1f,1f);
        }

        //后乘旋转
        matrix.postRotate(Float.valueOf(orientation));
        //后乘缩放
        matrix.postScale(surfaceView.getWidth()/2000f,surfaceView.getHeight()/2000f);
        //位移
        matrix.postTranslate(surfaceView.getWidth()/2f,surfaceView.getHeight()/2f);
        ArrayList<RectF> arrayList = new ArrayList<>();
        for(Camera.Face face : faces){
            RectF srcR = new RectF(face.rect);
            RectF dstR = new RectF(0f,0f,0f,0f);
            //通过Matrix映射
            matrix.mapRect(dstR,srcR);
            arrayList.add(dstR);
        }
        return arrayList;
    }

    //因为在市面上安卓机型五花八门，屏幕分辨率也很多，为了避免图像变形，需要调整预览图像和保存的图像尺寸
    /**
     * 设置保存图片的尺寸
     */
    private void setPictureSize(){
        //Camera.Size  图片的大小
        //获取camera支持的图片大小
        List<Camera.Size> localSizes = parameters.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;//优先选择预览界面的尺寸
        //获取预览界面尺寸
        Camera.Size previewSize = parameters.getPreviewSize();

        //界面尺寸宽高比
        float previewSizeScale = 0;
        if(previewSize != null){
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if(localSizes != null){
            int cameraSizeLength = localSizes.size();
            //选出与预览界面等比的最高分辨率
            for(int i=0;i<cameraSizeLength;i++){
                Camera.Size size = localSizes.get(i);
                if(biggestSize == null){
                    biggestSize = size;
                }else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                //选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }
            if(fitSize == null){
                fitSize = biggestSize;
            }
            Log.d("最佳存储尺寸:",fitSize.width + "*" + fitSize.height);
            parameters.setPictureSize(fitSize.width,fitSize.height);
        }
    }

    /**
     * 预览大小
     */
    private void setPreViewSize(){
        //获取系统支持预览大小
        List<Camera.Size> localSizes = parameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;//最大分辨率
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int i = 0; i < cameraSizeLength; i++) {
                Camera.Size size = localSizes.get(i);
                Log.d("系统支持预览的尺寸:",size.width + "*" +size.height);
                if (biggestSize == null ||
                        (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                    biggestSize = size;
                }

                //如果支持的比例都等于所获取到的宽高
                //在相机中，都是width是长边，也就是width > height
                // 获取所支持的size.width要和screenHeight比较，size.height要和screenWidth
                if (size.width == screenHeight
                        && size.height == screenWidth) {
                    fitSize = size;
                    //如果任一宽或者高等于所支持的尺寸
                } else if (size.width == screenHeight
                        || size.height == screenWidth) {
                    if (targetSize == null) {
                        targetSize = size;
                        //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                    } else if (size.width < screenHeight
                            || size.height < screenWidth) {
                        targetSiz2 = size;
                    }
                }
            }

            if (fitSize == null) {
                fitSize = targetSize;
            }

            if (fitSize == null) {
                fitSize = targetSiz2;
            }

            if (fitSize == null) {
                fitSize = biggestSize;
            }
            Log.d("最佳预览尺寸:",fitSize.width + "*" + fitSize.height);
            parameters.setPreviewSize(fitSize.width, fitSize.height);
        }


    }
    /**
     * 调整预览方向
     * 官方推荐方法
     * @param appCompatActivity
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(AppCompatActivity appCompatActivity,int cameraId,Camera camera){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,cameraInfo);

        //rotation 预览window的旋转方向
        //对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，rotation=0，这时没有旋转
        //当screenOrientation="landScape"时，rotation=1。
        int rotation = appCompatActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        //计算图像要旋转的角度
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) %360;
        }else {
            result = (cameraInfo.orientation - degrees +360) % 360;
        }
        orientation = result;
        camera.setDisplayOrientation(orientation);
    }


    /**
     * 拍照
     */
    public void takePicture() {
        if (camera != null) {
            //直接调用Camera.takePicture(ShutterCallback shutter,PictureCallback raw,PictureCallback jpeg)
            //拍照回调 点击拍照时回调 写一个空实现
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new Camera.PictureCallback() {
                //回调没压缩的原始数据
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                }
            }, new Camera.PictureCallback() {
                //回调图片数据 点击拍照后相机返回的照片byte数组，照片数据
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //拍照后调用预览方法，否则会停在拍照图像的界面
                    camera.startPreview();
                    //回调
                    cameraCallBack.onTakePicture(data, camera);
                    //保存图片
                    getPhotoPath(data);

                }
            });

        }
    }

    /**
     * 创建拍照照片的文件夹
     */
    private void setUpFile(){
        photosFile = new File(Environment.getExternalStorageDirectory().toString()+"/pic");

        //还未创建过该文件目录
        if (!photosFile.exists() || !photosFile.isDirectory()) {
            boolean isSuccess = false;
            try {
                //创建目录
                isSuccess = photosFile.mkdirs();
                Log.d("TAG", "setUpFile: "+isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showShortToast(appCompatActivity, "创建存放目录失败,请检查磁盘空间~");
                appCompatActivity.finish();
            } finally {
                if (!isSuccess) {
                    ToastUtil.showShortToast(appCompatActivity, "创建存放目录失败,请检查磁盘空间~");
                    appCompatActivity.finish();
                }
            }
        }
    }

    /**
     * 开启线程 保存图片
     * @param data
     */
    private void getPhotoPath(final byte[] data){
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis();
                String time = SystemUtil.formatTime(timeMillis);
                //拍照数量+1
                photoNum++;
                //图片名字
                String name = SystemUtil.formatTime(timeMillis, SystemUtil.formatTime(photoNum) + ".jpg");
                //创建具体文件
                File file = new File(photosFile, name);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        //将数据写入文件
                        fos.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //将图片保存到手机相册中
                    SystemUtil.saveAlbum(Configuration.insidePath + file.getName(), file.getName(),appCompatActivity);
                    //将图片复制到外部
                    //SystemUtil.copyPicture(Configuration.insidePath + file.getName(),Configuration.outPath,file.getName());
                    //发消息给主线程
                    Message message = new Message();
                    message.what = 1;
                    //文件路径
                    message.obj = Configuration.insidePath + file.getName();
                    mHandler.sendMessage(message);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera(){
        if(camera != null){
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    public void setCameraCallBack(CameraCallBack mCameraCallBack) {
        this.cameraCallBack = mCameraCallBack;

    }
    public void setFrontOrBack(int cameraId){
        this.cameraId = cameraId;
    }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
