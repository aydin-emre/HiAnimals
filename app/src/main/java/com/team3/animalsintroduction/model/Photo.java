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

package com.team3.animalsintroduction.model;

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
