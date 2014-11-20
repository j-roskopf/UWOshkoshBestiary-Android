package com.example.uwoshkoshbestiary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import database.DatabaseHelper;
import database.Entry;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.provider.MediaStore;

public class NewSubmission extends Fragment implements LocationListener {

	private final String URL_FOR_SUBMISSION = "https://plonedev.uwosh.edu/zmi41/sites4/Bestiary/bestiary-submissions";

	// Shared preferences
	SharedPreferences prefs;

	// UI References

	// TOS checkbox
	private CheckBox cb;
	private ScrollView sv;
	private Spinner privacySpinner;
	private Spinner observationalSpinner;
	private Spinner countySpinner;
	private Spinner groupSpinner;
	private Spinner affiliationSpinner;
	private Button photoVideoButton;
	private Button viewVideoButton;
	private Button audioButton;
	private Button weatherButton;
	private Button discardButton;
	private ImageView capturedPicture;
	private Button saveButton;
	private Button submitButton;
	private Button manualLocationButton;
	private TextView audioStatus;
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText commonName;
	private EditText species;
	private EditText amount;
	private EditText behavorialDescription;
	private EditText ecosystem;
	private EditText observationalTechniqueOther;
	private EditText additionalInformation;
	private EditText altitudeEditText;
	private EditText longitudeEditText;
	private EditText latitudeEditText;
	private ProgressDialog prgDialog;

	// Activity request codes
	private static final int VIDEO_CAPTURE = 101;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int GALLERY_CHOSEN_IMAGE = 1;
	public static final int GALLERY_CHOSEN_VIDEO = 2;
	public static final int GALLERY_KITKAT_INTENT_CALLED_IMAGE = 1337;
	public static final int GALLERY_KITKAT_INTENT_CALLED_VIDEO = 1338;

	// Path for image/video. I store thet old Uri so if the user records a new
	// video/image, the old one will be deleted
	private Uri oldImageFileUri;
	private Uri imageFileUri;
	private Uri oldVideoFileUri;
	private Uri videoFileUri;

	private static View view;

	// Location objects
	private LocationManager lm;
	private Location location;
	private static double longitude;
	private static double latitude;
	private static double altitude;
	private Geocoder gcd;
	private String locationToSend;
	private LocationListener ll;

	// Using existing picture or not
	boolean comingFromExisting = false;

	// Used to store if the location was successfully retrieved.
	static boolean retrievedLocation;

	// Used to manually switch to the new submission tab when the user clicks on
	// an entry
	private android.app.ActionBar ab;

	// Used to determine weather direction from degrees
	private String[] dirTable = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE",
			"SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

	// Database helper
	private DatabaseHelper db;

	// Context
	private Context c;

	// Current Entry
	static Entry e;

	// holds whether or not the user is coming from the existing submission tab
	static boolean comingFromExistingSubmission;

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
		}

		return view;

	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser && comingFromExistingSubmission) {

			// set the check box

			cb.setChecked(isVisibleToUser);
			sv.setVisibility(1);
			sv.fullScroll(ScrollView.FOCUS_UP);

			// Depending on the orientation of the phone when the image was
			// taken, the image can be displayed
			// sideways in the image view. This will rotate the image correctly
			Matrix matrix = new Matrix();
			try {
				if (e.getPhotoPath() != null
						&& new File(e.getPhotoPath()).exists()) {
					ExifInterface exif = new ExifInterface(e.getPhotoPath());
					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, 1);

					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						matrix.postRotate(90);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						matrix.postRotate(180);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						matrix.postRotate(270);
					}
					// Setting the image stuff
					BitmapFactory.Options options = new BitmapFactory.Options();
					// downsizing image as it throws OutOfMemory Exception for
					// larger
					// images
					options.inSampleSize = 8;
					Bitmap bitmap = BitmapFactory.decodeFile(e.getPhotoPath(),
							options);

					bitmap = Bitmap
							.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
									bitmap.getHeight(), matrix, true); // rotating
																		// bitmap

					capturedPicture.setImageBitmap(bitmap);
				} else {
					capturedPicture.setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_menu_gallery));
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Setting the video stuff
			if (e.getVideoPath() != null) {
				videoFileUri = Uri.fromFile(new File(e.getVideoPath()));
			}

			// No need to set anything with the audio

			firstName.setText(e.getFirstName());
			lastName.setText(e.getLastName());
			email.setText(e.getEmail());

			// set affiliation spinner
			ArrayAdapter adapter = (ArrayAdapter) affiliationSpinner
					.getAdapter(); // cast to an ArrayAdapter
			int spinnerPosition = adapter.getPosition(e.getAffiliation());
			// set the default according to value
			affiliationSpinner.setSelection(spinnerPosition);

			if (e.getAudioPath() != null) {
				audioStatus.setText("Recorded");
			}

			// set group spinner
			adapter = (ArrayAdapter) groupSpinner.getAdapter(); // cast to an
																// ArrayAdapter
			spinnerPosition = adapter.getPosition(e.getGroup());
			// set the default according to value
			groupSpinner.setSelection(spinnerPosition);

			commonName.setText(e.getCommonName());
			species.setText(e.getSpecies());
			amount.setText(e.getAmount());
			behavorialDescription.setText(e.getBehavorialDescription());

			// set county spinner
			adapter = (ArrayAdapter) countySpinner.getAdapter(); // cast to an
																	// ArrayAdapter
			spinnerPosition = adapter.getPosition(e.getCounty());
			// set the default according to value
			countySpinner.setSelection(spinnerPosition);

			// set observational technique spinner
			adapter = (ArrayAdapter) observationalSpinner.getAdapter(); // cast
																		// to an
																		// ArrayAdapter
			spinnerPosition = adapter
					.getPosition(e.getObservationalTechnique());
			// set the default according to value
			observationalSpinner.setSelection(spinnerPosition);

			observationalTechniqueOther.setText(e
					.getObservationalTechniqueOther());
			ecosystem.setText(e.getEcosystemType());
			additionalInformation.setText(e.getAdditionalInformation());

			if (e.getLatitude() != null) {
				latitudeEditText.setText(e.getLatitude());
			}
			if (e.getLongitude() != null) {
				longitudeEditText.setText(e.getLongitude());
			}
			if (e.getAltitude() != null) {
				altitudeEditText.setText(e.getAltitude());
			}
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Store action bar
		ab = getActivity().getActionBar();

		// Start a new entry
		e = new Entry();

		// No location yet!
		retrievedLocation = false;

		// Store context
		c = getActivity();

		// instantiate database helper
		db = new DatabaseHelper(getActivity());

		// instantiate all of the edit texts / ui elements
		affiliationSpinner = (Spinner) getActivity().findViewById(
				R.id.affiliationSpinner);
		groupSpinner = (Spinner) getActivity().findViewById(R.id.groupSpinner);
		countySpinner = (Spinner) getActivity()
				.findViewById(R.id.countySpinner);
		observationalSpinner = (Spinner) getActivity().findViewById(
				R.id.observationTechniqueSpinner);
		privacySpinner = (Spinner) getActivity().findViewById(
				R.id.privacySpinner);
		altitudeEditText = (EditText) getActivity().findViewById(
				R.id.altitudeTextField);
		altitudeEditText = (EditText) getActivity().findViewById(
				R.id.altitudeTextField);
		longitudeEditText = (EditText) getActivity().findViewById(
				R.id.longitudeTextField);
		latitudeEditText = (EditText) getActivity().findViewById(
				R.id.latitudeTextField);
		firstName = (EditText) getActivity().findViewById(
				R.id.firstNameTextField);
		lastName = (EditText) getActivity()
				.findViewById(R.id.lastNameTextField);
		email = (EditText) getActivity().findViewById(R.id.emailTextField);
		commonName = (EditText) getActivity().findViewById(
				R.id.commonNameTextField);
		species = (EditText) getActivity().findViewById(R.id.speciesTextField);
		amount = (EditText) getActivity().findViewById(R.id.amountTextField);
		behavorialDescription = (EditText) getActivity().findViewById(
				R.id.behavorialDescriptionTextField);
		observationalTechniqueOther = (EditText) getActivity().findViewById(
				R.id.observationTechniqueOtherTextField);
		ecosystem = (EditText) getActivity().findViewById(
				R.id.ecosystemTypeTextField);
		additionalInformation = (EditText) getActivity().findViewById(
				R.id.additionalInformationTextField);
		audioStatus = (TextView) getActivity().findViewById(R.id.audioStatus);

		// Set first/last/email if the user has already entered in an entry
		prefs = getActivity().getSharedPreferences(
				"com.example.uwoshkoshbestiary", Context.MODE_PRIVATE);

		firstName.setText(prefs.getString("firstName", ""));
		lastName.setText(prefs.getString("lastName", ""));
		email.setText(prefs.getString("email", ""));

		// Save location listener to fragment so you can stop updates later
		ll = this;

		collectLocation();

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

		// Button to collect location manually if user has location turned off
		// on startup of app
		manualLocationButton = (Button) getActivity().findViewById(
				R.id.collectionLocationManuallyButton);
		manualLocationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				collectLocation();

			}

		});

		// Add listener to audio button to start the audio recording activity
		audioButton = (Button) getActivity().findViewById(R.id.addAudio);
		audioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent ar = new Intent(getActivity(), AudioRecording.class);
				Bundle mBundle = new Bundle();
				mBundle.putSerializable("entry", e);
				ar.putExtras(mBundle);
				ar.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(ar);
			}

		});

		// Add listener to discard button to clear the fields
		discardButton = (Button) getActivity().findViewById(R.id.discardButton);
		discardButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				new AlertDialog.Builder(getActivity())
						.setTitle("Confirm discard")
						.setMessage(
								"You will loose all data regarding this submission")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										clearForm();

									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								}).show();

			}

		});
		// Sets weather button to open new activity on press
		weatherButton = (Button) getActivity().findViewById(
				R.id.weatherDatabutton);
		weatherButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent w = new Intent(getActivity(), ViewWeather.class);
				Bundle mBundle = new Bundle();
				mBundle.putSerializable("entry", e);
				w.putExtras(mBundle);
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
												"yyyy-MM-dd-HH-mm-ss")
												.format(Calendar.getInstance()
														.getTime());
										e.setUsingExistingPhotoOrVideo(true);
										e.setPhotoTime(timestamp);
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
												"yyyy-MM-dd-HH-mm-ss")
												.format(Calendar.getInstance()
														.getTime());
										e.setVideoTime(timestamp);
										e.setUsingExistingPhotoOrVideo(true);

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
										e.setUsingExistingPhotoOrVideo(false);

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

										}
										e.setUsingExistingPhotoOrVideo(false);

										break;

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
		// Opens the activity that the user can view their video
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

		// Attempts to save the entry into the database
		saveButton = (Button) getActivity().findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String message;

				DateFormat dateFormat = new SimpleDateFormat(
						"MM/dd/yyyy HH:mm:ss");
				String timestamp = dateFormat.format(new Date());
				e.setCurrentTime(timestamp);

				// Save preferences
				Editor editor = prefs.edit();
				editor.putString("firstName", firstName.getText().toString());
				editor.putString("lastName", lastName.getText().toString());
				editor.putString("email", email.getText().toString());
				editor.commit();

				if (comingFromExistingSubmission) {
					// Not in a new submission anymore
					comingFromExistingSubmission = false;

					// Store all of the information
					e.setFirstName(firstName.getText().toString());
					e.setLastName(lastName.getText().toString());
					e.setEmail(email.getText().toString());
					e.setAffiliation(affiliationSpinner.getSelectedItem()
							.toString());
					e.setGroup(groupSpinner.getSelectedItem().toString());
					e.setCommonName(commonName.getText().toString());
					e.setSpecies(species.getText().toString());
					e.setAmount(amount.getText().toString());
					e.setBehavorialDescription(behavorialDescription.getText()
							.toString());
					e.setCounty(countySpinner.getSelectedItem().toString());
					e.setObservationalTechnique(observationalSpinner
							.getSelectedItem().toString());
					e.setObservationalTechniqueOther(observationalTechniqueOther
							.getText().toString());
					e.setEcosystemType(ecosystem.getText().toString());
					e.setAdditionalInformation(additionalInformation.getText()
							.toString());
					e.setPrivacySetting(privacySpinner.getSelectedItem()
							.toString());
					e.setLatitude(latitudeEditText.getText().toString());

					e.setLongitude(longitudeEditText.getText().toString());

					e.setAltitude(altitudeEditText.getText().toString());

					// if the entry is inserted correctly, the method
					// returns a
					// 1
					if (db.updateEntry(e) == 1) {
						message = "Success";
						Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
						clearForm();
						ab.setSelectedNavigationItem(1);

					} else {
						message = "Failure";
						Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
					}

				} else {

					storeInformationInFiledsToEntryObject();

					// if the entry is inserted correctly, the method
					// returns a
					// 1
					if (db.insertEntry(e) == 1) {
						message = "Success";
						Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
						clearForm();
						ab.setSelectedNavigationItem(1);

					} else {
						message = "Failure";
						Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
					}
				}

			}
		});

		// Submit to plone database
		submitButton = (Button) getActivity().findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.d("D", "123456789 STARTING SUBMIT");
				final AQuery aq = new AQuery(getActivity());

				storeInformationInFiledsToEntryObject();

				if (e.getFirstName().equals("")) {
					Toast.makeText(c, "Please enter a first name",
							Toast.LENGTH_SHORT).show();
				} else if (e.getLastName().equals("")) {
					Toast.makeText(c, "Please enter a last name",
							Toast.LENGTH_SHORT).show();
				} else if (e.getEmail().equals("")) {
					Toast.makeText(c, "Please enter an email",
							Toast.LENGTH_SHORT).show();
				} else if (e.getGroup().equals("Choose a group/phyla")) {
					Toast.makeText(c, "Please choose a group/phyla",
							Toast.LENGTH_SHORT).show();
				} else if (e.getAffiliation().equals("Choose an affiliation")) {
					Toast.makeText(c, "Please choose an affiliation",
							Toast.LENGTH_SHORT).show();
				} else if (e.getCounty().equals("Choose a county")) {
					Toast.makeText(c, "Please choose a county",
							Toast.LENGTH_SHORT).show();
				} else if (e.getObservationalTechnique().equals(
						"Choose an observational technique")) {
					Toast.makeText(c,
							"Please choose an observational technique.",
							Toast.LENGTH_SHORT).show();
				} else if (e.getPrivacySetting().equals(
						"Choose a privacy setting")) {
					Toast.makeText(c, "Please choose a privacy setting",
							Toast.LENGTH_SHORT).show();
				} else {
					// Send call to get user information
					final AjaxCallback<String> cb = new AjaxCallback<String>();
					cb.url(URL_FOR_SUBMISSION).type(String.class)
							.weakHandler(this, "submissionCallback");

					cb.header("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
					cb.header("Accept-Language", "en-US,en;q=0.5");
					cb.header("Accept-Encoding", "gzip, deflate");
					cb.header("Host", "plonedev.uwosh.edu");
					cb.header("Referer",
							"https://plonedev.uwosh.edu/zmi41/sites4/Bestiary/bestiary-submissions");
					cb.header("Connection", "keep-alive");

					cb.method(AQuery.METHOD_POST);

					cb.param("first-name", e.getFirstName());
					cb.param("last-name", e.getLastName());
					// cb.param("weather", "");
					cb.param("replyto", e.getEmail());
					cb.param("topic", "Bestiary Submission");
					cb.param("school-affiliation", e.getAffiliation());
					cb.param("animal", e.getGroup());
					cb.param("common-name", e.getCommonName());
					cb.param("species", e.getSpecies());
					cb.param("how-many-of-this-animal-did-you-see",
							e.getAmount());
					cb.param("behavioral-description",
							e.getBehavorialDescription());
					cb.param("county", e.getCounty());
					cb.param("latitude", e.getLatitude());
					// TODO FIX SPELLING ERROR
					cb.param("logitude", e.getLongitude());
					cb.param("altitude", e.getAltitude());
					cb.param("latitude", e.getLatitude());
					cb.param("observation-technique-1",
							e.getObservationalTechnique());
					cb.param("observation-technique",
							e.getObservationalTechniqueOther());
					cb.param("degrees-celcius", e.getTemperature());
					cb.param("wind-speed-mph", e.getWindSpeed());
					cb.param("wind-direction", e.getWindDirection());
					cb.param("pressure-mbar", e.getPressure());
					cb.param("precipitation-inches", e.getPrecipitation());
					cb.param("ecosystem-type", e.getEcosystemType());
					if (e.getPhotoPath() != null) {
						if (!e.getPhotoPath().equals("")
								&& new File(e.getPhotoPath()).exists()) {
							cb.param("image-to-append_file",
									new File(e.getPhotoPath()));
						}
					}

					if (e.getAudioPath() != null) {
						if (!e.getAudioPath().equals("")
								&& new File(e.getAudioPath()).exists()) {
							cb.param("audio_file", new File(e.getAudioPath()));
						}
					}

					cb.param(
							"specific-text-you-would-like-used-to-acknowledge-photograph-interesting-anecdote-submission",
							e.getAdditionalInformation());
					cb.param("agreement:list", "I agree");
					cb.param("app-version", "mobile-android");
					cb.param("device", "mobile-android");
					cb.param("fieldset", "default");
					cb.param("_authenticator",
							"6b9ec1bdad9b1656f6ebf3720017d3c9118ed11f");
					cb.param("form_submit", "Submit");
					Log.d("D", "123456789 RIGHT BEFORE FIRST SUBMIT");
					if (e.getPhotoTime() != null) {
						Log.d("D", "123456789 1");
						if (!e.getPhotoTime().equals("")) {
							Log.d("D", "123456789 2");
							try {
								Log.d("D", "123456789 5");
								String[] photoTime = e.getPhotoTime()
										.split("-");
								cb.param("date-photo-was-taken",
										e.getPhotoTime());
								cb.param("date-photo-was-taken_year",
										photoTime[0]);
								cb.param("date-photo-was-taken_month",
										photoTime[1]);
								cb.param("date-photo-was-taken_day",
										photoTime[2]);



								prgDialog = new ProgressDialog(getActivity());
								prgDialog.setCancelable(false);
								prgDialog.setTitle("Submitting");
								// Display Progress Dialog Bar by invoking
								// progress method
								aq.progress(prgDialog).ajax(cb);

							} catch (Exception e1) {
								Log.d("D", "123456789 6");

								new AlertDialog.Builder(c)
										.setTitle(
												"Missing date for photograph/sighting")
										.setMessage(
												"Click okay to enter a best guess for the day when the photo/sighting occured")
										.setPositiveButton(
												android.R.string.ok,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														final Calendar cal = Calendar
																.getInstance();
														int mYear = cal
																.get(Calendar.YEAR);
														int mMonth = cal
																.get(Calendar.MONTH);
														int mDay = cal
																.get(Calendar.DAY_OF_MONTH);

														DatePickerDialog dpd = new DatePickerDialog(
																c,
																new DatePickerDialog.OnDateSetListener() {

																	@Override
																	public void onDateSet(
																			DatePicker view,
																			int year,
																			int monthOfYear,
																			int dayOfMonth) {
																		e.setPhotoTime(year
																				+ ":"
																				+ (monthOfYear + 1)
																				+ ":"
																				+ dayOfMonth
																				+ ":0:0:0");

																		prgDialog = new ProgressDialog(
																				getActivity());
																		prgDialog
																				.setCancelable(false);
																		prgDialog
																				.setTitle("Submitting");
																		// Display
																		// Progress
																		// Dialog
																		// Bar
																		// by
																		// invoking
																		// progress
																		// method
																		aq.progress(
																				prgDialog)
																				.ajax(cb);
																	}
																}, mYear,
																mMonth, mDay);
														dpd.show();
													}
												})
										.setNegativeButton(
												android.R.string.cancel,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														e.setPhotoTime("Unavailable");
													}
												})
										.setIcon(
												android.R.drawable.ic_dialog_alert)
										.show();
							}

						} else {
							Log.d("D", "123456789 3");

							new AlertDialog.Builder(c)
									.setTitle(
											"Missing date for photograph/sighting")
									.setMessage(
											"Click okay to enter a best guess for the day when the photo/sighting occured")
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													final Calendar cal = Calendar
															.getInstance();
													int mYear = cal
															.get(Calendar.YEAR);
													int mMonth = cal
															.get(Calendar.MONTH);
													int mDay = cal
															.get(Calendar.DAY_OF_MONTH);

													DatePickerDialog dpd = new DatePickerDialog(
															c,
															new DatePickerDialog.OnDateSetListener() {

																@Override
																public void onDateSet(
																		DatePicker view,
																		int year,
																		int monthOfYear,
																		int dayOfMonth) {
																	e.setPhotoTime(year
																			+ ":"
																			+ (monthOfYear + 1)
																			+ ":"
																			+ dayOfMonth
																			+ ":0:0:0");

																	prgDialog = new ProgressDialog(
																			getActivity());
																	prgDialog
																			.setCancelable(false);
																	prgDialog
																			.setTitle("Submitting");
																	// Display
																	// Progress
																	// Dialog
																	// Bar by
																	// invoking
																	// progress
																	// method
																	aq.progress(
																			prgDialog)
																			.ajax(cb);
																}
															}, mYear, mMonth,
															mDay);
													dpd.show();
												}
											})
									.setNegativeButton(
											android.R.string.cancel,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													e.setPhotoTime("Unavailable");

													prgDialog = new ProgressDialog(
															getActivity());
													prgDialog
															.setCancelable(false);
													prgDialog
															.setTitle("Submitting");
													// Display Progress Dialog
													// Bar by invoking progress
													// method
													aq.progress(prgDialog)
															.ajax(cb);
												}
											})
									.setIcon(android.R.drawable.ic_dialog_alert)
									.show();

						}
					} else {
						Log.d("D", "123456789 4");
						new AlertDialog.Builder(c)
								.setTitle(
										"Missing date for photograph/sighting")
								.setMessage(
										"Click okay to enter a best guess for the day when the photo/sighting occured")
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												final Calendar cal = Calendar
														.getInstance();
												int mYear = cal
														.get(Calendar.YEAR);
												int mMonth = cal
														.get(Calendar.MONTH);
												int mDay = cal
														.get(Calendar.DAY_OF_MONTH);

												DatePickerDialog dpd = new DatePickerDialog(
														c,
														new DatePickerDialog.OnDateSetListener() {

															@Override
															public void onDateSet(
																	DatePicker view,
																	int year,
																	int monthOfYear,
																	int dayOfMonth) {
																e.setPhotoTime(year
																		+ ":"
																		+ (monthOfYear + 1)
																		+ ":"
																		+ dayOfMonth
																		+ ":0:0:0");

																prgDialog = new ProgressDialog(
																		getActivity());
																prgDialog
																		.setCancelable(false);
																prgDialog
																		.setTitle("Submitting");
																// Display
																// Progress
																// Dialog Bar by
																// invoking
																// progress
																// method
																aq.progress(
																		prgDialog)
																		.ajax(cb);
															}
														}, mYear, mMonth, mDay);
												dpd.show();
											}
										})
								.setNegativeButton(android.R.string.cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												e.setPhotoTime("Unavailable");

												prgDialog = new ProgressDialog(
														getActivity());
												prgDialog.setCancelable(false);
												prgDialog
														.setTitle("Submitting");
												// Display Progress Dialog Bar
												// by invoking progress method
												aq.progress(prgDialog).ajax(cb);
											}
										})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();

					}

				}

			}

		});

		// Initialize image/video Uri
		oldImageFileUri = null;
		oldVideoFileUri = null;
		imageFileUri = null;
		videoFileUri = null;

	}

	public void submissionCallback(String url, String response,
			AjaxStatus status) {
		Log.d("this is the url", url);

		if (response != null) {
			Log.d("res", response);
			// Remove item from database
			String message;
			if (db.removeEntry(e) == 1) {
				message = "Successfully deleted";
				Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
				clearForm();
				ab.setSelectedNavigationItem(1);

			} else {
				message = "Failure";
				Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
			}

		} else {
			Toast.makeText(
					c,
					"There was an error submitting your sighting. Try again later",
					Toast.LENGTH_LONG).show();
		}
	}

	boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

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

				Matrix matrix = new Matrix();
				try {
					ExifInterface exif = new ExifInterface(
							imageFileUri.getPath());
					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, 1);

					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						matrix.postRotate(90);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						matrix.postRotate(180);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						matrix.postRotate(270);
					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// bimatp factory
				BitmapFactory.Options options = new BitmapFactory.Options();

				// downsizing image as it throws OutOfMemory Exception for
				// larger
				// images
				options.inSampleSize = 8;
				Bitmap bitmap = BitmapFactory.decodeFile(
						imageFileUri.getPath(), options);

				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, true); // rotating bitmap

				capturedPicture.setImageBitmap(bitmap);

				// Save file path to Entry class
				e.setPhotoPath(imageFileUri.getPath());

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else if (requestCode == VIDEO_CAPTURE) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {

				// Delete old recorded video
				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}

				oldVideoFileUri = videoFileUri;
				Uri uri = data.getData();
				videoFileUri = uri;

				// Save file path to Entry class
				e.setVideoPath(videoFileUri.getPath());

			}
		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_VIDEO) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {

				// Delete old recorded video
				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}

				oldVideoFileUri = videoFileUri;
				Uri uri = data.getData();
				videoFileUri = uri;

				// Save file path to Entry class
				e.setVideoPath(videoFileUri.getPath());
				setVideoTime();

			}

		}
		// Pre kitkat
		else if (requestCode == GALLERY_CHOSEN_IMAGE) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				// Store existing imageURI
				oldImageFileUri = imageFileUri;

				// Get image data
				Uri selectedImageURI = data.getData();

				// Get absolute bath
				String pathToSelectedImage = getImagePath(selectedImageURI);

				// Depending on the orientation of the phone when the image was
				// taken, the image can be displayed
				// sideways in the image view. This will rotate the image
				// correctly
				Matrix matrix = new Matrix();
				try {
					ExifInterface exif = new ExifInterface(pathToSelectedImage);
					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, 1);

					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						matrix.postRotate(90);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						matrix.postRotate(180);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						matrix.postRotate(270);
					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

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
				options.inSampleSize = 8;

				Bitmap bitmap = BitmapFactory
						.decodeStream(input, null, options);

				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, true); // rotating bitmap

				// Delete the old image. Don't want to take up too much space.
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}

				// Save file path to Entry class
				e.setPhotoPath(pathToSelectedImage);
				setPhotoTime();

				capturedPicture.setImageBitmap(bitmap);
			}

		}
		// Kitkat video from gallery
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_VIDEO) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				oldVideoFileUri = videoFileUri;
				String path = getVideoPath(data.getData());

				// Save file path to Entry class
				e.setVideoPath(path);
				setVideoTime();

				File externalFile = new File(path);

				Uri videoFile = Uri.fromFile(externalFile);

				videoFileUri = videoFile;

				if (oldVideoFileUri != null) {
					deleteOldFile(oldVideoFileUri.getPath());
				}
			}

		}
		// Kitkat image from gallery
		else if (requestCode == GALLERY_KITKAT_INTENT_CALLED_IMAGE) {
			// Check to make sure the user didn't cancel the image selection
			if (data != null) {
				// Store existing imageURI
				oldImageFileUri = imageFileUri;

				// Get image data
				Uri selectedImageURI = data.getData();

				// Get absolute bath
				String pathToSelectedImage = getImagePath(selectedImageURI);

				// Depending on the orientation of the phone when the image was
				// taken, the image can be displayed
				// sideways in the image view. This will rotate the image
				// correctly
				Matrix matrix = new Matrix();
				try {
					ExifInterface exif = new ExifInterface(pathToSelectedImage);
					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, 1);

					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						matrix.postRotate(90);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						matrix.postRotate(180);
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						matrix.postRotate(270);
					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

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
				options.inSampleSize = 8;

				Bitmap bitmap = BitmapFactory
						.decodeStream(input, null, options);

				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, true); // rotating bitmap

				// Delete the old image. Don't want to take up too much space.
				if (oldImageFileUri != null) {
					deleteOldFile(oldImageFileUri.getPath());
				}

				// Save file path to Entry class
				e.setPhotoPath(pathToSelectedImage);
				setPhotoTime();

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

		// Try and get the location again, if it hasn't already been found
		if (!retrievedLocation) {
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
					|| lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
						0, this);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
						this);
			}
		} else {

			altitudeEditText.setText(e.getAltitude() + "");
			latitudeEditText.setText(e.getLatitude() + "");
			longitudeEditText.setText(e.getLongitude() + "");

		}

		// Check to see if audio was recorded
		if (NewSubmission.e.getAudioPath() != null) {
			audioStatus.setText("Recorded");
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// Store the location
		latitude = arg0.getLatitude();
		longitude = arg0.getLongitude();
		altitude = arg0.getAltitude();

		e.setLongitude(longitude + "");
		e.setLatitude(latitude + "");
		e.setAltitude(altitude + "");

		retrievedLocation = true;

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

				e.setWindDirection(dirTable[(int) (((getDouble("deg", windObj) + 11.25) / 22.5) % 16)]);
				e.setWindSpeed(getDouble("speed", windObj) + "");

				JSONObject mainObj = getObject("main", jObj);
				e.setTemperature((getDouble("temp", mainObj) - 273.15) + "");
				e.setPressure(getDouble("pressure", mainObj) + "");

				// Open weather can return the precipitation measure in either
				// 1h,2h,or 3h. Have to check for all 3
				JSONObject rainObj = getObject("rain", jObj);

				if (rainObj.has("1h")) {
					e.setPrecipitationMeasure("1 hour");
					e.setPrecipitation(getDouble("1h", rainObj) + "");
				} else if (rainObj.has("2h")) {
					e.setPrecipitationMeasure("2 hours");
					e.setPrecipitation(getDouble("2h", rainObj) + "");
				} else if (rainObj.has("3h")) {
					e.setPrecipitationMeasure("3 hours");
					e.setPrecipitation(getDouble("3h", rainObj) + "");
				}

			} catch (JSONException e1) {
				// If it is not raining, no rain measure will be returned. So if
				// we are in this catch block,
				// we will just set a default value for the rain
				e.setPrecipitationMeasure("3 hours");
				e.setPrecipitation("0");
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		private JSONObject getObject(String tagName, JSONObject jObj)
				throws JSONException {
			JSONObject subObj = jObj.getJSONObject(tagName);
			return subObj;
		}

		private double getDouble(String tagName, JSONObject jObj)
				throws JSONException {
			return jObj.getDouble(tagName);
		}

	}

	private void collectLocation() {
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

				e.setLongitude(longitude + "");
				e.setLatitude(latitude + "");
				e.setAltitude(altitude + "");

				retrievedLocation = true;

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
	}

	public void setPhotoTime() {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(e.getPhotoPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (exif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
			e.setPhotoTime(exif.getAttribute(ExifInterface.TAG_DATETIME));
		} else {
			e.setPhotoTime("");
		}

		Log.d("D",
				"SETTING PHOTO TIME "
						+ exif.getAttribute(ExifInterface.TAG_DATETIME));
	}

	public void setVideoTime() {
		MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
		metaRetriver.setDataSource(e.getVideoPath());

		String metadataDate = metaRetriver
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);

		if (metadataDate != null) {
			String year = metadataDate.substring(0, 4);
			String month = metadataDate.substring(4, 6);
			String day = metadataDate.substring(6, 8);

			String hour = metadataDate.substring(9, 11);
			String minute = metadataDate.substring(11, 13);
			String second = metadataDate.substring(13, 15);

			e.setVideoTime(year + ":" + month + ":" + day + " " + hour + ":"
					+ minute + ":" + second);
		}

	}

	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

	public void clearForm() {

		// Not in the existing submission tab
		comingFromExistingSubmission = false;

		// Clear new entry
		e = new Entry();

		// Clear the fields
		// set the check box to false
		sv.fullScroll(ScrollView.FOCUS_UP);

		capturedPicture.setImageDrawable(getResources().getDrawable(
				android.R.drawable.ic_menu_gallery));

		// No need to set anything with the audio. This is handled with the e
		// being set to new entry
		audioStatus.setText("Not Recorded");

		firstName.setText("");
		lastName.setText("");
		email.setText("");

		// set the default value
		affiliationSpinner.setSelection(0);

		// set the default value
		groupSpinner.setSelection(0);

		commonName.setText("");
		species.setText("");
		amount.setText("");
		behavorialDescription.setText("");

		// set the default value
		countySpinner.setSelection(0);

		// set the default value
		observationalSpinner.setSelection(0);

		observationalTechniqueOther.setText("");
		ecosystem.setText("");
		additionalInformation.setText("");

		latitudeEditText.setText("");
		longitudeEditText.setText("");
		altitudeEditText.setText("");

		latitude = 0;
		longitude = 0;
		altitude = 0;

	}

	private void storeInformationInFiledsToEntryObject() {

		// Store all of the information
		e.setFirstName(firstName.getText().toString());
		e.setLastName(lastName.getText().toString());
		e.setEmail(email.getText().toString());
		e.setAffiliation(affiliationSpinner.getSelectedItem().toString());
		e.setGroup(groupSpinner.getSelectedItem().toString());
		e.setCommonName(commonName.getText().toString());
		e.setSpecies(species.getText().toString());
		e.setAmount(amount.getText().toString());
		e.setBehavorialDescription(behavorialDescription.getText().toString());
		e.setCounty(countySpinner.getSelectedItem().toString());
		e.setObservationalTechnique(observationalSpinner.getSelectedItem()
				.toString());
		e.setObservationalTechniqueOther(observationalTechniqueOther.getText()
				.toString());
		e.setEcosystemType(ecosystem.getText().toString());
		e.setAdditionalInformation(additionalInformation.getText().toString());
		e.setPrivacySetting(privacySpinner.getSelectedItem().toString());

		e.setLatitude(latitudeEditText.getText().toString());

		e.setLongitude(longitudeEditText.getText().toString());

		e.setAltitude(altitudeEditText.getText().toString());

	}

}
