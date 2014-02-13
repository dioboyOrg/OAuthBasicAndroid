package com.example.oauthbasicapp;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.example.oauthbasicapp.mWebClient.WebClientCallBack;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;

public class MainActivity extends Activity implements OnClickListener {
	/* facebook Utils */
	String APP_ID = "708651932501499";
	String App_Serect = "c4f6f7f05e1c1f6071787e498d41e64c";
	String Redirect_Uri = "http://www.facebook.com/connect/login_success.html";
	String FB_URL = "https://graph.facebook.com/oauth/authorize?client_id="
			+ APP_ID + "&redirect_uri=" + Redirect_Uri
			+ "&type=user_agent&scope=email,read_stream,offline_access";

	private String mFacebookAccessToken;
	@SuppressWarnings("deprecation")
	Facebook mFacebook = new Facebook(APP_ID);
	private boolean flag = false;

	/* google Utils */
	String CLIENT_ID = "31860159396-r2lqrrabsjub8ilgfifvj2a3fqqhph1a.apps.googleusercontent.com";

	String API_KEY = "AIzaSyDJyn2oOrIwClrCD4kWp15NtzBC9FKw0rE";

	String CLIENT_SECRET = "";

	String SCOPE = "https://www.googleapis.com/auth/tasks";

	String ENDPOINT_URL = "https://www.googleapis.com/tasks/v1/users/@me/lists";

	String REDIRECT_URI = "http://localhost";

	TextView tv;

	WebView webview1;

	Button btn1, btn2;

	private String UserInform_Google = null;
	private String UserInform_Facebook = null;

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		flag = false;
		btn1 = (Button) findViewById(R.id.button1);
		btn2 = (Button) findViewById(R.id.button2);
		webview1 = (WebView) findViewById(R.id.webview1);
		tv = (TextView) findViewById(R.id.textView2);
		tv.setMovementMethod(new ScrollingMovementMethod());
		mFacebookAccessToken = getAppPreferences(MainActivity.this,
				"ACCESS_TOKEN");
		mFacebook.setAccessToken(mFacebookAccessToken);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);

		final WebSettings settings1 = webview1.getSettings();
		settings1.setDefaultTextEncodingName("utf-8");
		settings1.setJavaScriptEnabled(true);
		settings1.setJavaScriptCanOpenWindowsAutomatically(false);
		settings1.setRenderPriority(WebSettings.RenderPriority.HIGH);

		webview1.setWebViewClient(new mWebClient(mCallBack, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, ENDPOINT_URL, UserInform_Google, webview1, tv));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// Google Oauth 2.0 Example
		case R.id.button1:
			Log.i("button", "google button1 click");
			String oauthUrl = new GoogleAuthorizationRequestUrl(CLIENT_ID,
					REDIRECT_URI, SCOPE).build();
			webview1.loadUrl(oauthUrl);
			break;
		// Facebook Oauth 2.0 Example
		case R.id.button2:

			Log.i("button", "facebook button2 click");

			login();

			new Thread(new Runnable() {
				@Override
				public void run() {
					Bundle parameters = new Bundle();
					try {
						parameters.putString(Facebook.TOKEN,
								mFacebook.getAccessToken());
						UserInform_Facebook = mFacebook.request("me",
								parameters, "GET");
						Log.i("response", "첫번째 응답");
						JSONObject json = Util.parseJson(UserInform_Facebook);
						// UserInform = json.getString("name") + "\n"
						// + json.getString("id");
					} catch (FacebookError e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}).start();

			break;
		default:
			break;
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	private void login() {
//		if (!("".equals(mFacebookAccessToken)) && mFacebookAccessToken != null)
//			mFacebook.setAccessToken(mFacebookAccessToken);
//		else
			mFacebook.authorize(this, new String[] { "publish_stream, user_photos, email" }, new AuthorizeListener());
		
		Log.i("login", "authorie complete");

	}

	// app 쉐어드 프레퍼런스에 값 저장
	private void setAppPreferences(Activity context, String key, String value) {
		SharedPreferences pref = null;
		pref = context.getSharedPreferences("MainActivity", 0);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(key, value);

		prefEditor.commit();
	}

	// app 쉐어드 프레퍼런스에서 값을 읽어옴
	private String getAppPreferences(Activity context, String key) {
		String returnValue = null;

		SharedPreferences pref = null;
		pref = context.getSharedPreferences("MainActivity", 0);

		returnValue = pref.getString(key, "");

		return returnValue;
	}

	// Facebook 인증후 처리를 위한 callback class
	public class AuthorizeListener implements DialogListener {
		@Override
		public void onCancel() {
		}

		@Override
		public void onComplete(Bundle values) {

			Log.i("login", "authorizing");
			mFacebookAccessToken = mFacebook.getAccessToken();
			setAppPreferences(MainActivity.this, "ACCESS_TOKEN",
					mFacebookAccessToken);

			flag = true;
			Log.i("flag", "flag set true");

			new Thread(new Runnable() {

				@Override
				public void run() {

					Bundle parameters = new Bundle();

					try {
						Log.i("response", "response gogo");
						parameters.putString(Facebook.TOKEN,
								mFacebook.getAccessToken());
						UserInform_Facebook = mFacebook.request("me",
								parameters, "GET");
						Log.i("response", "response완료");
						Log.i("userinformed", "request me");
						JSONObject json = Util.parseJson(UserInform_Facebook);
						// UserInform = json.getString("name") + "\n"
						// + json.getString("id");
					} catch (FacebookError e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}

					MainActivity.this.runOnUiThread(new Runnable() {

						public void run() {
							Log.i("tv2", "facebook textview performed");
							if (flag) {
								Log.i("스레드", "runonuithread에서 정보 출력");
								tv.setText(UserInform_Facebook);
							}
						}
					});
				}
			}).start();

		}
		
		@Override
		public void onError(DialogError e) {
		}

		@Override
		public void onFacebookError(FacebookError e) {
		}
	}
	
	private WebClientCallBack mCallBack = new WebClientCallBack() {
		
		@Override
		public void callback(final String userinform) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					webview1.setVisibility(View.INVISIBLE);
					Log.i("tv1", "google textview performed");
					tv.setText(userinform);

				}
			});
			
		}
	};
}
