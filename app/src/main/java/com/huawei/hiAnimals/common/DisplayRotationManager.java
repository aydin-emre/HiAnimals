package com.huawei.hiAnimals.common;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.huawei.hiar.ARSession;

public class DisplayRotationManager implements DisplayListener {
    private static final String TAG = DisplayRotationManager.class.getSimpleName();

    private boolean mIsDeviceRotation;

    private final Context mContext;

    private final Display mDisplay;

    private int mViewPx;

    private int mViewPy;

    public DisplayRotationManager(@NonNull Context context) {
        mContext = context;
        WindowManager systemService = mContext.getSystemService(WindowManager.class);
        if (systemService != null) {
            mDisplay = systemService.getDefaultDisplay();
        } else {
            mDisplay = null;
        }
    }

    public void registerDisplayListener() {
        DisplayManager systemService = mContext.getSystemService(DisplayManager.class);
        if (systemService != null) {
            systemService.registerDisplayListener(this, null);
        }
    }

    public void unregisterDisplayListener() {
        DisplayManager systemService = mContext.getSystemService(DisplayManager.class);
        if (systemService != null) {
            systemService.unregisterDisplayListener(this);
        }
    }

    /**
     * When a device is rotated, the viewfinder size and whether the device is rotated
     * should be updated to correctly display the geometric information returned by the
     * AR Engine. This method should be called when onSurfaceChanged.
     *
     * @param width Width of the surface updated by the device.
     * @param height Height of the surface updated by the device.
     */
    public void updateViewportRotation(int width, int height) {
        mViewPx = width;
        mViewPy = height;
        mIsDeviceRotation = true;
    }

    /**
     * Check whether the current device is rotated.
     *
     * @return The device rotation result.
     */
    public boolean getDeviceRotation() {
        return mIsDeviceRotation;
    }

    /**
     * If the device is rotated, update the device window of the current ARSession.
     * This method can be called when onDrawFrame is called.
     *
     * @param session {@link ARSession} object.
     */
    public void updateArSessionDisplayGeometry(ARSession session) {
        int displayRotation = 0;
        if (mDisplay != null) {
            displayRotation = mDisplay.getRotation();
        } else {
            Log.e(TAG, "updateArSessionDisplayGeometry mDisplay null!");
        }
        session.setDisplayGeometry(displayRotation, mViewPx, mViewPy);
        mIsDeviceRotation = false;
    }

    @Override
    public void onDisplayAdded(int displayId) {
    }

    @Override
    public void onDisplayRemoved(int displayId) {
    }

    @Override
    public void onDisplayChanged(int displayId) {
        mIsDeviceRotation = true;
    }
}
