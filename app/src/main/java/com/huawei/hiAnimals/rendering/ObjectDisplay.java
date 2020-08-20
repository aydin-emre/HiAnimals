package com.huawei.hiAnimals.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.huawei.hiAnimals.Constant;
import com.huawei.hiAnimals.R;
import com.huawei.hiAnimals.common.MatrixUtil;
import com.huawei.hiAnimals.common.ShaderUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

/**
 * Draw a virtual object based on the specified parameters.
 *
 * @author HW
 * @since 2020-04-11
 */
public class  ObjectDisplay {
    private static final String TAG = ObjectDisplay.class.getSimpleName();

    // Set the default light direction.
    private static final float[] LIGHT_DIRECTIONS = new float[]{0.0f, 1.0f, 0.0f, 0.0f};

    private static final int MATRIX_SIZE = 16;

    // Light direction (x, y, z, w).
    private float[] mViewLightDirections = new float[4];

    private int mTexCoordsBaseAddress;

    private int mNormalsBaseAddress;

    private int mVertexBufferId;

    private int mIndexCount;

    private int mGlProgram;

    private int mIndexBufferId;

    private int[] mTextures = new int[1];

    private int mModelViewUniform;

    private int mModelViewProjectionUniform;

    private int mPositionAttribute;

    private int mNormalAttribute;

    private int mTexCoordAttribute;

    private int mTextureUniform;

    private int mLightingParametersUniform;

    private int mColorUniform;

    private float[] mModelMatrixs = new float[MATRIX_SIZE];

    private float[] mModelViewMatrixs = new float[MATRIX_SIZE];

    private float[] mModelViewProjectionMatrixs = new float[MATRIX_SIZE];

    // The largest bounding box of a virtual object, represented by two diagonals of a cube.
    private float[] mBoundingBoxs = new float[6];

    private float mWidth;

    private float mHeight;

    private float[] mModelMatrix = new float[Constant.MAX_TRACKING_ANCHOR_NUM];

    private int mMaterialParametersUniform;

    /**
     * If the surface size is changed, update the changed size of the record synchronously.
     * This method is called when {@link WorldRenderManager#onSurfaceChanged}.
     *
     * @param width Surface's width.
     * @param height Surface's height.
     */
    void setSize(float width, float height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Create a shader program to read the data of the virtual object.
     * This method is called when {@link WorldRenderManager#onSurfaceCreated}
     *
     * @param context Context.
     */
    void init(Context context, int type) {
        ShaderUtil.checkGlError(TAG, "Init start.");
        // Coordinate and index.
        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        mVertexBufferId = buffers[0];
        mIndexBufferId = buffers[1];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);

        try {
            switch (type){
                case 1:
                    createOnGlThread(context, "deer.obj", "Diffuse.png");
                    break;
                case 2:
                    createOnGlThread(context, "dog.obj", "dog_diffuse.png");
                    break;
                case 3:
                    createOnGlThread(context, "tiger.obj", "tiger_diffuse.png");
                    break;
                case 4:
                    createOnGlThread(context, "duck.obj", "duck_diffuse.jpg");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ShaderUtil.checkGlError(TAG, "Init end.");
    }

    /**
     * Draw a virtual object at a specific location on a specified plane.
     * This method is called when {@link WorldRenderManager#onDrawFrame}.
     *
     * @param cameraView The viewMatrix is a 4 * 4 matrix.
     * @param cameraProjection The ProjectionMatrix is a 4 * 4 matrix.
     * @param lightIntensity The lighting intensity.
     * @param obj The virtual object.
     */

    private float mAmbient = 0.5f;
    private float mDiffuse = 1.0f;
    private float mSpecular = 1.0f;
    private float mSpecularPower = 4.0f;

    public void onDrawFrame(float[] cameraView, float[] cameraProjection, float lightIntensity, VirtualObject obj) {
        ShaderUtil.checkGlError(TAG, "onDrawFrame start.");
        mModelMatrixs = obj.getModelAnchorMatrix();
        Matrix.multiplyMM(mModelViewMatrixs, 0, cameraView, 0, mModelMatrixs, 0);
        Matrix.multiplyMM(mModelViewProjectionMatrixs, 0, cameraProjection, 0, mModelViewMatrixs, 0);
        GLES20.glUseProgram(mGlProgram);
        Matrix.multiplyMV(mViewLightDirections, 0, mModelViewMatrixs, 0, LIGHT_DIRECTIONS, 0);
        MatrixUtil.normalizeVec3(mViewLightDirections);

        // Light direction.
        GLES20.glUniform4f(mLightingParametersUniform,
            mViewLightDirections[0], mViewLightDirections[1], mViewLightDirections[2], lightIntensity);

        // Set the object color property.
        GLES20.glUniform4f(mMaterialParametersUniform, mAmbient, mDiffuse, mSpecular, mSpecularPower);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLES20.glUniform1i(mTextureUniform, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferId);

        // The coordinate dimension of the read virtual object is 3.
        GLES20.glVertexAttribPointer(
            mPositionAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

        // The dimension of the normal vector is 3.
        GLES20.glVertexAttribPointer(
            mNormalAttribute, 3, GLES20.GL_FLOAT, false, 0, mNormalsBaseAddress);

        // The dimension of the texture coordinate is 2.
        GLES20.glVertexAttribPointer(
            mTexCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, mTexCoordsBaseAddress);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(
            mModelViewUniform, 1, false, mModelViewMatrixs, 0);
        GLES20.glUniformMatrix4fv(
            mModelViewProjectionUniform, 1, false, mModelViewProjectionMatrixs, 0);
        GLES20.glEnableVertexAttribArray(mPositionAttribute);
        GLES20.glEnableVertexAttribArray(mNormalAttribute);
        GLES20.glEnableVertexAttribArray(mTexCoordAttribute);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionAttribute);
        GLES20.glDisableVertexAttribArray(mNormalAttribute);
        GLES20.glDisableVertexAttribArray(mTexCoordAttribute);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        ShaderUtil.checkGlError(TAG, "onDrawFrame end.");

    }

    /**
     * Check whether the virtual object is clicked.
     *
     * @param cameraView The viewMatrix 4 * 4.
     * @param cameraPerspective The ProjectionMatrix 4 * 4.
     * @param obj The virtual object data.
     * @param event The gesture event.
     * @return Return the click result for determining whether the input virtual object is clicked
     */
    boolean hitTest(float[] cameraView, float[] cameraPerspective, VirtualObject obj, MotionEvent event) {
        mModelMatrixs = obj.getModelAnchorMatrix();
        Matrix.multiplyMM(mModelViewMatrixs, 0, cameraView, 0, mModelMatrixs, 0);
        Matrix.multiplyMM(mModelViewProjectionMatrixs, 0, cameraPerspective, 0, mModelViewMatrixs, 0);

        // Calculate the coordinates of the smallest bounding box in the coordinate system of the device screen.
        float[] screenPos = calculateScreenPos(mBoundingBoxs[0], mBoundingBoxs[1], mBoundingBoxs[2]);

        // Record the largest bounding rectangle of an object (minX/minY/maxX/maxY).
        float[] boundarys = new float[4];
        boundarys[0] = screenPos[0];
        boundarys[1] = screenPos[0];
        boundarys[2] = screenPos[1];
        boundarys[3] = screenPos[1];

        // Determine whether a screen position corresponding to (maxX, maxY, maxZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{3, 4, 5});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (minX, minY, maxZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{0, 1, 5});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (minX, maxY, minZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{0, 4, 2});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (minX, maxY, maxZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{0, 4, 5});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (maxX, minY, minZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{3, 1, 2});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (maxX, minY, maxZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{3, 1, 5});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }

        // Determine whether a screen position corresponding to (maxX, maxY, maxZ) is clicked.
        boundarys = findMaximum(boundarys, new int[]{3, 4, 2});
        if (((event.getX() > boundarys[0]) && (event.getX() < boundarys[1]))
            && ((event.getY() > boundarys[2]) && (event.getY() < boundarys[3]))) {
            return true;
        }
        return false;
    }

    // The size of minXmaxXminYmaxY is 4, and the size of index is 3.
    private float[] findMaximum(float[] minXmaxXminYmaxY, int[] index) {
        float[] screenPos = calculateScreenPos(mBoundingBoxs[index[0]],
                mBoundingBoxs[index[1]], mBoundingBoxs[index[2]]);
        if (screenPos[0] < minXmaxXminYmaxY[0]) {
            minXmaxXminYmaxY[0] = screenPos[0];
        }
        if (screenPos[0] > minXmaxXminYmaxY[1]) {
            minXmaxXminYmaxY[1] = screenPos[0];
        }
        if (screenPos[1] < minXmaxXminYmaxY[2]) {
            minXmaxXminYmaxY[2] = screenPos[1];
        }
        if (screenPos[1] > minXmaxXminYmaxY[3]) {
            minXmaxXminYmaxY[3] = screenPos[1];
        }
        return minXmaxXminYmaxY;
    }

    // Convert the input coordinates to the plane coordinate system.
    private float[] calculateScreenPos(float coordinateX, float coordinateY, float coordinateZ) {
        // The coordinates of the point are four-dimensional (x, y, z, w).
        float[] vecs = new float[4];
        vecs[0] = coordinateX;
        vecs[1] = coordinateY;
        vecs[2] = coordinateZ;
        vecs[3] = 1.0f;

        // Store the coordinate values in the clip coordinate system.
        float[] rets = new float[4];
        Matrix.multiplyMV(rets, 0, mModelViewProjectionMatrixs, 0, vecs, 0);

        // Divide by the w component of the coordinates.
        rets[0] /= rets[3];
        rets[1] /= rets[3];
        rets[2] /= rets[3];

        // In the current coordinate system, left is negative, right is positive, downward
        // is positive, and upward is negative.Adding 1 to the left of the X coordinate is
        // equivalent to moving the coordinate system leftwards. Such an operation on the Y
        // axis is equivalent to moving the coordinate system upwards.
        rets[0] += 1.0f;
        rets[1] = 1.0f - rets[1];

        // Convert to pixel coordinates.
        rets[0] *= mWidth;
        rets[1] *= mHeight;

        // When the w component is set to 1, the xy component caused by coordinate system
        // movement is eliminated and doubled.
        rets[3] = 1.0f;
        rets[0] /= 2.0f;
        rets[1] /= 2.0f;
        return rets;
    }

    // Bounding box [minX, minY, minZ, maxX, maxY, maxZ].
    private void calculateBoundingBox(FloatBuffer vertices) {
        if (vertices.limit() < 3) {
            mBoundingBoxs[0] = 0.0f;
            mBoundingBoxs[1] = 0.0f;
            mBoundingBoxs[2] = 0.0f;
            mBoundingBoxs[3] = 0.0f;
            mBoundingBoxs[4] = 0.0f;
            mBoundingBoxs[5] = 0.0f;
            return;
        } else {
            mBoundingBoxs[0] = vertices.get(0);
            mBoundingBoxs[1] = vertices.get(1);
            mBoundingBoxs[2] = vertices.get(2);
            mBoundingBoxs[3] = vertices.get(0);
            mBoundingBoxs[4] = vertices.get(1);
            mBoundingBoxs[5] = vertices.get(2);
        }

        // Use the first three pairs as the initial variables and get the three
        // maximum values and three minimum values.
        int index = 3;
        while (index < vertices.limit() - 2) {
            if (vertices.get(index) < mBoundingBoxs[0]) {
                mBoundingBoxs[0] = vertices.get(index);
            }
            if (vertices.get(index) > mBoundingBoxs[3]) {
                mBoundingBoxs[3] = vertices.get(index);
            }
            index++;

            if (vertices.get(index) < mBoundingBoxs[1]) {
                mBoundingBoxs[1] = vertices.get(index);
            }
            if (vertices.get(index) > mBoundingBoxs[4]) {
                mBoundingBoxs[4] = vertices.get(index);
            }
            index++;

            if (vertices.get(index) < mBoundingBoxs[2]) {
                mBoundingBoxs[2] = vertices.get(index);
            }
            if (vertices.get(index) > mBoundingBoxs[5]) {
                mBoundingBoxs[5] = vertices.get(index);
            }
            index++;
        }
    }

    public void createOnGlThread(Context context, String objAssetName,
                                 String diffuseTextureAssetName) throws IOException {
        Bitmap textureBitmap = BitmapFactory.decodeStream(
                context.getAssets().open(diffuseTextureAssetName));

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        textureBitmap.recycle();

        ShaderUtil.checkGlError(TAG, "load texture");

        InputStream objInputStream = context.getAssets().open(objAssetName);
        Obj obj = ObjReader.read(objInputStream);

        obj = ObjUtils.convertToRenderable(obj);

        IntBuffer wideIndices = ObjData.getFaceVertexIndices(obj, 3);
        FloatBuffer vertices = ObjData.getVertices(obj);
        FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
        FloatBuffer normals = ObjData.getNormals(obj);

        calculateBoundingBox(vertices);


        ShortBuffer indices = ByteBuffer.allocateDirect(2 * wideIndices.limit())
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        while (wideIndices.hasRemaining()) {
            indices.put((short) wideIndices.get());
        }
        indices.rewind();

        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        mVertexBufferId = buffers[0];
        mIndexBufferId = buffers[1];

        int mVerticesBaseAddress = 0;
        mTexCoordsBaseAddress = mVerticesBaseAddress + 4 * vertices.limit();
        mNormalsBaseAddress = mTexCoordsBaseAddress + 4 * texCoords.limit();
        final int totalBytes = mNormalsBaseAddress + 4 * normals.limit();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, totalBytes, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, mVerticesBaseAddress, 4 * vertices.limit(), vertices);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, mTexCoordsBaseAddress, 4 * texCoords.limit(), texCoords);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, mNormalsBaseAddress, 4 * normals.limit(), normals);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferId);
        mIndexCount = indices.limit();
        GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * mIndexCount, indices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        ShaderUtil.checkGlError(TAG, "obj buffer load");

        final int vertexShader = ShaderUtil.loadGLShader(TAG, context,
                GLES20.GL_VERTEX_SHADER, R.raw.virtualobject_vertex);
        final int fragmentShader = ShaderUtil.loadGLShader(TAG, context,
                GLES20.GL_FRAGMENT_SHADER, R.raw.virtualobject_fragment);

        mGlProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGlProgram, vertexShader);
        GLES20.glAttachShader(mGlProgram, fragmentShader);
        GLES20.glLinkProgram(mGlProgram);
        GLES20.glUseProgram(mGlProgram);

        ShaderUtil.checkGlError(TAG, "program creation");

        mModelViewUniform = GLES20.glGetUniformLocation(mGlProgram, "u_ModelView");
        mModelViewProjectionUniform =
                GLES20.glGetUniformLocation(mGlProgram, "u_ModelViewProjection");

        mPositionAttribute = GLES20.glGetAttribLocation(mGlProgram, "a_Position");
        mNormalAttribute = GLES20.glGetAttribLocation(mGlProgram, "a_Normal");
        mTexCoordAttribute = GLES20.glGetAttribLocation(mGlProgram, "a_TexCoord");

        mTextureUniform = GLES20.glGetUniformLocation(mGlProgram, "u_Texture");

        mLightingParametersUniform = GLES20.glGetUniformLocation(mGlProgram, "u_LightingParameters");
        mMaterialParametersUniform = GLES20.glGetUniformLocation(mGlProgram, "u_MaterialParameters");
        mColorUniform = GLES20.glGetUniformLocation(mGlProgram, "u_ObjColor");

        ShaderUtil.checkGlError(TAG, "Program parameters");

        Matrix.setIdentityM(mModelMatrix, 0);
    }

}