package com.huawei.animalsintroduction.common;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtil {
    private ShaderUtil() {
    }

    /**
     * Check OpenGL ES running exceptions and throw them when necessary.
     *
     * @param tag Exception information.
     * @param label Program label.
     */
    public static void checkGlError(@NonNull String tag, @NonNull String label) {
        int lastError = GLES20.GL_NO_ERROR;
        int error = GLES20.glGetError();
        while (error != GLES20.GL_NO_ERROR) {
            Log.e(tag, label + ": glError " + error);
            lastError = error;
            error = GLES20.glGetError();
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new ArDemoRuntimeException(label + ": glError " + lastError);
        }
    }
    public static int loadGLShader(String tag, Context context, int type, int resId) {
        String code = readRawTextFile(context, resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        final int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] == 0) {
            Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    private static String readRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}