package com.example.oauth;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;

public class OAuthFacebook implements OAuthUser.Convertable {
    
    public interface OAuthFacebookCallBack {
        public void saveToken(String key, String token);
        public void onComplete(OAuthUser user);
    }
    
    public final static String PREF_KEY = "ACCESS_TOKEN";
    
    private final String FB_JSON_ID_KEY = "id";
    private final String FB_JSON_NAME_KEY = "name";
    
//    private final String App_Serect = "c4f6f7f05e1c1f6071787e498d41e64c";
//    private final String Redirect_Uri = "http://www.facebook.com/connect/login_success.html";
//    private final String FB_URL = "https://graph.facebook.com/oauth/authorize?client_id="
//            + APP_ID + "&redirect_uri=" + Redirect_Uri
//            + "&type=user_agent&scope=email,read_stream,offline_access";
    
    private Activity mContext = null;
    private Facebook mFacebook = null;
    private String mFacebookAccessToken = null;
    private OAuthFacebookCallBack mCallback = null;
    
    
    public OAuthFacebook(String appId, Activity context, OAuthFacebookCallBack callback) {
        mFacebook = new Facebook(appId);
        mContext = context;
        mCallback = callback;
    }
    
    public void setAccessToken(String token) {
        if(!Strings.isNullOrEmpty(token)) {
            mFacebookAccessToken = token;
            if(mFacebook != null) {
                mFacebook.setAccessToken(mFacebookAccessToken);
            }
        }
    }
    
    public void login() {
        Log.i("[facebook]", "authorise call");
        if(mFacebook != null) {
            mFacebook.authorize((Activity) mContext,
                    new String[] { "publish_stream, user_photos, email" },
                    (DialogListener) new FacebookAuthorizeListener());
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mFacebook != null) {
            mFacebook.authorizeCallback(requestCode, resultCode, data);
        }
    }
    
    /**
     *  Facebook 인증후 처리를 위한 callback class
     */
    public class FacebookAuthorizeListener implements DialogListener {

        @Override
        public void onCancel() {

        }

        @Override
        public void onComplete(Bundle values) {
            Log.i("[facebook]", "authorise complete");
            
            mFacebookAccessToken = mFacebook.getAccessToken();
            if(mCallback != null) {
                mCallback.saveToken(PREF_KEY, mFacebookAccessToken);
            }
 
            new Thread(new Runnable() {

                @Override
                public void run() {

                    Bundle parameters = new Bundle();

                    try {
                        Log.i("[facebook]", "new thread second");
                        parameters.putString(Facebook.TOKEN, mFacebook.getAccessToken());
                        String userInfo = mFacebook.request("me", parameters, "GET");
                       
                        OAuthUser oUser = convertUser(userInfo);

                        if (mCallback != null) {
                            mCallback.onComplete(oUser);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onFacebookError(FacebookError e) {
        }

        @Override
        public void onError(DialogError e) {
        }
    }

    @Override
    public OAuthUser convertUser(String jsonString) {
        if (!Strings.isNullOrEmpty(jsonString)) {
            try {
                JSONObject json = Util.parseJson(jsonString);
                if (json != null) {
                    return new OAuthUser(json.getString(FB_JSON_ID_KEY),
                            json.getString(FB_JSON_NAME_KEY), OAuthVendor.FACEBOOK);
                }
            } catch (FacebookError e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
