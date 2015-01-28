package com.awisconsinbestiary.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HTTPGet {
	HttpClient httpclient;
	HttpGet httpget; 
	List<NameValuePair> params;
	HttpResponse response = null;
	HttpEntity entity;
	InputStream instream;
	
	ResponseHandler<String> responseHandler;
    String responseString =""; 
    



	public HTTPGet(String url) {
		
		responseHandler = new BasicResponseHandler();
		httpclient = new DefaultHttpClient();
		
		httpget = new HttpGet(url);

		

		try {
			
			responseString = httpclient.execute(httpget,responseHandler);
		} catch (UnsupportedEncodingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
}
