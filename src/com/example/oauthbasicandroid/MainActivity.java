package com.example.oauthbasicandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.api.client.auth.oauth2.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpResponse;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	/*facebook Utils*/
	String APP_ID = "708651932501499";
	String App_Serect = "c4f6f7f05e1c1f6071787e498d41e64c";
	String FB_URL = "https://www.facebook.com/dialog/oauth?client_id=708651932501499&redirect_uri=http://psgyes.egloos.com/&scope=email,read_stream,offline_access";
	static String Get_URL = "";
	
	/*google Utils*/
	String CLIENT_ID = "31860159396-r2lqrrabsjub8ilgfifvj2a3fqqhph1a.apps.googleusercontent.com";
	String API_KEY = "AIzaSyDJyn2oOrIwClrCD4kWp15NtzBC9FKw0rE";
	String CLIENT_SECRET = "";
	String SCOPE = "https://www.googleapis.com/auth/tasks";
	String ENDPOINT_URL = "https://www.googleapis.com/tasks/v1/users/@me/lists";
	String REDIRECT_URI = "http://localhost";
	
	TextView tv;
	WebView webview;
	Button btn;
	private OAuthProvider provider;
	private OAuthConsumer consumer;
	private String parseResultString = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		try {
			URL url = new URL(FB_URL);
			URLConnection conn = url.openConnection();
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
		*/
		setContentView(R.layout.activity_main);
		btn = (Button) findViewById(R.id.button1);
		webview = (WebView) findViewById(R.id.webview);
		
		// 자바스크립트를 활서오하해야 로그인 페이지가 작동함
		final WebSettings settings = webview.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String oauthUrl = new GoogleAuthorizationRequestUrl(CLIENT_ID, REDIRECT_URI, SCOPE).build();
				
				//webview.loadUrl(FB_URL);
				webview.loadUrl(oauthUrl); // 인증 페이지 로딩
				
			}
		});

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url,
					Bitmap favicon) {
				webview.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
				System.out
				.println("아래 URL로 가서 사용자 인증을 하시면 인증코드(verifier)를 얻을 수 있습니다.");
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				/*
				if (processUrl(url)) {
					// accesstoken과 verifier값을 받아옴.
					Uri uri = Uri.parse(url);
					String verifier = uri
							.getQueryParameter(OAuth.OAUTH_VERIFIER);
					String token = uri
							.getQueryParameter(OAuth.OAUTH_TOKEN);
					access_token = consumer.getToken();
					access_token_secret = consumer.getTokenSecret();
					
					try {
						getMyCafeList();
					} catch (OAuthMessageSignerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				 */
				/*
				Log.i("webview", Get_URL);
				Uri uri = Uri.parse(FB_URL);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
				*/
				Get_URL = webview.getUrl();
				super.onPageFinished(view, url);

				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						JacksonFactory jsonFactory = new JacksonFactory();//json 처리
						HttpTransport transport = new NetHttpTransport();//http 처리
						String code = Get_URL.substring(REDIRECT_URI.length()+7, Get_URL.length());//접근 토큰 요청시 넘길 코드값
						com.google.api.client.auth.oauth2.draft10.AccessTokenResponse accessTokenResponse;//접근 토큰 요청 객체 생성
						try {
							accessTokenResponse = new GoogleAuthorizationCodeGrant(transport, jsonFactory, CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URI).execute();
							//접근 토큰 요청을 활용한 task api 호출
							GoogleAccessProtectedResource accessProtectedResource
							= new GoogleAccessProtectedResource(accessTokenResponse.accessToken, transport, jsonFactory, CLIENT_ID, CLIENT_SECRET, accessTokenResponse.refreshToken);
							HttpRequestFactory rf = transport.createRequestFactory(accessProtectedResource);
							GenericUrl endPoint = new GenericUrl(ENDPOINT_URL);
							try {
								HttpRequest request = rf.buildGetRequest(endPoint);
								final com.google.api.client.http.HttpResponse response = request.execute();
								parseResultString = response.parseAsString();

								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										webview.setVisibility(View.INVISIBLE);
										tv.setText(parseResultString);
									}
								});
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} // setWebView
						
					}
				}).start();
			}
			
		});
		
	}

/*
	private void getMyCafeList() throws IOException,
			OAuthMessageSignerException, OAuthExpectationFailedException,
			OAuthCommunicationException {
		// TODO Auto-generated method stub
		URL url = new URL(API_URL + "/cafe/favorite_cafes.json");
		HttpURLConnection request = (HttpURLConnection) url.openConnection();

		// oauth_signature 값을 얻습니다.
		consumer.sign(request);

		request.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		String tmpStr = "";
		while ((tmpStr = br.readLine()) != null) {
			System.out.println(tmpStr);
		}

	}
*/
	// onPageFinished로 넘어오는 URL에서 oauth_token, oauth_verifier 파싱하는 함수
	/*
	private boolean processUrl(String url) {
		for (String param : url.split("&")) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			if (name.equals("oauth_token"))
				oauth_token = value;
			if (name.equals("oauth_verifier"))
				oauth_verifier = value;

		}
		return (oauth_token != null && oauth_verifier != null);
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
