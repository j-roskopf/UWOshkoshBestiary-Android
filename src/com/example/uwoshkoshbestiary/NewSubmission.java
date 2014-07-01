package com.example.uwoshkoshbestiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;
import android.provider.MediaStore;

public class NewSubmission extends Fragment implements LocationListener {

	// UI References
	CheckBox cb;
	ScrollView sv;
	Spinner privacySpinner;
	Spinner observationalSpinner;
	Spinner countySpinner;
	Spinner groupSpinner;
	Spinner affiliationSpinner;
	Button photoVideoButton;
	Button viewVideoButton;
	Button audioButton;
	Button weatherButton;
	ImageView capturedPicture;
	EditText altitudeEditText;
	EditText longitudeEditText;
	EditText latitudeEditText;

	// Activity request codes
	private static final int VIDEO_CAPTURE = 101;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int GALLERY_CHOSEN_IMAGE = 1;
	public static final int GALLERY_CHOSEN_VIDEO = 2;
	public static final int GALLERY_KITKAT_INTENT_CALLED_IMAGE = 1337;
	public static final int GALLERY_KITKAT_INTENT_CALLED_VIDEO = 1338;

	// Path for image/video. I store thet old Uri so if the user records a new
	// video/image, the old one will be deleted
	Uri oldImageFileUri;
	Uri imageFileUri;
	Uri oldVideoFileUri;
	Uri videoFileUri;

	private static View view;

	// Location objects
	LocationManager lm;
	Location location;
	double longitude;
	double latitude;
	double altitude;
	Geocoder gcd;
	String locationToSend;
	LocationListener ll;

	// Used to determine weather direction from degrees
	String[] dirTable = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
			"S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// This if statement and try/catch prevent the map fragment from being
		// inflated twice.
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_new_submission,
					container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}

		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		altitudeEditText = (EditText) getActivity().findViewById(
				R.id.altitudeTextField);
		longitudeEditText = (EditText) getActivity().findViewById(
				R.id.longitudeTextField);
		latitudeEditText = (EditText) getActivity().findViewById(
				R.id.latitudeTextField);

		// Save location listener to fragment so you can stop updates later
		ll = this;

		lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				// Store the location
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				altitude = location.getAltitude();
				// Set TextField with gathered location
				altitudeEditText.setText(altitude + "");
				latitudeEditText.setText(latitude + "");
				longitudeEditText.setText(longitude + "");

				lm.removeUpdates(ll);

				// If weather is successfully retrieved, try and get weather too
				try {
					getWeather();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
						0, this);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
						this);
			}

		} else {
			// The user doesn't have location enabled.
			// Prompt them with an alert that they can click to go to their
			// settings
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());

			// set title
			alertDialogBuilder.setTitle("Alert: Location Disabled");

			// set dialog message
			alertDialogBuilder
					.setMessage("Unable to collect location")
					.setCancelable(false)
					.setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							})
					.setNegativeButton("Settings",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent dialogIntent = new Intent(
											android.provider.Settings.ACTION_SETTINGS);
									dialogIntent
											.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(dialogIntent);
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}

		// Set scrollview to be at the top
		sv = (ScrollView) getActivity().findViewById(R.id.container);
		sv.setVisibility(View.INVISIBLE);

		// Adds listener to check box
		cb = (CheckBox) getView().findViewById(R.id.TOSCheck);
		cb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					sv.fullScroll(ScrollView.FOCUS_UP);
					sv.setVisibility(View.VISIBLE);
				} else {
					sv.setVisibility(View.INVISIBLE);

				}
			}
		});

		// Add listener to audio button to start the audio recording activity
		audioButton = (Button) getActivity().findViewById(R.id.addAudio);
		audioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent ar = new Intent(getActivity(), AudioRecording.class);
				ar.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(ar);

			}

		});

		// Sets weather button to open new activity on press
		weatherButton = (Button) getActivity().findViewById(
				R.id.weatherDatabutton);
		weatherButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent w = new Intent(getActivity(), ViewWeather.class);
				w.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(w);

			}

		});

		capturedPicture = (ImageView) getActivity().findViewById(
				R.id.capturedPicture);
		photoVideoButton = (Button) getActivity().findViewById(
				R.id.photoVideoButton);
		// Shows an alert diaglog that allows the user to take a picture or
		// video
		photoVideoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (hasCamera()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle("Choose an option");
					builder.setItems(
							new CharSequence[] { "New Picture", "New Video",
									"Exisitng Picture", "Existing Video" },
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// The 'which' argument contains the index
									// position
									// of the selected item
									switch (which) {
									case 0:
										String timestamp = new SimpleDateFormat(
												"MM-dd-yyyy_HH-mm-ss aa")
												.format(Calendar.getInstance()
														.getTime());
										File filepath = Environment
												.getExternalStorageDirectory();
										File dir = new File(filepath
												.getAbsolutePath()
												+ "/UWOBestiary/");
										dir.mkdirs();
										File mediaFile = new File(Environment
												.getExternalStorageDirectory()
												.getAbsolutePath()
												+ "/UWOBestiary/Picture_"
												+ timestamp + ".jpg");
										Intent intent = new Intent(
												MediaStore.ACTION_IMAGE_CAPTURE);
										// Store the old Uri so it can be
										// deleted
										oldImageFileUri = imageFileUri;
										imageFileUri = Uri.fromFile(mediaFile);
										intent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												imageFileUri);
										startActivityForResult(intent,
												CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
										break;
									case 1:
										timestamp = new SimpleDateFormat(
												"MM-dd-yyyy_HH-mm-ss aa")
												.format(Calendar.getInstance()
														.getTime());
										filepath = Environment
												.getExternalStorageDirectory();
										dir = new File(filepath
												.getAbsolutePath()
												+ "/UWOBestiary/");
										dir.mkdirs();
										mediaFile = new File(
												Environment
														.getExternalStorageDirectory()
														.getAbsolutePath()
														+ "/UWOBestiary/VideoRecording_"
														+ timestamp + ".avi");
										intent = new Intent(
												MediaStore.ACTION_VIDEO_CAPTURE);
										oldVideoFileUri = videoFileUri;
										videoFileUri = Uri.fromFile(mediaFile);
										intent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												videoFileUri);
										intent.putExtra(
												MediaStore.EXTRA_DURATION_LIMIT,
												30);
										startActivityForResult(intent,
												VIDEO_CAPTURE);
										break;
									case 2:
										// Kitkat changed the way picking
										// from the gallery was done. So we
										// have to check and do it both ways
										if (Build.VERSION.SDK_INT < 19) {
											intent = new Intent();
											intent.setType("image/*");
											intent.setAction(Intent.ACTION_GET_CONTENT);
											startActivityForResult(intent,
													GALLERY_CHOSEN_IMAGE);
										} else {
											intent = new Intent(
													Intent.ACTION_OPEN_DOCUMENT);
											intent.addCategory(Intent.CATEGORY_OPENABLE);
											intent.setType("image/*");
											startActivityForResult(intent,
													GALLERY_KITKAT_INTENT_CALLED_IMAGE);
										}
										break;
									case 3:
										// Kitkat changed the way picking
										// from the gallery was done. So we
										// have to check and do it both ways
										if (Build.VERSION.SDK_INT < 19) {
											intent = new Intent();
											intent.setType("video/*");
											intent.setAction(Intent.ACTION_GET_CONTENT);
											startActivityForResult(intent,
													GALLERY_CHOSEN_VIDEO);
										} else {
											intent = new Intent(
													Intent.ACTION_OPEN_DOCUMENT);
											intent.addCategory(Intent.CATEGORY_OPENABLE);
											intent.setType("video/*");
											startActivityForResult(intent,
													GALLERY_KITKAT_INTENT_CALLED_VIDEO);
											break;
										}
									}
								}
							});
					builder.create().show();

				} else {
					// No camera present!
					Toast.makeText(getActivity(), "No camera detected",
							Toast.LENGTH_SHORT).show();
				}

			}

		});
		viewVideoButton = (Button) getActivity().findViewById(R.id.viewVideo);
		viewVideoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (videoFileUri != null) {
					Intent vv = new Intent(getActivity(), ViewVideo.class);
					// Pass video URI
					vv.putExtra("videoUri", videoFileUri);
					vv.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(vv);
				} else {
					Toast.makeText(getActivity(),
							"You must record a video first!",
							Toast.LENGTH_SHORT).show();
				}

			}

		});

		// Initialize image/video Uri
		oldImageFileUri = null;
		oldVideoFileUri = null;
		imageFileUri = null;
		videoFileUri = null;

	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			// successfully captured the image
			// display it in image view
			try {
				// Delete the old image. Don't want to take up too much space.
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}

				// bimatp factory
				BitmapFactory.Options options = new BitmapFactory.Options();

				// downsizing image as it throws OutOfMemory Exception for
				// larger
				// images
				options.inSampleSize = 2;

				final Bitmap bitmap = BitmapFactory.decodeFile(
						imageFileUri.getPath(), options);

				capturedPicture.setImageBitmap(bitmap);

				// Save file path to Entry class
				Entry.setPhotoPath(imageFileUri.getPath());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_VIDEO) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				oldVideoFileUri = videoFileUri;
				Uri uri = data.getData();
				videoFileUri = uri;

				// Save file path to Entry class
				Entry.setPhotoPath(videoFileUri.getPath());

				// Delete old recorded video
				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}
			}

		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_IMAGE) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				oldImageFileUri = imageFileUri;
				Uri uri = data.getData();
				imageFileUri = uri;

				// Save file path to Entry class
				Entry.setPhotoPath(imageFileUri.getPath());

				// Delete the old image. Don't want to take up too much space.
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}

				// bimatp factory
				BitmapFactory.Options options = new BitmapFactory.Options();

				// downsizing image as it throws OutOfMemory Exception for
				// larger
				// images
				options.inSampleSize = 2;

				final Bitmap bitmap = BitmapFactory.decodeFile(
						imageFileUri.getPath(), options);

				capturedPicture.setImageBitmap(bitmap);

				// Delete the old image
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}
			}

		}
		// Kitkat
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_VIDEO) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				oldVideoFileUri = videoFileUri;
				String path = getVideoPath(data.getData());

				// Save file path to Entry class
				Entry.setPhotoPath(path);

				File externalFile = new File(path);
				Uri videoFile = Uri.fromFile(externalFile);

				videoFileUri = videoFile;

				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}
			}

		}
		// Kitkat
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_IMAGE) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				oldImageFileUri = imageFileUri;

				Uri selectedImageURI = data.getData();

				String pathToSelectedImage = getImagePath(selectedImageURI);

				// Save file path to Entry class
				Entry.setPhotoPath(pathToSelectedImage);

				InputStream input = null;
				try {
					input = getActivity().getContentResolver().openInputStream(
							selectedImageURI);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// bimatp factory
				BitmapFactory.Options options = new BitmapFactory.Options();

				// downsizing image as it throws OutOfMemory Exception for
				// larger
				// images
				options.inSampleSize = 2;

				final Bitmap bitmap = BitmapFactory.decodeStream(input, null,
						options);
				// Delete the old image. Don't want to take up too much space.
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}

				capturedPicture.setImageBitmap(bitmap);

			}

		}
	}

	public String getVideoPath(Uri uri) {
		Cursor cursor = getActivity().getContentResolver().query(uri, null,
				null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
		cursor.close();

		cursor = getActivity().getContentResolver().query(
				android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				null, MediaStore.Images.Media._ID + " = ? ",
				new String[] { document_id }, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Video.Media.DATA));
		cursor.close();

		return path;
	}

	public String getImagePath(Uri uri) {
		Cursor cursor = getActivity().getContentResolver().query(uri, null,
				null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
		cursor.close();

		cursor = getActivity().getContentResolver().query(
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null, MediaStore.Images.Media._ID + " = ? ",
				new String[] { document_id }, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Video.Media.DATA));
		cursor.close();

		return path;
	}

	private void deleteOldFile(String path) {
		File fdelete = new File(path);
		if (fdelete.exists()) {
			if (fdelete.delete()) {
				Log.d("here", "deleted " + path);
			} else {
				Log.d("here", "not deleted " + path);
			}
		} else {
			Log.d("doesn't exist", "not deleted " + path);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Checks if the phone has a camera
	private boolean hasCamera() {
		if (getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// If the user isn't going to save the entry, delete the recorded media
		// as to not take up that much space
		if (oldVideoFileUri != null) {
			deleteOldFile(oldVideoFileUri.getPath());
		}
		if (videoFileUri != null) {
			deleteOldFile(videoFileUri.getPath());
		}
		if (oldImageFileUri != null) {
			deleteOldFile(oldImageFileUri.getPath());
		}
		if (imageFileUri != null) {
			deleteOldFile(imageFileUri.getPath());
		}

		// Stop location manager
		lm.removeUpdates(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		// Try and get the location again
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					this);
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// Store the location
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		altitude = location.getAltitude();

		// Set TextField with gathered location
		altitudeEditText.setText(arg0.getAltitude() + "");
		latitudeEditText.setText(arg0.getLatitude() + "");
		longitudeEditText.setText(arg0.getLongitude() + "");

		lm.removeUpdates(ll);

		// Stop location manager
		lm.removeUpdates(this);

		// If weather is successfully retrieved, try and get weather too
		try {
			getWeather();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	public void getWeather() throws JSONException {

		String url = "http://api.openweathermap.org/data/2.5/weather?lat="
				+ latitude + "&lon=" + longitude;

		new AsynchWeather().execute(url);
	}

	// Class that makes an Async call to the open weather api to return the
	// weather
	public class AsynchWeather extends AsyncTask<String, Context, String> {

		@Override
		protected String doInBackground(String... url) {

			HTTPGet hg = new HTTPGet(url[0]);

			return hg.responseString;

		}

		@Override
		protected void onPostExecute(String jsonValue) {
			// PARSE JSON VALUES
			// Gson gson = new Gson();
			// Weather w = gson.fromJson(jsonValue,Weather.class);

			JSONObject jObj;
			try {
				jObj = new JSONObject(jsonValue);
				JSONObject windObj = getObject("wind", jObj);

				Entry.setWindDirection(dirTable[(int) Math.floor((getDouble(
						"deg", windObj) + 11.25) / 22.5)]);
				Entry.setWindSpeed(getDouble("speed", windObj) + "");

				JSONObject mainObj = getObject("main", jObj);
				Entry.setTemperature((getDouble("temp", mainObj) - 273.15) + "");
				Entry.setPressure(getDouble("pressure", mainObj) + "");

				// Open weather can return the precipitation measure in either
				// 1h,2h,or 3h. Have to check for all 3
				JSONObject rainObj = getObject("rain", jObj);

				if (rainObj.has("1h")) {
					Entry.setPrecipitationMeasure("1h");
					Entry.setPrecipitation(getDouble("1h", rainObj) + "");
				} else if (rainObj.has("2h")) {
					Entry.setPrecipitationMeasure("2h");
					Entry.setPrecipitation(getDouble("2h", rainObj) + "");
				} else if (rainObj.has("3h")) {
					Entry.setPrecipitationMeasure("3h");
					Entry.setPrecipitation(getDouble("3h", rainObj)+"");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private JSONObject getObject(String tagName, JSONObject jObj)
				throws JSONException {
			JSONObject subObj = jObj.getJSONObject(tagName);
			return subObj;
		}

		private String getString(String tagName, JSONObject jObj)
				throws JSONException {
			return jObj.getString(tagName);
		}

		private double getDouble(String tagName, JSONObject jObj)
				throws JSONException {
			return jObj.getDouble(tagName);
		}

		private int getInt(String tagName, JSONObject jObj)
				throws JSONException {
			return jObj.getInt(tagName);
		}
	}

}
