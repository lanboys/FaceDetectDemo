package com.facepp.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.facepp.demo.load.BitmapUtil;
import com.facepp.demo.load.LogUtil;
import com.facepp.demo.util.ConUtil;
import com.google.gson.Gson;
import com.megvii.facepp.sdk.Facepp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class CollectActivity extends Activity implements View.OnClickListener {

    protected final LogUtil log = LogUtil.getLogUtil(getClass(), LogUtil.LOG_VERBOSE);
    private static final int IMAGE_SIZE = 50;
    private static final int THREAD_SIZE = 20;
    private Facepp facepp;
    private Button btnStart;
    private ImageView ivImage;
    private ExecutorService mExecutor;
    private OkHttpClient mOkHttpClient;
    private TextView tvDetectNum;
    private TextView tvDetectSuccessNum;
    private TextView tvDetectTime;
    private TextView tvDetectAllTime;
    private long detectNum;
    private long detectSuccessNum;
    private TextView tvDetectSuccessRate;
    private long mDetectTime;
    private long mDetectStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        btnStart = findViewById(R.id.btn_start);
        ivImage = findViewById(R.id.iv_image);

        tvDetectNum = findViewById(R.id.tv_detect_num);
        tvDetectSuccessNum = findViewById(R.id.tv_detect_success_num);
        tvDetectSuccessRate = findViewById(R.id.tv_detect_success_rate);

        tvDetectTime = findViewById(R.id.tv_detect_time);
        tvDetectAllTime = findViewById(R.id.tv_detect_all_time);

        btnStart.setOnClickListener(this);

        facepp = new Facepp();
        String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_5_2_model), 1);
        log.i("onCreate() errorCode: " + errorCode);

        mExecutor = Executors.newFixedThreadPool(THREAD_SIZE);

        mOkHttpClient = new OkHttpClient.Builder()
                .build();
    }

    public void updateUI() {
        log.i("updateUI(): 采集数量: " + detectNum);
        log.i("updateUI(): 成功数量: " + detectSuccessNum);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDetectTime.setText("单次采集时间: " + mDetectTime + "ms");
                tvDetectAllTime.setText("总采集时间: " + (System.currentTimeMillis() - mDetectStartTime) + "ms");

                tvDetectNum.setText("采集数量: " + detectNum);
                tvDetectSuccessNum.setText("成功数量: " + detectSuccessNum);
                double rate = 0;
                if (detectSuccessNum != 0) {
                    rate = detectNum / detectSuccessNum;
                }
                tvDetectSuccessRate.setText("成功率: " + rate);

                btnStart.setText(isStart ? "停止" : "启动");
            }
        });
    }

    public synchronized void updateDetectNum() {
        detectNum += 1;
    }

    public synchronized void updateDetectSuccessNum() {
        detectSuccessNum += 1;
    }

    public Facepp.Face[] detect(byte[] imageData, int width, int height) {
        return facepp.detect(imageData, width, height, Facepp.IMAGEMODE_NV21);
    }

    private boolean isStart = false;
    private boolean isRunning = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                isStart = !isStart;
                if (isStart && !isRunning) {
                    try {
                        mDetectStartTime = System.currentTimeMillis();
                        detectNum = 0;
                        detectSuccessNum = 0;
                        getImageUrl();
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.e("onClick():  " + e.getLocalizedMessage());
                        isStart = !isStart;
                    }
                }
                updateUI();
                break;
        }
    }

    public void getImageUrl() throws IOException {

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                try {
                    isRunning = true;
                    //log.i("run() thread: " + Thread.currentThread().getName());

                    Request request = new Request.Builder()
                            .url("http://47.106.96.179:8085/kde/admin/feedback/edit?num=" + IMAGE_SIZE)
                            .get()
                            .build();

                    ResponseBody body = mOkHttpClient.newCall(request).execute().body();
                    String string = body.string();
                    List<ImageInfoBean> mImageInfoBeans = new Gson().fromJson(string, ApiResult.class).getData();
                    CountDownLatch latch = new CountDownLatch(mImageInfoBeans.size());

                    for (ImageInfoBean imageInfoBean : mImageInfoBeans) {
                        mExecutor.submit(new ImageProcessor(latch, imageInfoBean));
                    }

                    latch.await();

                    Request request1 = new Request.Builder()
                            .url("http://47.106.96.179:8085/kde/admin/feedback/delete")
                            .get()
                            .build();

                    mOkHttpClient.newCall(request1).execute().body();
                    if (isStart) {
                        getImageUrl();
                    } else {
                        isRunning = false;
                    }
                } catch (Exception e) {
                    log.e("run(): error " + e.getLocalizedMessage());
                    isRunning = false;
                }

                log.e("run() 一个循环耗时： " + (System.currentTimeMillis() - startTime));
            }
        });
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public class ImageProcessor implements Runnable {

        private CountDownLatch latch;
        private ImageInfoBean imageInfoBean;

        public ImageProcessor(CountDownLatch latch, ImageInfoBean imageInfoBean) {
            this.latch = latch;
            this.imageInfoBean = imageInfoBean;
        }

        public void run() {
            //log.i("run() thread: " + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();
            final File file;
            try {
                file = Glide.with(getApplication())
                        .load(imageInfoBean.getUrl() + "?uuid=" + UUID.randomUUID())
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();

                BitmapUtil.BitmapResult bitmapResult = null;

                if (file != null) {

                    //此path就是对应文件的缓存路径
                    String path = file.getAbsolutePath();
                    log.i("run() path: " + path);

                    bitmapResult = BitmapUtil.decodeSampledBitmapFromFile(path, 0, 0, 0);

                    Facepp.Face[] detect = detect(bitmap2Bytes(bitmapResult.bitmap), bitmapResult.realWidth, bitmapResult.realHeight);
                    log.i("run() face size:" + detect.length);
                    for (Facepp.Face face : detect) {
                        PointF[] points = face.points;
                        log.i("run() point size:" + points.length);
                        for (PointF point : points) {
                            log.i("run() point x: " + point.x + "point y: " + point.y);
                        }
                    }

                    imageInfoBean.setDetect(true);
                    updateDetectNum();
                    if (detect.length > 0) {
                        imageInfoBean.setDetectSuccess(true);
                        updateDetectSuccessNum();
                    }
                }
            } catch (Exception e) {
                log.e("run():  " + e.getLocalizedMessage());
            }

            mDetectTime = System.currentTimeMillis() - startTime;
            log.e("run() 一次采集耗时： " + mDetectTime);

            updateUI();
            latch.countDown();
        }
    }

    //ImageLoader.getInstance()
    //        .loadImageCallBackFile(ivImage, imageInfoBean.getUrl(), new GetImageCacheCallBack() {
    //            @Override
    //            public void onGetImageCacheCallBack(boolean isSuccess, String imageUrl, File file, BitmapUtil.BitmapResult bitmapResult) {
    //                log.i("onGetImageCacheCallBack() thread: " + Thread.currentThread().getName());
    //
    //                Facepp.Face[] detect = detect(bitmap2Bytes(bitmapResult.bitmap), bitmapResult.realWidth, bitmapResult.realHeight);
    //                log.i("onGetImageCacheCallBack() face size:" + detect.length);
    //                for (Facepp.Face face : detect) {
    //                    PointF[] points = face.points;
    //                    log.i("onGetImageCacheCallBack() point size:" + points.length);
    //                    for (PointF point : points) {
    //                        log.i("onGetImageCacheCallBack() point x: " + point.x + "point y: " + point.y);
    //                    }
    //                }
    //
    //                imageInfoBean.setDetect(true);
    //                if (detect.length >= 0) {
    //                    imageInfoBean.setDetectSuccess(true);
    //                }
    //                latch.countDown();
    //            }
    //        });
}
