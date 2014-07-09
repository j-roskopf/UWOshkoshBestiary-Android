package com.example.uwoshkoshbestiary;

import java.util.ArrayList;
import java.util.HashMap;

import database.DatabaseHelper;
import database.Entry;
import adapter.Adapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.os.Build;

public class ExistingSubmission extends Fragment {

	private static View view;

	//Helper class for interacting with the DB
	DatabaseHelper db;
	
	//Used to manually switch to the new submission tab when the user clicks on an entry
	android.app.ActionBar ab;

	//Listview list/adapter
	ListView list;
	Adapter adapter;

	//List of the entries
	ArrayList<Entry> allEntresFromDatabase;
	
	//Hashmap of all the entires with each key value pair pairing with an entry's information
	ArrayList<HashMap<String, String>> entries;

	public static final String ID = "id";
	public static final String TABLE_NAME = "entry";
	public static final String PHOTO = "photo";
	public static final String VIDEO = "video";
	public static final String AUDIO = "audio";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String EMAIL = "email";
	public static final String AFFILIATION = "affiliation";
	public static final String GROUP = "groupOrPhyla";
	public static final String COMMON_NAME = "commonName";
	public static final String SPECIES = "species";
	public static final String AMOUNT = "amount";
	public static final String BEHAVORIAL_DESCRIPTION = "behavorialDescription";
	public static final String COUNTY = "county";
	public static final String OBSERVATIONAL_TECHNIQUE = "observationalTechnique";
	public static final String OBSERVATIONAL_TECHNIQUE_OTHER = "observationalTechniqueOther";
	public static final String ECOSYSTEM_TYPE = "ecosystemType";
	public static final String ADDITIONAL_INFORMATION = "additionalInformation";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String PRIVACY_SETTING = "privacySetting";
	public static final String TEMPERATURE = "temperature";
	public static final String WIND_SPEED = "windSpeed";
	public static final String WIND_DIRECTION = "windDirection";
	public static final String PRESSURE = "pressure";
	public static final String PRECIPITATION = "precipitation";
	public static final String PRECIPITATION_MEASURE = "precipitationMeasure";
	public static final String TIME_PHOTO = "photoTime";
	public static final String TIME_VIDEO = "videoTime";

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
			view = inflater.inflate(R.layout.fragment_existing_submission,
					container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}

		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ab = getActivity().getActionBar();

		db = new DatabaseHelper(getActivity());
		
		entries = new ArrayList<HashMap<String, String>>();
		
		allEntresFromDatabase = new ArrayList<Entry>();

		
		


	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    if (isVisibleToUser)
	    {
	    	allEntresFromDatabase.clear();
	    	entries.clear();
	    	pullDataFromDatabase();
			adapter.notifyDataSetChanged();
	    }

	}
	
	public void pullDataFromDatabase()
	{


		allEntresFromDatabase.addAll(db.getAllEntries());

		for (int i = 0; i < allEntresFromDatabase.size(); i++) {
			// creating new HashMap
			HashMap<String, String> map = new HashMap<String, String>();

			// adding each child node to HashMap key => value
			map.put(ID, allEntresFromDatabase.get(i).getID());
			map.put(PHOTO, allEntresFromDatabase.get(i).getPhotoPath());
			map.put(VIDEO, allEntresFromDatabase.get(i).getVideoPath());
			map.put(AUDIO, allEntresFromDatabase.get(i).getAudioPath());
			map.put(FIRST_NAME, allEntresFromDatabase.get(i).getFirstName());
			map.put(LAST_NAME, allEntresFromDatabase.get(i).getLastName());
			map.put(EMAIL, allEntresFromDatabase.get(i).getEmail());
			map.put(AFFILIATION, allEntresFromDatabase.get(i).getAffiliation());
			map.put(GROUP, allEntresFromDatabase.get(i).getGroup());
			map.put(COMMON_NAME, allEntresFromDatabase.get(i).getCommonName());
			map.put(SPECIES, allEntresFromDatabase.get(i).getSpecies());
			map.put(AMOUNT, allEntresFromDatabase.get(i).getAmount());
			map.put(BEHAVORIAL_DESCRIPTION, allEntresFromDatabase.get(i)
					.getBehavorialDescription());
			map.put(COUNTY, allEntresFromDatabase.get(i).getCounty());
			map.put(OBSERVATIONAL_TECHNIQUE, allEntresFromDatabase.get(i)
					.getObservationalTechnique());
			map.put(OBSERVATIONAL_TECHNIQUE_OTHER, allEntresFromDatabase.get(i)
					.getObservationalTechniqueOther());
			map.put(ECOSYSTEM_TYPE, allEntresFromDatabase.get(i)
					.getEcosystemType());
			map.put(ADDITIONAL_INFORMATION, allEntresFromDatabase.get(i)
					.getAdditionalInformation());
			map.put(LATITUDE, allEntresFromDatabase.get(i).getLatitude());
			map.put(LONGITUDE, allEntresFromDatabase.get(i).getLongitude());
			map.put(ALTITUDE, allEntresFromDatabase.get(i).getAltitude());
			map.put(PRIVACY_SETTING, allEntresFromDatabase.get(i)
					.getPrivacySetting());
			map.put(TEMPERATURE, allEntresFromDatabase.get(i).getTemperature());
			map.put(WIND_SPEED, allEntresFromDatabase.get(i).getWindSpeed());
			map.put(WIND_DIRECTION, allEntresFromDatabase.get(i)
					.getWindDirection());
			map.put(PRESSURE, allEntresFromDatabase.get(i).getPressure());
			map.put(PRECIPITATION, allEntresFromDatabase.get(i)
					.getPrecipitation());
			map.put(PRECIPITATION_MEASURE, allEntresFromDatabase.get(i)
					.getPrecipitationMeasure());
			map.put(TIME_PHOTO, allEntresFromDatabase.get(i).getPhotoTime());
			map.put(TIME_VIDEO, allEntresFromDatabase.get(i).getVideoTime());
			map.put(ID,allEntresFromDatabase.get(i).getID());

			// adding HashList to ArrayList
			entries.add(map);
		}

		list = (ListView) getActivity().findViewById(R.id.listview);

		if (list == null) {
			Log.d("it's null", "yup");
		}

		// Getting adapter by passing xml data ArrayList
		if (adapter == null) {
			adapter = new Adapter(getActivity(), entries);
			
		}
		list.setAdapter(adapter);

		// Click event for single list row
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NewSubmission.comingFromExistingSubmission = true;
				NewSubmission.e = allEntresFromDatabase.get(position);
				ab.setSelectedNavigationItem(0);

			}
		});
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

}
