package com.awisconsinbestiary.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.VideoView;

public class ViewVideo extends Activity {
	
	//UI References
	VideoView vv;
	
	//Video Uri
	Uri videoUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vew_video);
		
		vv = (VideoView) findViewById(R.id.video);
		
		Intent intent = getIntent();
		videoUri = intent.getParcelableExtra("videoUri");
		if(videoUri != null){
			Log.d("D","123456789 " + videoUri.getPath());
		}
		
		
		vv.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(vv.isPlaying())
				{
					vv.pause();
				}
				else
				{
					vv.start();
				}
				return false;
			}
			
		});
		
		
		if(videoUri != null)
		{
			
			vv.setVideoPath(videoUri.getPath());
			vv.start();
		}
	}
}
