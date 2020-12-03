/*
*       Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.team3.animalsintroduction.common;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] PERMISSIONS_ARRAYS = new String[]{
        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // List of permissions to be applied for.
    private static List<String> permissionsList = new ArrayList<>();

    private PermissionManager() {
    }

    public static void onResume(final Activity activity) {
        boolean isHasPermission = true;
        for (String permission : PERMISSIONS_ARRAYS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                isHasPermission = false;
                break;
            }
        }
        if (!isHasPermission) {
            for (String permission : PERMISSIONS_ARRAYS) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
            ActivityCompat.requestPermissions(activity,
                permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public static boolean hasPermission(@NonNull final Activity activity) {
        for (String permission : PERMISSIONS_ARRAYS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}