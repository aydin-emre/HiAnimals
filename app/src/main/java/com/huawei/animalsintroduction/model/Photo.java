/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.huawei.animalsintroduction.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKey;

/**
 * Definition of ObjectType Photo.
 *
 * @since 2020-07-29
 */
public class Photo extends CloudDBZoneObject {
    private String token;

    private byte[] photo;

    @PrimaryKey
    private String  id;

    public Photo() {
        super();

    }
    public Photo(String token, byte[] photo, String id){
        this.token = token;
        this.photo = photo;
        this.id = id;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
