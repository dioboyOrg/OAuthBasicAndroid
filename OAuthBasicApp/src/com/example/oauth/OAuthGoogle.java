package com.example.oauth;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

public class OAuthGoogle {
    
    public interface OAuthGoogleCallBack {
        public void onComplete(OAuthUser user);
    }
    
    /** google Defines */
//    private final String API_KEY = "AIzaSyDJyn2oOrIwClrCD4kWp15NtzBC9FKw0rE";
    private final String SCOPE = "https://www.googleapis.com/auth/tasks";
    private final String ENDPOINT_URL = "https://www.googleapis.com/tasks/v1/users/@me/lists";
    private final String REDIRECT_URI = "http://localhost";
    private final String CLIENT_SECRET = "";
    
    private WebView mWebView = null;
    private OAuthGoogleCallBack mCallback = null;
    
    private String mClientID = null;
    
    
    public OAuthGoogle(String clientID, WebView webview, OAuthGoogleCallBack callback) {
        mClientID = clientID;
        mWebView = webview;
        mCallback = callback;
        if (mWebView != null) {
            final WebSettings settings1 = mWebView.getSettings();
            settings1.setDefaultTextEncodingName("utf-8");
            settings1.setJavaScriptEnabled(true);
            settings1.setJavaScriptCanOpenWindowsAutomatically(false);
            // settings1.setRenderPriority(WebSettings.RenderPriority.HIGH);

            mWebView.setWebViewClient(mWebViewClient);
        }
    }
    
    public void login() {
        if(mWebView != null) {
            String oauthUrl = new GoogleAuthorizationRequestUrl(mClientID,
                    REDIRECT_URI, SCOPE).build();
            mWebView.loadUrl(oauthUrl);
        }
    }
    
    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i("[google]", "google webview start");
            if (mWebView != null) {
                mWebView.setVisibility(View.VISIBLE);
            }
        }
        
        private OAuthUser parseResult(String jsonString) {
            if (!Strings.isNullOrEmpty(jsonString)) {
                try {
                    JSONObject json = new JSONObject(jsonString);
                    String items = json.getString("items");
                    if (!Strings.isNullOrEmpty(items)) {
                        json = new JSONObject(items.substring(1, items.length()-1));
                        return new OAuthUser(json.getString("id"),
                                "name", OAuthVendor.GOOGLE);
                    }
                    Log.i("[google]", json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mWebView != null && url != null && url.length() > 0) {
                final String pageUrl = url;
                Log.i("[google]", "webview geturl() complete : " + url);

                if (pageUrl != null && pageUrl.startsWith(REDIRECT_URI)) {
                    mWebView.setVisibility(View.GONE);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Log.i("[google]", "running new thread");

                            JacksonFactory jsonFactory = new JacksonFactory();// json
                            HttpTransport transport = new NetHttpTransport();// http
                            String code = pageUrl.substring(REDIRECT_URI.length() + 7,
                                    pageUrl.length());
                            try {
                                AccessTokenResponse accessTokenResponse = new GoogleAuthorizationCodeGrant(
                                        transport, jsonFactory, mClientID, CLIENT_SECRET,
                                        code, REDIRECT_URI)
                                        .execute();

                                GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(
                                        accessTokenResponse.accessToken, transport,
                                        jsonFactory, mClientID, CLIENT_SECRET,
                                        accessTokenResponse.refreshToken);
                                HttpRequestFactory rf = transport
                                        .createRequestFactory(accessProtectedResource);
                                GenericUrl endPoint = new GenericUrl(ENDPOINT_URL);
                                try {
                                    HttpRequest request = rf.buildGetRequest(endPoint);
                                    final com.google.api.client.http.HttpResponse response = request
                                            .execute();
                                    String authResultString = response.parseAsString();
                                    OAuthUser user = parseResult(authResultString);
                                    if (mCallback != null) {
                                        mCallback.onComplete(user);
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }
    };
}
