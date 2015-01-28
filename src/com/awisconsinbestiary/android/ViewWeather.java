package com.awisconsinbestiary.android;

import org.json.JSONException;
import org.json.JSONObject;

import com.awisconsinbestiary.android.NewSubmission.AsynchWeather;

import database.Entry;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ViewWeather extends Activity implements LocationListener {

	// UI References
	TextView pressure;
	TextView precipitation;
	TextView precipitationMeasure;
	TextView windSpeed;
	TextView windDegrees;
	TextView temperature;
	Button manualButton;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_weather);
		
		// Store a location listener
		ll = this;

		pressure = (TextView) findViewById(R.id.actualPressure);
		precipitation = (TextView) findViewById(R.id.actualPrecipitation);
		precipitationMeasure = (TextView) findViewById(R.id.precipitation);
		windSpeed = (TextView) findViewById(R.id.actualWindSpeed);
		windDegrees = (TextView) findViewById(R.id.actualWindDirection);
		temperature = (TextView) findViewById(R.id.actualTemperature);
		manualButton = (Button) findViewById(R.id.manual);
		manualButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					getLocation();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

		setTextFieldsWithWeather();

	}

	private void getLocation() throws JSONException {
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				// Store the location
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				altitude = location.getAltitude();
				
				NewSubmission.e.setLongitude(longitude+"");
				NewSubmission.e.setLatitude(latitude+"");
				NewSubmission.e.setAltitude(altitude+"");
				
				//Alert the previous tab that the location was retrieved
				NewSubmission.retrievedLocation = true;

				lm.removeUpdates(ll);

				getWeather();

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
					this);

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
	
	
	private void getWeather()
	{

		String url = "http://api.openweathermap.org/data/2.5/weather?lat="
				+ latitude + "&lon=" + longitude;

		new AsynchWeather().execute(url);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// Store the location
		latitude = arg0.getLatitude();
		longitude = arg0.getLongitude();
		altitude = arg0.getAltitude();
		
		NewSubmission.e.setLongitude(longitude+"");
		NewSubmission.e.setLatitude(latitude+"");
		NewSubmission.e.setAltitude(altitude+"");
		
		//Alert the previous tab that the location was retrieved
		NewSubmission.retrievedLocation = true;

		lm.removeUpdates(ll);

		// Stop location manager
		lm.removeUpdates(this);

		getWeather();
		
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

					NewSubmission.e.setWindDirection(dirTable[ (int) (((getDouble("deg", windObj) + 11.25)/22.5)%16) ]);
					NewSubmission.e.setWindSpeed(getDouble("speed", windObj) + "");

					JSONObject mainObj = getObject("main", jObj);
					NewSubmission.e.setTemperature((getDouble("temp", mainObj) - 273.15) + "");
					NewSubmission.e.setPressure(getDouble("pressure", mainObj) + "");

					// Open weather can return the precipitation measure in either
					// 1h,2h,or 3h. Have to check for all 3
					JSONObject rainObj = getObject("rain", jObj);

					if (rainObj.has("1h")) {
						NewSubmission.e.setPrecipitationMeasure("1 hour");
						NewSubmission.e.setPrecipitation(getDouble("1h", rainObj) + "");
					} else if (rainObj.has("2h")) {
						NewSubmission.e.setPrecipitationMeasure("2 hours");
						NewSubmission.e.setPrecipitation(getDouble("2h", rainObj) + "");
					} else if (rainObj.has("3h")) {
						NewSubmission.e.setPrecipitationMeasure("3 hours");
						NewSubmission.e.setPrecipitation(getDouble("3h", rainObj)+"");
					}
					
					setTextFieldsWithWeather();


				} catch (JSONException e1) {
					//If it is not raining, no rain measure will be returned. So if we are in this catch block,
					//we will just set a default value for the rain
					NewSubmission.e.setPrecipitationMeasure("3 hours");
					NewSubmission.e.setPrecipitation("0");
					
					setTextFieldsWithWeather();
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
		
		

		private void setTextFieldsWithWeather() {
			pressure.setText(NewSubmission.e.getPressure());
			precipitation.setText(NewSubmission.e.getPrecipitation());
			precipitationMeasure.setText("Precipitation in MM per "
					+ NewSubmission.e.getPrecipitationMeasure() + ":");
			windSpeed.setText(NewSubmission.e.getWindSpeed());
			windDegrees.setText(NewSubmission.e.getWindDirection());
			temperature.setText(NewSubmission.e.getTemperature());
			
			
		}
}
