package com.huawei.animalsintroduction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.huawei.animalsintroduction.common.DisplayRotationManager;
import com.huawei.animalsintroduction.common.PermissionManager;
import com.huawei.animalsintroduction.model.Photo;
import com.huawei.animalsintroduction.model.User;
import com.huawei.animalsintroduction.rendering.WorldRenderManager;
import com.huawei.hiar.ARConfigBase;
import com.huawei.hiar.AREnginesApk;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARWorldTrackingConfig;
import com.huawei.hiar.exceptions.ARCameraNotAvailableException;
import com.huawei.hiar.exceptions.ARUnSupportedConfigurationException;
import com.huawei.hiar.exceptions.ARUnavailableClientSdkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceApkTooOldException;
import com.huawei.hiar.exceptions.ARUnavailableServiceNotInstalledException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class AnimalActivity extends Activity implements CloudDBZoneWrapper.UiCallBack {
    private static final String TAG = AnimalActivity.class.getSimpleName();

    private static final int MOTIONEVENT_QUEUE_CAPACITY = 2;

    private static final int OPENGLES_VERSION = 2;

    private ARSession mArSession;

    private GLSurfaceView mSurfaceView;

    private WorldRenderManager mWorldRenderManager;

    private GestureDetector mGestureDetector;

    private DisplayRotationManager mDisplayRotationManager;

    private ArrayBlockingQueue<GestureEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(MOTIONEVENT_QUEUE_CAPACITY);

    private String message = null;

    private boolean isRemindInstall = false;

    private ImageView ivTakePhoto;

    ImageView ivListPhoto;

    RelativeLayout loadinPanel;

    private CloudDBZoneWrapper mCloudDBZoneWrapper;

    private MyHandler mHandler = new MyHandler();

    public static Photo photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_animal);

        mSurfaceView = findViewById(R.id.surfaceview);
        ivTakePhoto = findViewById(R.id.ivTakePhoto);
        loadinPanel = findViewById(R.id.loadingPanel);

        mDisplayRotationManager = new DisplayRotationManager(this);
        initGestureDetector();

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(OPENGLES_VERSION);

        // Set the EGL configuration chooser, including for the number of
        // bits of the color buffer and the number of depth bits.
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        int type = getIntent().getIntExtra("Type", -1);

        mWorldRenderManager = new WorldRenderManager(this, this, type);
        mWorldRenderManager.setDisplayRotationManager(mDisplayRotationManager);
        mWorldRenderManager.setQueuedSingleTaps(mQueuedSingleTaps);

        mSurfaceView.setRenderer(mWorldRenderManager);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        ivTakePhoto.setOnClickListener(view -> takePhoto());

        ivListPhoto = findViewById(R.id.ivListPhoto);

        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler.post(() -> {
            mCloudDBZoneWrapper.addCallBacks(AnimalActivity.this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZone();
        });
        ivListPhoto.setOnClickListener(view -> {
            //get images from Huawei Cloud Db

           getImages();
        });


    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onGestureEvent(GestureEvent.createSingleTapUpEvent(e));
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {

                onGestureEvent(GestureEvent.createDownEvent(e));
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                // handle scrolling
                onGestureEvent(GestureEvent.createScrollEvent(e1, e2, distanceX, distanceY));
                return true;
            }
        });

        mSurfaceView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
    }

    private void onGestureEvent(GestureEvent e) {
        boolean offerResult = mQueuedSingleTaps.offer(e);
        if (offerResult) {
            Log.d(TAG, "Successfully joined the queue.");
        } else {
            Log.d(TAG, "Failed to join queue.");
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // AR Engine requires the camera permission.
        PermissionManager.onResume(this);

        Exception exception = null;
        message = null;
        if (mArSession == null) {
            try {
                if (!arEngineAbilityCheck()) {
                    finish();
                    return;
                }
                mArSession = new ARSession(this);
                ARWorldTrackingConfig config = new ARWorldTrackingConfig(mArSession);
                config.setFocusMode(ARConfigBase.FocusMode.AUTO_FOCUS);
                config.setSemanticMode(ARWorldTrackingConfig.SEMANTIC_PLANE);
                mArSession.configure(config);
                mWorldRenderManager.setArSession(mArSession);
            } catch (Exception capturedException) {
                exception = capturedException;
                setMessageWhenError(capturedException);
            }
            if (message != null) {
                stopArSession(exception);
                return;
            }
        }
        try {
            mArSession.resume();
        } catch (ARCameraNotAvailableException e) {
            Toast.makeText(this, "Camera open failed, please restart the app", Toast.LENGTH_LONG).show();
            mArSession = null;
            return;
        }
        mDisplayRotationManager.registerDisplayListener();
        mSurfaceView.onResume();
    }

    /**
     * Check whether HUAWEI AR Engine server (com.huawei.arengine.service) is installed on
     * the current device. If not, redirect the user to HUAWEI AppGallery for installation.
     */
    private boolean arEngineAbilityCheck() {
        boolean isInstallArEngineApk = AREnginesApk.isAREngineApkReady(this);
        if (!isInstallArEngineApk && isRemindInstall) {
            Toast.makeText(this, "Please agree to install.", Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d(TAG, "Is Install AR Engine Apk: " + isInstallArEngineApk);
        if (!isInstallArEngineApk) {
            startActivity(new Intent(this, com.huawei.animalsintroduction.common.ConnectAppMarketActivity.class));
            isRemindInstall = true;
        }
        return AREnginesApk.isAREngineApkReady(this);
    }

    private void setMessageWhenError(Exception catchException) {
        if (catchException instanceof ARUnavailableServiceNotInstalledException) {
            startActivity(new Intent(this, com.huawei.animalsintroduction.common.ConnectAppMarketActivity.class));
        } else if (catchException instanceof ARUnavailableServiceApkTooOldException) {
            message = "Please update HuaweiARService.apk";
        } else if (catchException instanceof ARUnavailableClientSdkTooOldException) {
            message = "Please update this app";
        } else if (catchException instanceof ARUnSupportedConfigurationException) {
            message = "The configuration is not supported by the device!";
        } else {
            message = "exception throw";
        }
    }

    private void stopArSession(Exception exception) {
        Log.i(TAG, "stopArSession start.");
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Creating session error", exception);
        if (mArSession != null) {
            mArSession.stop();
            mArSession = null;
        }
        Log.i(TAG, "stopArSession end.");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause start.");
        super.onPause();
        if (mArSession != null) {
            mDisplayRotationManager.unregisterDisplayListener();
            mSurfaceView.onPause();
            mArSession.pause();
        }
        Log.i(TAG, "onPause end.");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy start.");
        if (mArSession != null) {
            mArSession.stop();
            mArSession = null;
        }
        mCloudDBZoneWrapper.closeCloudDBZone();
        super.onDestroy();
        Log.i(TAG, "onDestroy end.");
    }

    @Override
    public void onWindowFocusChanged(boolean isHasFocus) {
        Log.d(TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(isHasFocus);
        if (isHasFocus) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onAddOrQuery(List<User> userList) {

    }

    @Override
    public void isLastID(int lastID) {

    }

    @Override
    public void isDataUpsert(Boolean state) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }

    @Override
    public void onAddOrQueryPhoto(List<Photo> photoList) {
        if (photoList == null){
            loadinPanel.setVisibility(View.GONE);
            ivListPhoto.setClickable(true);
            ivTakePhoto.setClickable(true);
        }
        else {
            photo = photoList.get(0);
            Intent intent = new Intent(AnimalActivity.this, PhotoActivity.class);

            //creates the temporary file and gets the path
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo.getPhoto(), 0, photo.getPhoto().length);

            String filePath= tempFileImage(this,bitmap,"name");

            //passes the file path string with the intent
            intent.putExtra("path", filePath);

            loadinPanel.setVisibility(View.GONE);
            ivListPhoto.setClickable(true);
            ivTakePhoto.setClickable(true);

            startActivity(intent);
        }
    }

    //creates a temporary file and return the absolute file path
    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

    private static final class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
        }
    }

    public void takePhoto(){
        mWorldRenderManager.takePhoto(getApplicationContext(), mCloudDBZoneWrapper);

    }

    public void getImages(){
        ivListPhoto.setClickable(false);
        ivTakePhoto.setClickable(false);
        loadinPanel.setVisibility(View.VISIBLE);
        mCloudDBZoneWrapper.getAllPhotos(this);
    }
}

