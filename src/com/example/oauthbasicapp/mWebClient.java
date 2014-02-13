package com.example.oauthbasicapp;

import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;


class mWebClient extends WebViewClient {
	private String ID, SECRET, REDI_URL, ENDP_URL, USR_INFO;
	private WebView webview;
	private TextView textview;
	private WebClientCallBack mCallBack;

	public mWebClient(WebClientCallBack callback, String ID, String SECRET, String REDI_URL,
			String ENDP_URL, String USR_INFO, WebView webview,
			TextView textview) {
		this.ID = ID;
		this.SECRET = SECRET;
		this.REDI_URL = REDI_URL;
		this.ENDP_URL = ENDP_URL;
		this.USR_INFO = USR_INFO;
		this.webview = webview;
		this.textview = textview;
		mCallBack = callback;

	}

@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		webview.setVisibility(View.VISIBLE);
		super.onPageStarted(view, url, favicon);
		Log.i("webview", "googlewebview");
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);

		new Thread(new Runnable() {
			@Override
			public void run() {
				JacksonFactory jsonFactory = new JacksonFactory();// json
				HttpTransport transport = new NetHttpTransport();// http
				String code = webview.getUrl().substring(
						REDI_URL.length() + 7,
						webview.getUrl().length());
				com.google.api.client.auth.oauth2.draft10.AccessTokenResponse accessTokenResponse;
				try {
					accessTokenResponse = new GoogleAuthorizationCodeGrant(
							transport, jsonFactory, ID,
							SECRET, code, REDI_URL).execute();

					GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(
							accessTokenResponse.accessToken, transport,
							jsonFactory, ID, SECRET,
							accessTokenResponse.refreshToken);
					HttpRequestFactory rf = transport
							.createRequestFactory(accessProtectedResource);
					GenericUrl endPoint = new GenericUrl(ENDP_URL);
					try {
						HttpRequest request = rf.buildGetRequest(endPoint);
						final com.google.api.client.http.HttpResponse response = request
								.execute();
						USR_INFO = response.parseAsString();
						if (mCallBack != null) {
							mCallBack.callback(USR_INFO);
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

	public interface WebClientCallBack {
		public void callback(String str);
	}
}