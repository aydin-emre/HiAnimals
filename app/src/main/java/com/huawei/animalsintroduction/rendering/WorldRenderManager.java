/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.animalsintroduction.rendering;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.animalsintroduction.GestureEvent;
import com.huawei.animalsintroduction.R;
import com.huawei.animalsintroduction.VirtualObject;
import com.huawei.animalsintroduction.common.ArDemoRuntimeException;
import com.huawei.animalsintroduction.common.DisplayRotationManager;
import com.huawei.animalsintroduction.common.TextureDisplay;
import com.huawei.hiar.ARCamera;
import com.huawei.hiar.ARFrame;
import com.huawei.hiar.ARHitResult;
import com.huawei.hiar.ARLightEstimate;
import com.huawei.hiar.ARPlane;
import com.huawei.hiar.ARPoint;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARSession;
import com.huawei.hiar.ARTrackable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class provides rendering management related to the world scene, including
 * label rendering and virtual object rendering management.
 *
 * @author HW
 * @since 2020-03-21
 */
public class WorldRenderManager implements GLSurfaceView.Renderer {
    private static final String TAG = WorldRenderManager.class.getSimpleName();

    private static final int PROJ_MATRIX_OFFSET = 0;

    private static final float PROJ_MATRIX_NEAR = 0.1f;

    private static final float PROJ_MATRIX_FAR = 100.0f;

    private static final float MATRIX_SCALE_SX = -1.0f;

    private static final float MATRIX_SCALE_SY = -1.0f;

    private static final float[] BLUE_COLORS = new float[] {66.0f, 133.0f, 244.0f, 255.0f};

    private static final float[] GREEN_COLORS = new float[] {66.0f, 133.0f, 244.0f, 255.0f};

    private ARSession mSession;

    private Activity mActivity;

    private Context mContext;

    private int mType;

    //private TextView mTextView;

    private TextView mSearchingTextView;

    private int frames = 0;

    private long lastInterval;

    private float fps;

    private TextureDisplay mTextureDisplay = new TextureDisplay();

   // private TextDisplay mTextDisplay = new TextDisplay();

    ///private LabelDisplay mLabelDisplay = new LabelDisplay();

    private ObjectDisplay mObjectDisplay = new ObjectDisplay();

    private DisplayRotationManager mDisplayRotationManager;

    private ArrayBlockingQueue<GestureEvent> mQueuedSingleTaps;

    private ArrayList<VirtualObject> mVirtualObjects = new ArrayList<>();

    private VirtualObject mSelectedObj = null;

    /**
     * The constructor passes context and activity. This method will be called when {@link Activity}.
     *
     * @param activity Activity
     * @param context Context
     */
    public WorldRenderManager(Activity activity, Context context, int type) {
        mActivity = activity;
        mContext = context;
        mType = type;
     //   mTextView = activity.findViewById(R.id.wordTextView);
        mSearchingTextView = activity.findViewById(R.id.searchingTextView);
    }

    /**
     * Set ARSession, which will update and obtain the latest data in OnDrawFrame.
     *
     * @param arSession ARSession.
     */
    public void setArSession(ARSession arSession) {
        if (arSession == null) {
            Log.e(TAG, "setSession error, arSession is null!");
            return;
        }
        mSession = arSession;
    }

    /**
     * Set a gesture type queue.
     *
     * @param queuedSingleTaps Gesture type queue.
     */
    public void setQueuedSingleTaps(ArrayBlockingQueue<GestureEvent> queuedSingleTaps) {
        if (queuedSingleTaps == null) {
            Log.e(TAG, "setSession error, arSession is null!");
            return;
        }
        mQueuedSingleTaps = queuedSingleTaps;
    }

    /**
     * Set the DisplayRotationManage object, which will be used in onSurfaceChanged and onDrawFrame.
     *
     * @param displayRotationManager DisplayRotationManage is a customized object.
     */
    public void setDisplayRotationManage(DisplayRotationManager displayRotationManager) {
        if (displayRotationManager == null) {
            Log.e(TAG, "SetDisplayRotationManage error, displayRotationManage is null!");
            return;
        }
        mDisplayRotationManager = displayRotationManager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the window color.
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        mTextureDisplay.init();
     /*   mTextDisplay.setListener(new TextDisplay.OnTextInfoChangeListener() {
            @Override
            public void textInfoChanged(String text, float positionX, float positionY) {
                showWorldTypeTextView(text, positionX, positionY);
            }
        });*/

       // mLabelDisplay.init(getPlaneBitmaps());

        mObjectDisplay.init(mContext, mType);
    }

    /**
     * Create a thread for text display in the UI thread. This thread will be called back in TextureDisplay.
     *
     * @param //text Gesture information displayed on the screen
     * @param //positionX The left padding in pixels.
     * @param //positionY The right padding in pixels.
     */
  /*  private void showWorldTypeTextView(final String text, final float positionX, final float positionY) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setTextColor(Color.WHITE);

                // Set the font size to be displayed on the screen.
                mTextView.setTextSize(10f);
                if (text != null) {
                    mTextView.setText(text);
                    mTextView.setPadding((int) positionX, (int) positionY, 0, 0);
                } else {
                    mTextView.setText("");
                }
            }
        });
    }*/

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        mTextureDisplay.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
        mDisplayRotationManager.updateViewportRotation(width, height);
        mObjectDisplay.setSize(width, height);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }
        if (mDisplayRotationManager.getDeviceRotation()) {
            mDisplayRotationManager.updateArSessionDisplayGeometry(mSession);
        }

        try {
            mSession.setCameraTextureName(mTextureDisplay.getExternalTextureId());
            ARFrame arFrame = mSession.update();
            ARCamera arCamera = arFrame.getCamera();

            // The size of the projection matrix is 4 * 4.
            float[] projectionMatrix = new float[16];

            arCamera.getProjectionMatrix(projectionMatrix, PROJ_MATRIX_OFFSET, PROJ_MATRIX_NEAR, PROJ_MATRIX_FAR);
            mTextureDisplay.onDrawFrame(arFrame);
            StringBuilder sb = new StringBuilder();
            updateMessageData(sb);
          //  mTextDisplay.onDrawFrame(sb);

            // The size of ViewMatrix is 4 * 4.
            float[] viewMatrix = new float[16];
            arCamera.getViewMatrix(viewMatrix, 0);
            for (ARPlane plane : mSession.getAllTrackables(ARPlane.class)) {
                if (plane.getType() != ARPlane.PlaneType.UNKNOWN_FACING
                    && plane.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                    hideLoadingMessage();
                    break;
                }
            }
           // mLabelDisplay.onDrawFrame(mSession.getAllTrackables(ARPlane.class), arCamera.getDisplayOrientedPose(),projectionMatrix);
            handleGestureEvent(arFrame, arCamera, projectionMatrix, viewMatrix);
            ARLightEstimate lightEstimate = arFrame.getLightEstimate();
            float lightPixelIntensity = 1;
            if (lightEstimate.getState() != ARLightEstimate.State.NOT_VALID) {
                lightPixelIntensity = lightEstimate.getPixelIntensity();
            }
            drawAllObjects(projectionMatrix, viewMatrix, lightPixelIntensity);

        } catch (ArDemoRuntimeException e) {
            Log.e(TAG, "Exception on the ArDemoRuntimeException!");
        } catch (Throwable t) {
            // This prevents the app from crashing due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread: ", t);
        }

        if (capturePicture) {
            capturePicture = false;
            try {
                SavePicture();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void drawAllObjects(float[] projectionMatrix, float[] viewMatrix, float lightPixelIntensity) {
        Iterator<VirtualObject> ite = mVirtualObjects.iterator();
        while (ite.hasNext()) {
            VirtualObject obj = ite.next();
            if (obj.getAnchor().getTrackingState() == ARTrackable.TrackingState.STOPPED) {
                ite.remove();
            }
            if (obj.getAnchor().getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                mObjectDisplay.onDrawFrame(viewMatrix, projectionMatrix, lightPixelIntensity, obj);
            }
        }
    }

   /* private ArrayList<Bitmap> getPlaneBitmaps() {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(getPlaneBitmap(R.id.plane_other));
        bitmaps.add(getPlaneBitmap(R.id.plane_wall));
        bitmaps.add(getPlaneBitmap(R.id.plane_floor));
        bitmaps.add(getPlaneBitmap(R.id.plane_seat));
        bitmaps.add(getPlaneBitmap(R.id.plane_table));
        bitmaps.add(getPlaneBitmap(R.id.plane_ceiling));
        return bitmaps;
    }*/

   /* private Bitmap getPlaneBitmap(int id) {
        TextView view = mActivity.findViewById(id);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = view.getDrawingCache();
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.setScale(MATRIX_SCALE_SX, MATRIX_SCALE_SY);
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }*/

    /**
     * Update the information to be displayed on the screen.
     *
     * @param sb String buffer.
     */
    private void updateMessageData(StringBuilder sb) {
        float fpsResult = doFpsCalculate();
        sb.append("FPS=").append(fpsResult).append(System.lineSeparator());
    }

    private float doFpsCalculate() {
        ++frames;
        long timeNow = System.currentTimeMillis();

        // Convert millisecond to second.
        if (((timeNow - lastInterval) / 1000.0f) > 0.5f) {
            fps = frames / ((timeNow - lastInterval) / 1000.0f);
            frames = 0;
            lastInterval = timeNow;
        }
        return fps;
    }

    private void hideLoadingMessage() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSearchingTextView != null) {
                    mSearchingTextView.setVisibility(View.GONE);
                    mSearchingTextView = null;
                }
            }
        });
    }

    private void handleGestureEvent(ARFrame arFrame, ARCamera arCamera, float[] projectionMatrix, float[] viewMatrix) {
        GestureEvent event = mQueuedSingleTaps.poll();
        if (event == null) {
            return;
        }

        // Do not perform anything when the object is not tracked.
        if (arCamera.getTrackingState() != ARTrackable.TrackingState.TRACKING) {
            return;
        }

        int eventType = event.getType();
        switch (eventType) {
            case GestureEvent.GESTURE_EVENT_TYPE_DOWN: {
                doWhenEventTypeDown(viewMatrix, projectionMatrix, event);
                break;
            }
            case GestureEvent.GESTURE_EVENT_TYPE_SCROLL: {
                if (mSelectedObj == null) {
                    break;
                }
                ARHitResult hitResult = hitTest4Result(arFrame, arCamera, event.getEventSecond());
                if (hitResult != null) {
                    mSelectedObj.setAnchor(hitResult.createAnchor());
                }
                break;
            }
            case GestureEvent.GESTURE_EVENT_TYPE_SINGLETAPUP: {
                // Do not perform anything when an object is selected.
                if (mSelectedObj != null) {
                    return;
                }

                MotionEvent tap = event.getEventFirst();
                ARHitResult hitResult = null;

                hitResult = hitTest4Result(arFrame, arCamera, tap);

                if (hitResult == null) {
                    break;
                }
                doWhenEventTypeSingleTap(hitResult);
                break;
            }
            case GestureEvent.GESTURE_EVENT_TYPE_PINCH: {
                // Do not perform anything when an object is selected.
                if (mSelectedObj == null) {
                    return;
                }

                ARHitResult hitResult = null;
                MotionEvent tap = event.getEventFirst();
                hitResult = hitTest4Result(arFrame, arCamera, tap);
                doWhenEventTypePinch(hitResult);
                break;
            }
            default: {
                Log.e(TAG, "Unknown motion event type, and do nothing.");
            }
        }
    }

    private void doWhenEventTypeDown(float[] viewMatrix, float[] projectionMatrix, GestureEvent event) {
        if (mSelectedObj != null) {
            mSelectedObj.setIsSelected(false);
            mSelectedObj = null;
        }
        for (VirtualObject obj : mVirtualObjects) {
            if (mObjectDisplay.hitTest(viewMatrix, projectionMatrix, obj, event.getEventFirst())) {
                obj.setIsSelected(true);
                mSelectedObj = obj;
                break;
            }
        }
    }

    private void doWhenEventTypeSingleTap(ARHitResult hitResult) {
        // The hit results are sorted by distance. Only the nearest hit point is valid.
        // Set the number of stored objects to 10 to avoid the overload of rendering and AR Engine.
        if (mVirtualObjects.size() >= 16) {
            mVirtualObjects.get(0).getAnchor().detach();
            mVirtualObjects.remove(0);
        }

        ARTrackable currentTrackable = hitResult.getTrackable();

      /*  if (currentTrackable instanceof ARPoint) {
            mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), BLUE_COLORS));
        } else if (currentTrackable instanceof ARPlane) {
            mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), GREEN_COLORS));
        } else {
            Log.i(TAG, "Hit result is not plane or point.");
        }*/

         float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};
        mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), DEFAULT_COLOR));
    }

    private void doWhenEventTypePinch(ARHitResult hitResult) {
        // The hit results are sorted by distance. Only the nearest hit point is valid.
        // Set the number of stored objects to 10 to avoid the overload of rendering and AR Engine.
        if (mVirtualObjects.size() >= 16) {
            mVirtualObjects.get(0).getAnchor().detach();
            mVirtualObjects.remove(0);
        }



        mSelectedObj.setScaleFactor(3.0f);
        mVirtualObjects.remove(0);
        mVirtualObjects.add(new VirtualObject(hitResult.createAnchor(), BLUE_COLORS));


    }

    private ARHitResult hitTest4Result(ARFrame frame, ARCamera camera, MotionEvent event) {
        ARHitResult hitResult = null;
        List<ARHitResult> hitTestResults = frame.hitTest(event);

        for (int i = 0; i < hitTestResults.size(); i++) {
            // Determine whether the hit point is within the plane polygon.
            ARHitResult hitResultTemp = hitTestResults.get(i);
            if (hitResultTemp == null) {
                continue;
            }
            ARTrackable trackable = hitResultTemp.getTrackable();

            boolean isPlanHitJudge =
                trackable instanceof ARPlane && ((ARPlane) trackable).isPoseInPolygon(hitResultTemp.getHitPose())
                    && (calculateDistanceToPlane(hitResultTemp.getHitPose(), camera.getPose()) > 0);

            // Determine whether the point cloud is clicked and whether the point faces the camera.
            boolean isPointHitJudge = trackable instanceof ARPoint
                && ((ARPoint) trackable).getOrientationMode() == ARPoint.OrientationMode.ESTIMATED_SURFACE_NORMAL;

            // Select points on the plane preferentially.
            if (isPlanHitJudge || isPointHitJudge) {
                hitResult = hitResultTemp;
                if (trackable instanceof ARPlane) {
                    break;
                }
            }
        }
        return hitResult;
    }

    /**
     * Calculate the distance between a point in a space and a plane. This method is used
     * to calculate the distance between a camera in a space and a specified plane.
     *
     * @param planePose ARPose of a plane.
     * @param cameraPose ARPose of a camera.
     * @return Calculation results.
     */
    private static float calculateDistanceToPlane(ARPose planePose, ARPose cameraPose) {
        // The dimension of the direction vector is 3.
        float[] normals = new float[3];

        // Obtain the unit coordinate vector of a normal vector of a plane.
        planePose.getTransformedAxis(1, 1.0f, normals, 0);

        // Calculate the distance based on projection.
        return (cameraPose.tx() - planePose.tx()) * normals[0] // 0:x
            + (cameraPose.ty() - planePose.ty()) * normals[1] // 1:y
            + (cameraPose.tz() - planePose.tz()) * normals[2]; // 2:z
    }

    public void takePhoto(Context mContext){

        // Here just a set a flag so we can copy
        // the image from the onDrawFrame() method.
        // This is required for OpenGL so we are on the rendering thread.
        Toast.makeText(mContext, "Photo is captured", Toast.LENGTH_LONG).show();
        this.capturePicture = true;
    }

    private int mWidth;
    private int mHeight;
    private  boolean capturePicture = false;

    /**
     * Call from the GLThread to save a picture of the current frame.
     */
    public void SavePicture() throws IOException {
        int pixelData[] = new int[mWidth * mHeight];

        // Read the pixels from the current GL frame.
        IntBuffer buf = IntBuffer.wrap(pixelData);
        buf.position(0);
        GLES20.glReadPixels(0, 0, mWidth, mHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);

        // Create a file in the Pictures/HelloAR album.
        final File out = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/AnimalsIntro", "Img" +
                Long.toHexString(System.currentTimeMillis()) + ".png");

        // Make sure the directory exists
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }

        // Convert the pixel data from RGBA to what Android wants, ARGB.
        int bitmapData[] = new int[pixelData.length];
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int p = pixelData[i * mWidth + j];
                int b = (p & 0x00ff0000) >> 16;
                int r = (p & 0x000000ff) << 16;
                int ga = p & 0xff00ff00;
                bitmapData[(mHeight - i - 1) * mWidth + j] = ga | r | b;
            }
        }
        // Create a bitmap.
        Bitmap bmp = Bitmap.createBitmap(bitmapData,
                mWidth, mHeight, Bitmap.Config.ARGB_8888);

        // Write it to disk.
        FileOutputStream fos = new FileOutputStream(out);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

}