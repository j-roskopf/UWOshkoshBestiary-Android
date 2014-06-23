package com.example.uwoshkoshbestiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;
import android.provider.MediaStore;

public class NewSubmission extends Fragment {

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
	ImageView capturedPicture;

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
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else if (requestCode == VIDEO_CAPTURE) {
			if (resultCode == Activity.RESULT_OK) {
				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}
			} else {
				// failed to record video
				Toast.makeText(getActivity(), "Sorry! Failed to record video",
						Toast.LENGTH_SHORT).show();
			}
		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_VIDEO) {
			oldVideoFileUri = videoFileUri;
			Uri uri = data.getData();
			videoFileUri = uri;

			if (oldVideoFileUri != null) {
				deleteOldFile(oldVideoFileUri.getPath());
			}

		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_IMAGE) {
			oldImageFileUri = imageFileUri;
			Uri uri = data.getData();
			imageFileUri = uri;

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

			if (oldImageFileUri != null) {
				deleteOldFile(oldImageFileUri.getPath());
			}
		}
		// Kitkat
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_VIDEO) {
			oldVideoFileUri = videoFileUri;
			String path = getVideoPath(data.getData());
			
			Log.d("here",path);
			
			File externalFile = new File(path);
			Uri videoFile = Uri.fromFile(externalFile);

			videoFileUri = videoFile;

			if (oldVideoFileUri != null) {
				deleteOldFile(oldVideoFileUri.getPath());
			}

		}
		// Kitkat
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_IMAGE) {
			oldImageFileUri = imageFileUri;
			Uri selectedImageURI = data.getData();

			InputStream input = null;
			try {
				input = getActivity().getContentResolver()
						.openInputStream(selectedImageURI);
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

			final Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
			// Delete the old image. Don't want to take up too much space.
			if (oldImageFileUri != null) {
				deleteOldFile(oldImageFileUri.getPath());
			}

			capturedPicture.setImageBitmap(bitmap);

		}
	}

	public String getVideoPath(Uri uri){
		   Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
		   cursor.moveToFirst();
		   String document_id = cursor.getString(0);
		   document_id = document_id.substring(document_id.lastIndexOf(":")+1);
		   cursor.close();

		   cursor = getActivity().getContentResolver().query( 
		   android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
		   null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
		   cursor.moveToFirst();
		   String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
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

	}

}
