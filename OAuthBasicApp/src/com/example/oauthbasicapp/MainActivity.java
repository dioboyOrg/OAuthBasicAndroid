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
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
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

	/** GA ID */
	private static final String GA_PROPERTY_ID = "UA-48219037-1";
	private static Tracker mTracker = null;
	
	private boolean mUseEasyTracker = true;        // EasyTracker 를 테스트하려면 true 세팅
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        if (!mUseEasyTracker) {
            mTracker = GoogleAnalytics.getInstance(this).getTracker(GA_PROPERTY_ID);
        }

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
                    if (mTracker != null) {
                        mTracker.send(MapBuilder.createEvent("UX", "BtnClick", "GoogleBtn", null)
                                .build());
                        GAServiceManager.getInstance().dispatchLocalHits();     // Manual Dispatch for Test (단, 설정하지않으면 default Periodic(30m) Dispatch로 동작함)
                    } else {
                        EasyTracker.getInstance(MainActivity.this).send(
                                MapBuilder.createEvent("Easy_UX", "BtnClick", "GoogleBtn", null)
                                        .build());
                        GAServiceManager.getInstance().dispatchLocalHits();     // Manual Dispatch for Test (단, 설정하지않으면 default Periodic(30m) Dispatch로 동작함)
                    }
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
                if (mFacebookLogin != null) {
                    if (mTracker != null) {
                        mTracker.send(MapBuilder.createEvent("UX", "BtnClick", "FacebookBtn", null)
                                .build());
                        GAServiceManager.getInstance().dispatchLocalHits();     // Manual Dispatch for Test (단, 설정하지않으면 default Periodic(30m) Dispatch로 동작함)
                    }  else {
                        EasyTracker.getInstance(MainActivity.this).send(
                                MapBuilder.createEvent("Easy_UX", "BtnClick", "FacebookBtn", null)
                                        .build());
                        GAServiceManager.getInstance().dispatchLocalHits();     // Manual Dispatch for Test (단, 설정하지않으면 default Periodic(30m) Dispatch로 동작함)
                    }
                    mFacebookLogin.login();
                }
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
	
    @Override
    protected void onStart() {
        super.onStart();
        if (mTracker != null) {
            mTracker.set(Fields.SCREEN_NAME, "Main Screen");
            mTracker.send(MapBuilder.createAppView().build());
        }
        // [EasyTracker] analytics.xml 에 Screen 이름 설정 추가해서 사용하는 방법 추가되어있음
        // 아래의 방법은 packagename+activityname 으로 화면이름 생성됨
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

     @Override
    protected void onStop() {
        super.onStop();
        if(mTracker != null) {
            mTracker.set(Fields.SCREEN_NAME, null);
        }
        // [EasyTracker] analytics.xml 에 Screen 이름 설정 추가해서 사용하는 방법 추가되어있음
        // [EasyTracker] xml 에 Screen 이름 설정 추가해서 사용하도록 변경
        // 아래의 방법은 packagename+activityname 으로 화면이름 생성됨
        EasyTracker.getInstance(this).activityStop(this);  // Add this method
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
