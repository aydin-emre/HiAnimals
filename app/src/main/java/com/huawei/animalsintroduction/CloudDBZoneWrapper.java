package com.huawei.animalsintroduction;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.CloudDBZoneTask;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSuccessListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.animalsintroduction.model.ObjectTypeInfoHelper;
import com.huawei.animalsintroduction.model.Photo;
import com.huawei.animalsintroduction.model.User;

import java.util.ArrayList;
import java.util.List;

public class CloudDBZoneWrapper {

    public AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private ListenerHandler mRegister;
    private CloudDBZoneConfig mConfig;
    private UiCallBack mUiCallBack;


    public CloudDBZoneWrapper() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            Log.w(Constant.DB_ZONE_WRAPPER, "createObjectTypeSuccess " );
        } catch (AGConnectCloudDBException e) {
            Log.w(Constant.DB_ZONE_WRAPPER, "createObjectTypeError: " + e.getMessage());
        }
    }
    public void openCloudDBZone() {
        mConfig = new CloudDBZoneConfig("animalsIntro",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Log.w(Constant.DB_ZONE_WRAPPER, "openCloudDBZoneSuccess " );
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig, true);
        } catch (AGConnectCloudDBException e) {
            Log.w(Constant.DB_ZONE_WRAPPER, "openCloudDBZoneError: " + e.getMessage());
        }
    }
    public void closeCloudDBZone() {
        try {
            mCloudDB.closeCloudDBZone(mCloudDBZone);
            Log.w(Constant.DB_ZONE_WRAPPER, "closeCloudDBZoneSuccess " );
        } catch (AGConnectCloudDBException e) {
            Log.w(Constant.DB_ZONE_WRAPPER, "closeCloudDBZoneError: " + e.getMessage());
        }
    }


    public interface UiCallBack { // Override in activity
        void onAddOrQuery(List<User> userList); //To get data
        void isLastID(int lastID);// To get id information
        void isDataUpsert(Boolean state); // To add data
        void updateUiOnError(String errorMessage);
        void onAddOrQueryPhoto(List<Photo> photoList); //To get data

    }
    public void addCallBacks(UiCallBack uiCallBack) {
        mUiCallBack = uiCallBack;
    }
    boolean state = false;

    public void insertUser(User user) {
        state = false;
        if (mCloudDBZone == null) {
            Log.w(Constant.DB_ZONE_WRAPPER, "INSERT USER : CloudDBZone is null, try re-open it");
            return;
        }
        CloudDBZoneTask<Integer> upsertTask = mCloudDBZone.executeUpsert(user);
        if (mUiCallBack == null) {
            return;
        }
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            state = true;
            Log.w(Constant.DB_ZONE_WRAPPER, "INSERT USER : upsert " + cloudDBZoneResult + " records");
        }).addOnFailureListener(e -> {
            state = false;
            mUiCallBack.updateUiOnError("INSERT USER : Insert user info failed");
        });
        if (mUiCallBack != null) {
            mUiCallBack.isDataUpsert(state);
        }
    }

    public void insertPhoto(Photo photo) {
        state = false;
        if (mCloudDBZone == null) {
            Log.w(Constant.DB_ZONE_WRAPPER, "INSERT Photo : CloudDBZone is null, try re-open it");
            return;
        }
        CloudDBZoneTask<Integer> upsertTask = mCloudDBZone.executeUpsert(photo);
        if (mUiCallBack == null) {
            return;
        }
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            state = true;
            Log.w(Constant.DB_ZONE_WRAPPER, "INSERT USER : upsert " + cloudDBZoneResult + " records");
        }).addOnFailureListener(e -> {
            state = false;
            mUiCallBack.updateUiOnError("INSERT photo : Insert photo info failed");
        });
        if (mUiCallBack != null) {
            mUiCallBack.isDataUpsert(state);
        }
    }

    public void getAllUsers() {
        if (mCloudDBZone == null) {
            Log.w(Constant.DB_ZONE_WRAPPER, "GET USER DETAIL : CloudDBZone is null, try re-open it");
            return;
        }
        CloudDBZoneTask<CloudDBZoneSnapshot<User>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(User.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            userListResult (snapshot);
            Log.w(Constant.DB_ZONE_WRAPPER, "GET USER DETAIL : GoResults: ");
        }).addOnFailureListener(e -> {
            if (mUiCallBack != null) {
                mUiCallBack.updateUiOnError("GET USER DETAIL : Query user list from cloud failed");
            }
        });
    }

    private void userListResult (CloudDBZoneSnapshot<User> snapshot) {
        CloudDBZoneObjectList<User> userInfoCursor = snapshot.getSnapshotObjects();
        List<User> userInfoList = new ArrayList<>();
        try {
            while (userInfoCursor.hasNext()) {
                User userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
                Log.w(Constant.DB_ZONE_WRAPPER, "USER DETAIL RESULT : processQueryResult: ");

            }
        } catch (AGConnectCloudDBException e) {
            Log.w(Constant.DB_ZONE_WRAPPER, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        snapshot.release();
        if (mUiCallBack != null) {
            mUiCallBack.onAddOrQuery(userInfoList);
        }
    }
    //To get all Photos
    public void getAllPhotos(Context mContext) {
        SharedPreferences pref = mContext.getSharedPreferences("MyPref", mContext.MODE_PRIVATE);
        String token = pref.getString("token",null);

        if (mCloudDBZone == null) {
            Log.w(Constant.DB_ZONE_WRAPPER, "GET USER DETAIL : CloudDBZone is null, try re-open it");
            return;
        }
        if (token ==null){
            Toast.makeText(mContext, "You have no photos or you are not logged in.", Toast.LENGTH_SHORT ).show();
            mUiCallBack.onAddOrQueryPhoto(null);
            return;
        }
        CloudDBZoneTask<CloudDBZoneSnapshot<Photo>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Photo.class).orderByDesc("id").equalTo("token",token ).limit(1),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            photoListResult (snapshot);
            Log.w(Constant.DB_ZONE_WRAPPER, "GET PHOTO LIST : GoResults: ");
        }).addOnFailureListener(e -> {
            if (mUiCallBack != null) {
                mUiCallBack.updateUiOnError("GET PHOTO LIST : Query photo list from cloud failed");
            }
        });
    }

    private void photoListResult (CloudDBZoneSnapshot<Photo> snapshot) {
        CloudDBZoneObjectList<Photo> photoCursor = snapshot.getSnapshotObjects();
        List<Photo> photoList = new ArrayList<>();
        try {
            while (photoCursor.hasNext()) {
                Photo photo = photoCursor.next();
                photoList.add(photo);
                Log.w(Constant.DB_ZONE_WRAPPER, "USER DETAIL RESULT : processQueryResult: ");

            }
        } catch (AGConnectCloudDBException e) {
            Log.w(Constant.DB_ZONE_WRAPPER, "USER DETAIL RESULT : processQueryResult: " + e.getMessage());
        }
        snapshot.release();
        if (mUiCallBack != null) {
            mUiCallBack.onAddOrQueryPhoto(photoList);
        }
    }

}

