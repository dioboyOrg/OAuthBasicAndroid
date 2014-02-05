package com.example.oauthbasicandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final String REQUEST_TOKEN_URL = "https://apis.daum.net/oauth/requestToken";
	static final String AUTHORIZE_URL = "https://apis.daum.net/oauth/authorize";
	static final String ACCESS_TOKEN_URL = "https://apis.daum.net/oauth/accessToken";
	// url parsing

	static final String CONSUMER_KEY = "f851c4a4-826e-45da-968e-ae4516eedb91";
	static final String CONSUMER_SECRET = "aoqF.X5_72JI5hn_GPIloOS3F4R8rzQj1MOKn_FaT4gKRIhFezTssw00";

	static final String API_URL = "https://apis.daum.net";

	// httputil.encode~?

	String request_token; // 요청토큰
	String request_token_secret; // 요청토큰 시크릿
	String oauth_token; // 인증된 요청토큰
	String oauth_verifier; // 인증된 요청토큰 검증값
	String oauthUrl; // 접근토큰 파싱을 위한 URL
	String access_token; // 접근토큰
	String access_token_secret; // 접근토큰 시크릿

	TextView tv;
	WebView webview;
	Button btn;
	private OAuthProvider provider;
	private OAuthConsumer consumer;

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		oauth_token = uri.getQueryParameter("oauth_token");
		oauth_verifier = uri.getQueryParameter("oauth_verifier");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL,
				ACCESS_TOKEN_URL, AUTHORIZE_URL);

		setContentView(R.layout.activity_main);
		btn = (Button) findViewById(R.id.button1);
		webview = (WebView) findViewById(R.id.webview);
		
		// 자바스크립트를 활서오하해야 로그인 페이지가 작동함
		final WebSettings settings = webview.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url,
					Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
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

			}

		}); // setWebView
		
		try {
			// 요청토큰을 서버에 요청해, 로그인하ㅗ면 URL, 요청토큰, 요청토큰시크릿 갑승ㄹ
			// 추출
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						oauthUrl = provider
								.retrieveRequestToken(
										consumer,
										OAuth.OUT_OF_BAND);
						System.out
								.println("아래 URL로 가서 사용자 인증을 하시면 인증코드(verifier)를 얻을 수 있습니다.");
						System.out.println(oauthUrl);						
					} catch (OAuthMessageSignerException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (OAuthNotAuthorizedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (OAuthExpectationFailedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (OAuthCommunicationException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			}).start();

			webview.setVisibility(View.INVISIBLE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				webview.setVisibility(View.VISIBLE);
				webview.loadUrl(oauthUrl); // 인증 페이지 로딩
				
			}
		});
	}

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

	// onPageFinished로 넘어오는 URL에서 oauth_token, oauth_verifier 파싱하는 함수
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
