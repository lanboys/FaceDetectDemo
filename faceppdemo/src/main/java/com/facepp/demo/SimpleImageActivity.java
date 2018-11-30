package com.facepp.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.megvii.facepp.sdk.Facepp;


/**
 * Created by xiejiantao on 2018/4/11.
 */

public class SimpleImageActivity extends Activity implements View.OnClickListener {

    private Facepp facepp;
    private ImageView ivDetectFace;
    private Button btnChoose;
    private Button btnSave;

    private HandlerThread mHandlerThread = new HandlerThread("imagedetect");
    private Handler mHandler;

    Bitmap mOriginBitmap;
    Bitmap mDetectBitmap;

    View vEmpty;

    ProgressDialog progressDialog;


    int rotation = 0;


    public static final int GALLERY_CODE = 101;
    public static final int REQ_GALLERY_CODE = 101;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_layout);
        ivDetectFace = findViewById(R.id.iv_detect_face);
        btnChoose = findViewById(R.id.bt_choose_img);
        btnSave = findViewById(R.id.bt_save_img);
        btnChoose.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        vEmpty = findViewById(R.id.ll_empty);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        initSdk();


    }


    private void initSdk() {
        facepp = new Facepp();
        String errorCode = facepp.init(this, ConUtilNew.getFileContent(this, R.raw.megviifacepp_0_5_2_model));

        //sdk内部其他api已经处理好，可以不判断
        if (errorCode != null) {
            Intent intent = new Intent();
            intent.putExtra("errorcode", errorCode);
            setResult(101, intent);
            finish();
            return;
        }

        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_NORMAL;
      //  faceppConfig.isSmooth = false;
        faceppConfig.rotation = rotation;
      //  faceppConfig.face_confidence_filter=0.7f;

        facepp.setFaceppConfig(faceppConfig);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_choose_img:
                requestGalleryPerm();

                break;
            case R.id.bt_save_img:
                saveBitmap();
                break;
        }
    }


    private void requestGalleryPerm() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //进行权限请求
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_CODE);
        } else {
            openGalleryActivity();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == GALLERY_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted
                showSettingDialog("读取存储卡");
            } else {
                openGalleryActivity();
            }
        }
    }

    private void openGalleryActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_GALLERY_CODE);


    }


    public void showSettingDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("无" + msg + "权限，去设置里打开");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConUtilNew.getAppDetailSettingIntent(SimpleImageActivity.this);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_GALLERY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    processGalleryResult(uri);

                }
                break;

        }
    }


    private void processGalleryResult(final Uri uri) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, "人脸检测", "检测中。。。。。", false, true);
            progressDialog.setCancelable(false);
        } else {
            progressDialog.show();
        }
        vEmpty.setVisibility(View.GONE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String path = ConUtilNew.getRealPathFromURI(SimpleImageActivity.this, uri);
                mOriginBitmap = ConUtilNew.getBitmapWithPath(path);    //get bitmap
                byte[] data = ConUtilNew.getPixelsRGBA(mOriginBitmap);




                for (int i = 0; i < 1; i++) {
                    rotation = (rotation + 90 * i) % 360;
                    Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
                    faceppConfig.rotation = rotation;
                    facepp.setFaceppConfig(faceppConfig);

                    final Facepp.Face[] faces = facepp.detect(data, mOriginBitmap.getWidth(), mOriginBitmap.getHeight(), Facepp.IMAGEMODE_RGBA);
                    if (faces.length != 0) {
                        mDetectBitmap = Bitmap.createBitmap(mOriginBitmap.getWidth(),
                                mOriginBitmap.getHeight(), Bitmap.Config.RGB_565);

                        Canvas canvas = new Canvas(mDetectBitmap);// 使用空白图片生成canvas
                        // 将bmp1绘制在画布上
                        Rect srcRect = new Rect(0, 0, mOriginBitmap.getWidth(), mOriginBitmap.getHeight());// 截取bmp1中的矩形区域
                        Rect dstRect = new Rect(0, 0, mOriginBitmap.getWidth(), mOriginBitmap.getHeight());// bmp1在目标画布中的位置
                        canvas.drawBitmap(mOriginBitmap, srcRect, dstRect, null);
                        Paint paint = new Paint();
                        paint.setColor(0xffff0000);

                        paint.setStrokeWidth(5);

                        for (int j = 0; j < faces.length; j++) {
                            Facepp.Face face = faces[j];
                            facepp.getLandmark(face, Facepp.FPP_GET_LANDMARK81);
                            float[] points = ConUtilNew.getPoints(face.points);
                            canvas.drawPoints(points, paint);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivDetectFace.setImageBitmap(mDetectBitmap);
                                progressDialog.dismiss();
                            }
                        });

                        return;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        mDetectBitmap = null;
                        ivDetectFace.setImageBitmap(mOriginBitmap);
                        Toast.makeText(SimpleImageActivity.this, "未检测到人脸", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

    }

    private void saveBitmap() {
        if (mDetectBitmap == null) {
            return;
        }

        String path = ConUtilNew.saveBitmap(this, mDetectBitmap);
        Toast.makeText(SimpleImageActivity.this, "保存到" + path, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        mHandlerThread.quit();
        facepp.release();
        super.onDestroy();
    }
}
