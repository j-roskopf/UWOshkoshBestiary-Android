package database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String ID = "id";
	private static final String TABLE_NAME = "entry";
	private static final String PHOTO = "photo";
	private static final String VIDEO = "video";
	private static final String AUDIO = "audio";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";
	private static final String AFFILIATION = "affiliation";
	private static final String GROUP = "groupOrPhyla";
	private static final String COMMON_NAME = "commonName";
	private static final String SPECIES = "species";
	private static final String AMOUNT = "amount";
	private static final String BEHAVORIAL_DESCRIPTION = "behavorialDescription";
	private static final String COUNTY = "county";
	private static final String OBSERVATIONAL_TECHNIQUE = "observationalTechnique";
	private static final String OBSERVATIONAL_TECHNIQUE_OTHER = "observationalTechniqueOther";
	private static final String ECOSYSTEM_TYPE = "ecosystemType";
	private static final String ADDITIONAL_INFORMATION = "additionalInformation";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String ALTITUDE = "altitude";
	private static final String PRIVACY_SETTING = "privacySetting";
	private static final String TEMPERATURE = "temperature";
	private static final String WIND_SPEED = "windSpeed";
	private static final String WIND_DIRECTION = "windDirection";
	private static final String PRESSURE = "pressure";
	private static final String PRECIPITATION = "precipitation";
	private static final String PRECIPITATION_MEASURE = "precipitationMeasure";
	private static final String TIME_PHOTO = "photoTime";
	private static final String TIME_VIDEO = "videoTime";
	private static final String CURRENT_TIME = "currentTime";

	private static final String DATABASE_NAME = "bestiary.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIRST_NAME
			+ " TEXT NOT NULL, " + LAST_NAME + " TEXT NOT NULL, " + EMAIL
			+ " TEXT NOT NULL, " + GROUP + " TEXT NOT NULL, " + PHOTO
			+ " TEXT, " + VIDEO + " TEXT, " + AUDIO + " TEXT, " + AFFILIATION
			+ " TEXT, " + COMMON_NAME + " TEXT, " + SPECIES + " TEXT, "
			+ AMOUNT + " TEXT, " + BEHAVORIAL_DESCRIPTION + " TEXT, "
			+ OBSERVATIONAL_TECHNIQUE + " TEXT, "
			+ OBSERVATIONAL_TECHNIQUE_OTHER + " TEXT, " + COUNTY + " TEXT, "
			+ ECOSYSTEM_TYPE + " TEXT, " + ADDITIONAL_INFORMATION + " TEXT, "
			+ LATITUDE + " TEXT, " + LONGITUDE + " TEXT, " + ALTITUDE
			+ " TEXT, " + PRIVACY_SETTING + " TEXT, " + TEMPERATURE + " TEXT, "
			+ WIND_SPEED + " TEXT, " + WIND_DIRECTION + " TEXT, " + PRESSURE
			+ " TEXT, " + PRECIPITATION + " TEXT, " + PRECIPITATION_MEASURE
			+ " TEXT, " + TIME_PHOTO + " TEXT " + ", " + TIME_VIDEO + " TEXT "
			+ ", " + CURRENT_TIME + " TEXT" +");";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public int insertEntry(Entry e) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(PHOTO, e.getPhotoPath());
		values.put(VIDEO, e.getVideoPath());
		values.put(AUDIO, e.getAudioPath());
		values.put(FIRST_NAME, e.getFirstName());
		values.put(LAST_NAME, e.getLastName());
		values.put(EMAIL, e.getEmail());
		values.put(AFFILIATION, e.getAffiliation());
		values.put(GROUP, e.getGroup());
		values.put(COMMON_NAME, e.getCommonName());
		values.put(SPECIES, e.getSpecies());
		values.put(AMOUNT, e.getAmount());
		values.put(BEHAVORIAL_DESCRIPTION, e.getBehavorialDescription());
		values.put(COUNTY, e.getCounty());
		values.put(OBSERVATIONAL_TECHNIQUE, e.getObservationalTechnique());
		values.put(OBSERVATIONAL_TECHNIQUE_OTHER,
				e.getObservationalTechniqueOther());
		values.put(ECOSYSTEM_TYPE, e.getEcosystemType());
		values.put(ADDITIONAL_INFORMATION, e.getAdditionalInformation());
		values.put(LATITUDE, e.getLatitude());
		values.put(LONGITUDE, e.getLongitude());
		values.put(ALTITUDE, e.getAltitude());
		values.put(PRIVACY_SETTING, e.getPrivacySetting());
		values.put(TEMPERATURE, e.getTemperature());
		values.put(WIND_SPEED, e.getWindSpeed());
		values.put(WIND_DIRECTION, e.getWindDirection());
		values.put(PRESSURE, e.getPressure());
		values.put(PRECIPITATION, e.getPrecipitation());
		values.put(PRECIPITATION_MEASURE, e.getPrecipitationMeasure());
		values.put(TIME_PHOTO, e.getPhotoTime());
		values.put(TIME_VIDEO, e.getVideoTime());
		values.put(CURRENT_TIME,e.getCurrentTime());
		try {
			db.insert(TABLE_NAME, null, values);
		} catch (SQLiteException e1) {
			db.close();
			return -1;
		}

		db.close(); // Closing database connection
		return 1;

	}
	
	public int updateEntry(Entry e) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(PHOTO, e.getPhotoPath());
		values.put(VIDEO, e.getVideoPath());
		values.put(AUDIO, e.getAudioPath());
		values.put(FIRST_NAME, e.getFirstName());
		values.put(LAST_NAME, e.getLastName());
		values.put(EMAIL, e.getEmail());
		values.put(AFFILIATION, e.getAffiliation());
		values.put(GROUP, e.getGroup());
		values.put(COMMON_NAME, e.getCommonName());
		values.put(SPECIES, e.getSpecies());
		values.put(AMOUNT, e.getAmount());
		values.put(BEHAVORIAL_DESCRIPTION, e.getBehavorialDescription());
		values.put(COUNTY, e.getCounty());
		values.put(OBSERVATIONAL_TECHNIQUE, e.getObservationalTechnique());
		values.put(OBSERVATIONAL_TECHNIQUE_OTHER,
				e.getObservationalTechniqueOther());
		values.put(ECOSYSTEM_TYPE, e.getEcosystemType());
		values.put(ADDITIONAL_INFORMATION, e.getAdditionalInformation());
		values.put(LATITUDE, e.getLatitude());
		values.put(LONGITUDE, e.getLongitude());
		values.put(ALTITUDE, e.getAltitude());
		values.put(PRIVACY_SETTING, e.getPrivacySetting());
		values.put(TEMPERATURE, e.getTemperature());
		values.put(WIND_SPEED, e.getWindSpeed());
		values.put(WIND_DIRECTION, e.getWindDirection());
		values.put(PRESSURE, e.getPressure());
		values.put(PRECIPITATION, e.getPrecipitation());
		values.put(PRECIPITATION_MEASURE, e.getPrecipitationMeasure());
		values.put(TIME_PHOTO, e.getPhotoTime());
		values.put(TIME_VIDEO, e.getVideoTime());
		values.put(CURRENT_TIME,e.getCurrentTime());


		try {
			db.update(TABLE_NAME, values, "id " + "= " + e.getID(),null);
		} catch (SQLiteException e1) {
			db.close();
			return -1;
		}

		db.close(); // Closing database connection
		return 1;

	}

	public ArrayList<Entry> getAllEntries() {
		ArrayList<Entry> entries = new ArrayList<Entry>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
		res.moveToFirst();
		Entry e;
		while (res.isAfterLast() == false) {
			e = new Entry();
			e.setID(res.getString(res.getColumnIndex(ID)));
			e.setPhotoPath(res.getString(res.getColumnIndex(PHOTO)));
			e.setVideoPath(res.getString(res.getColumnIndex(VIDEO)));
			e.setAudioPath(res.getString(res.getColumnIndex(AUDIO)));
			e.setFirstName(res.getString(res.getColumnIndex(FIRST_NAME)));
			e.setLastName(res.getString(res.getColumnIndex(LAST_NAME)));
			e.setEmail(res.getString(res.getColumnIndex(EMAIL)));
			e.setAffiliation(res.getString(res.getColumnIndex(AFFILIATION)));
			e.setGroup(res.getString(res.getColumnIndex(GROUP)));
			e.setCommonName(res.getString(res.getColumnIndex(COMMON_NAME)));
			e.setSpecies(res.getString(res.getColumnIndex(SPECIES)));
			e.setAmount(res.getString(res.getColumnIndex(AMOUNT)));
			e.setBehavorialDescription(res.getString(res.getColumnIndex(BEHAVORIAL_DESCRIPTION)));
			e.setCounty(res.getString(res.getColumnIndex(COUNTY)));
			e.setObservationalTechnique(res.getString(res.getColumnIndex(OBSERVATIONAL_TECHNIQUE)));
			e.setObservationalTechniqueOther(res.getString(res.getColumnIndex(OBSERVATIONAL_TECHNIQUE_OTHER)));
			e.setEcosystemType(res.getString(res.getColumnIndex(ECOSYSTEM_TYPE)));
			e.setAdditionalInformation(res.getString(res.getColumnIndex(ADDITIONAL_INFORMATION)));
			e.setLatitude(res.getString(res.getColumnIndex(LATITUDE)));
			e.setLongitude(res.getString(res.getColumnIndex(LONGITUDE)));
			e.setAltitude(res.getString(res.getColumnIndex(ALTITUDE)));
			e.setPrivacySetting(res.getString(res.getColumnIndex(PRIVACY_SETTING)));
			e.setTemperature(res.getString(res.getColumnIndex(TEMPERATURE)));
			e.setWindSpeed(res.getString(res.getColumnIndex(WIND_SPEED)));
			e.setWindDirection(res.getString(res.getColumnIndex(WIND_DIRECTION)));
			e.setPressure(res.getString(res.getColumnIndex(PRESSURE)));
			e.setPrecipitation(res.getString(res.getColumnIndex(PRECIPITATION)));
			e.setPrecipitationMeasure(res.getString(res.getColumnIndex(PRECIPITATION_MEASURE)));
			e.setPhotoTime(res.getString(res.getColumnIndex(TIME_PHOTO)));
			e.setVideoTime(res.getString(res.getColumnIndex(TIME_VIDEO)));
			e.setCurrentTime(res.getString(res.getColumnIndex(CURRENT_TIME)));
			
			entries.add(e);
			res.moveToNext();
		}
		res.close();
		return entries;
	}
	
	public int removeEntry(Entry e) {

		SQLiteDatabase db = this.getWritableDatabase();

		try {
		      db.delete(TABLE_NAME, "id " + "=" + e.getID(), null);
		} catch (SQLiteException e1) {
			db.close();
			return -1;
		}

		db.close(); // Closing database connection
		return 1;

	}
}