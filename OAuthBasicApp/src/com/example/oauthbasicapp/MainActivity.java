package com.example.oauthbasicapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.example.oauth.OAuthDefines;
import com.example.oauth.OAuthFacebook;
import com.example.oauth.OAuthFacebook.OAuthFacebookCallBack;
import com.example.oauth.OAuthGoogle;
import com.example.oauth.OAuthGoogle.OAuthGoogleCallBack;
import com.example.oauth.OAuthUser;
import com.google.common.base.Strings;

public class MainActivity extends Activity {
     
    /** Controls */
	private TextView mUserInfoTextView;
	private WebView mGoogleWebView;
	private Button mGoogleBtn;
	private Button mFacebookBtn;
	
	/** OAuth classes */
	private OAuthGoogle mGoogleLogin = null; 
	private OAuthFacebook mFacebookLogin = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		// user info view
		mUserInfoTextView = (TextView) findViewById(R.id.resultTextview);
		
		// initialize Google+
		mGoogleWebView = (WebView) findViewById(R.id.webview1);
		mGoogleLogin = new OAuthGoogle(OAuthDefines.GOOGLE_CLIENT_ID, mGoogleWebView, mGoogleLoginCallBack);
		
		mGoogleBtn = (Button) findViewById(R.id.googleBtn);
		mGoogleBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("[google]", "google button click");
                if (mGoogleLogin != null) {
                    mGoogleLogin.login();
                }
            }
        });

		// initialize Facebook
        mFacebookBtn = (Button) findViewById(R.id.facebookBtn);
		
		mFacebookLogin = new OAuthFacebook(OAuthDefines.FACEBOOK_APP_ID, this, mFacebookCallback);
		String token = getAppPreferences(this, OAuthFacebook.PREF_KEY);
		if(!Strings.isNullOrEmpty(token)) {
		    mFacebookLogin.setAccessToken(token);
		}
		
		mFacebookBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("[facebook]", "facebook button click");
                mFacebookLogin.login();
            }
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        if (mFacebookLogin != null) {
            mFacebookLogin.onActivityResult(requestCode, resultCode, data);
        }
	}

	/**
	 *  app 쉐어드 프레퍼런스에서 값을 읽어옴
	 * @param context Activity context
	 * @param key preference key
	 * @return preferecne value
	 */
	private String getAppPreferences(Activity context, String key) {
		String returnValue = null;

		SharedPreferences pref = null;
		pref = context.getSharedPreferences("MainActivity", 0);

		returnValue = pref.getString(key, "");

		return returnValue;
	}

	private OAuthGoogleCallBack mGoogleLoginCallBack = new OAuthGoogleCallBack() {

		@Override
		public void onComplete(final OAuthUser userinform) {
            if (userinform == null) {
                return;
            }
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
				    Log.i("[google]", "google textview performed");
			        mUserInfoTextView.setText(userinform.toString());
				}
			});
		}
	};
	
	private OAuthFacebookCallBack mFacebookCallback = new OAuthFacebookCallBack() {

        @Override
        public void saveToken(String key, String token) {
            SharedPreferences pref = null;
            pref = MainActivity.this.getSharedPreferences("MainActivity", 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putString(key, token);

            prefEditor.commit();
        }

        @Override
        public void onComplete(final OAuthUser userinform) {
            if (userinform == null) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(userinform != null) {
                        mUserInfoTextView.setText(userinform.toString());
                    }
                }
            });
        }
	};
}
