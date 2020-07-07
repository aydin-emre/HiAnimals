package com.huawei.animalsintroduction;

public class Constant {
    public static final int IS_LOG = 1;
    //login
    public static final int REQUEST_SIGN_IN_LOGIN = 1002;
    //login by code
    public static final int REQUEST_SIGN_IN_LOGIN_CODE = 1003;

    /**
     * your app’s client ID,please replace it of yours
     */
    public static final String CLIENT_ID = "101090009";

    /**
     * JWK JSON Web Key endpoint, developer can get the JWK of the last two days from this endpoint
     * See more about JWK in http://self-issued.info/docs/draft-ietf-jose-json-web-key.html
     */
    public static final String CERT_URL = "https://oauth-login.cloud.huawei.com/oauth2/v3/certs";
    /**
     *  Id Token issue
     */
    public static final String ID_TOKEN_ISSUE = "https://accounts.huawei.com";
}