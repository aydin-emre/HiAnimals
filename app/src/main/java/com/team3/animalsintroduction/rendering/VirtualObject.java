package com.team3.animalsintroduction.rendering;

import android.opengl.Matrix;

import com.huawei.hiar.ARAnchor;

import java.util.Arrays;

/**
 * This class provides attributes of the virtual object and necessary methods related to virtual object rendering.
 */
public class VirtualObject {
    private static final float ROTATION_ANGLE = 315.0f;

    private static final int MATRIX_SIZE = 16;

    private static final int COLOR_SIZE = 4;

    private static  float SCALE_FACTOR = 0.50f;

    private ARAnchor mArAnchor;

    private float[] mObjectColors = new float[COLOR_SIZE];

    private float[] mModelMatrix = new float[MATRIX_SIZE];

    private boolean mIsSelectedFlag = false;

    /**
     * The constructor initializes the pose of the virtual object in a space and the
     * color of the virtual object with the input anchor point and color parameters.
     *
     * @param arAnchor Data provided by AR Engine, describing the pose.
     * @param //color4f Color data in an array with a length of 4.
     */
    public VirtualObject(ARAnchor arAnchor, float[] color4f) {
        mObjectColors = Arrays.copyOf(color4f, color4f.length);
        mArAnchor = arAnchor;
        init();
    }

    @Override
    protected void finalize() throws Throwable {
        // If the anchor object is destroyed, call the detach() method to instruct
        // the AR Engine to stop tracking the anchor.
        if (mArAnchor != null) {
            mArAnchor.detach();
        }
        super.finalize();
    }

    private void init() {
        // Set a scaling matrix, in which the elements of the principal diagonal is the scaling coefficient.
        Matrix.setIdentityM(mModelMatrix, 0);
        mModelMatrix[0] = SCALE_FACTOR;
        mModelMatrix[5] = SCALE_FACTOR;
        mModelMatrix[10] = SCALE_FACTOR;

        // Rotate the camera along the Y axis by a certain angle.
        Matrix.rotateM(mModelMatrix, 0, ROTATION_ANGLE, 0f, 1f, 0f);
    }

    /**
     * Update the anchor information in the virtual object corresponding to the class.
     *
     * @param arAnchor Data provided by AR Engine, describing the pose.
     */
    public void setAnchor(ARAnchor arAnchor) {
        if (mArAnchor != null) {
            mArAnchor.detach();
        }
        mArAnchor = arAnchor;
    }

    /**
     * Obtain the anchor information of a virtual object corresponding to the class.
     *
     * @return ARAnchor(provided by AREngine)
     */
    public ARAnchor getAnchor() {
        return mArAnchor;
    }

    /**
     *  Obtain the anchor information of a virtual object corresponding to the class.
     *
     * @return Color of the virtual object, returned in an array with a length of 4.
     */
    public float[] getColor() {
        float[] rets = new float[4];
        if (mIsSelectedFlag) {
            rets[0] = 255.0f - mObjectColors[0];
            rets[1] = 255.0f - mObjectColors[1];
            rets[2] = 255.0f - mObjectColors[2];
            rets[3] = mObjectColors[3];
            return rets;
        } else {
            rets = Arrays.copyOf(mObjectColors, mObjectColors.length);
            return rets;
        }
    }

    /**
     * Set the color of the current virtual object.
     *
     * @param color Color data in an array with a length of 4.
     */
    public void setColor(float[] color) {
        if (color != null && color.length == COLOR_SIZE) {
            mObjectColors = Arrays.copyOf(color, color.length);
        }
    }
    /**
     * Obtain the anchor matrix data of the current virtual object.
     *
     * @return Anchor matrix data of the current virtual object.
     */
    public float[] getModelAnchorMatrix() {
        float[] modelMatrix = new float[MATRIX_SIZE];
        if (mArAnchor != null) {
            mArAnchor.getPose().toMatrix(modelMatrix, 0);
        } else {
            Matrix.setIdentityM(modelMatrix, 0);
        }
        float[] rets = new float[MATRIX_SIZE];
        Matrix.multiplyMM(rets, 0, modelMatrix, 0, mModelMatrix, 0);
        return rets;
    }

    /**
     * Set the selection status of the current object by passing true or false,
     * where true indicates that the object is selected, and false indicates not.
     *
     * @param isSelected Whether the selection is successful.
     */
    public void setIsSelected(boolean isSelected) {
        mIsSelectedFlag = isSelected;
    }
}