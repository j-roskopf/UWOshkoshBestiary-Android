package com.example.uwoshkoshbestiary;

import database.Entry;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewWeather extends Activity {
	
	//UI References
	TextView pressure;
	TextView precipitation;
	TextView precipitationMeasure;
	TextView windSpeed;
	TextView windDegrees;
	TextView temperature;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_weather);
		
		pressure = (TextView) findViewById(R.id.actualPressure);
		precipitation = (TextView) findViewById(R.id.actualPrecipitation);
		precipitationMeasure = (TextView) findViewById(R.id.precipitation);
		windSpeed = (TextView) findViewById(R.id.actualWindSpeed);
		windDegrees = (TextView) findViewById(R.id.actualWindDirection);
		temperature = (TextView) findViewById(R.id.actualTemperature);
		
		pressure.setText(Entry.getPressure());
		precipitation.setText(Entry.getPrecipitation());
		precipitationMeasure.setText("Precipitation in MM per "+Entry.getPrecipitationMeasure()+":");
		windSpeed.setText(Entry.getWindSpeed());
		windDegrees.setText(Entry.getWindDirection());
		temperature.setText(Entry.getTemperature());


		
	}
}
