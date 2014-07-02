package database;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

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
	public static final String TIME_PHOTO = "timePhoto";

	private static final String DATABASE_NAME = "commments.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIRST_NAME
			+ " TEXT NOT NULL, " + LAST_NAME + " TEXT NOT NULL, " + EMAIL
			+ " TEXT NOT NULL, " + GROUP + " TEXT NOT NULL, " + PHOTO + " TEXT, "
			+ VIDEO + " TEXT, " + AUDIO + " TEXT, " + AFFILIATION + " TEXT, " + COMMON_NAME
			+ " TEXT, " + SPECIES + " TEXT, " + AMOUNT + " TEXT, "
			+ BEHAVORIAL_DESCRIPTION + " TEXT, " + OBSERVATIONAL_TECHNIQUE
			+ " TEXT, " + OBSERVATIONAL_TECHNIQUE_OTHER + " TEXT, " + COUNTY + " TEXT, " 
			+ ECOSYSTEM_TYPE + " TEXT, " + ADDITIONAL_INFORMATION + " TEXT, "
			+ LATITUDE + " TEXT, " + LONGITUDE + " TEXT, " + ALTITUDE + " TEXT, "
			+ PRIVACY_SETTING + " TEXT, " + TEMPERATURE + " TEXT, " + WIND_SPEED
			+ " TEXT, " + WIND_DIRECTION + " TEXT, " + PRESSURE + " TEXT, "
			+ PRECIPITATION + " TEXT, " + PRECIPITATION_MEASURE + " TEXT, "
			+ TIME_PHOTO + " TEXT " + ");";
	
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

	public int insertEntry() {
		
		
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		

		ContentValues values = new ContentValues();
		values.put(PHOTO, Entry.getPhotoPath());
		values.put(VIDEO, Entry.getVideoPath());
		values.put(AUDIO, Entry.getAudioPath());
		values.put(FIRST_NAME, Entry.getFirstName()); 
		values.put(LAST_NAME, Entry.getLastName());
		values.put(EMAIL, Entry.getEmail()); 
		values.put(AFFILIATION, Entry.getAffiliation());
		values.put(GROUP, Entry.getGroup()); 
		values.put(COMMON_NAME, Entry.getCommonName());
		values.put(SPECIES, Entry.getSpecies()); 
		values.put(AMOUNT, Entry.getAmount());
		values.put(BEHAVORIAL_DESCRIPTION, Entry.getBehavorialDescription()); 
		values.put(COUNTY, Entry.getCounty());
		values.put(OBSERVATIONAL_TECHNIQUE, Entry.getObservationalTechnique()); 
		values.put(OBSERVATIONAL_TECHNIQUE_OTHER, Entry.getObservationalTechniqueOther());
		values.put(ECOSYSTEM_TYPE, Entry.getEcosystemType()); 
		values.put(ADDITIONAL_INFORMATION, Entry.getAdditionalInformation());
		values.put(LATITUDE, Entry.getLatitude()); 
		values.put(LONGITUDE, Entry.getLongitude());
		values.put(ALTITUDE, Entry.getAltitude()); 
		values.put(PRIVACY_SETTING, Entry.getPrivacySetting());
		values.put(TEMPERATURE, Entry.getTemperature()); 
		values.put(WIND_SPEED, Entry.getWindSpeed());
		values.put(WIND_DIRECTION, Entry.getWindDirection()); 
		values.put(PRESSURE, Entry.getPressure());
		values.put(PRECIPITATION, Entry.getPrecipitation()); 
		values.put(PRECIPITATION_MEASURE, Entry.getPrecipitationMeasure());
		values.put(TIME_PHOTO, Entry.getTimePhoto()); 

		
		try{
			db.insert(TABLE_NAME, null, values);
		}catch(SQLiteException e)
		{
			db.close();
			return -1;
		}

		db.close(); // Closing database connection
		return 1;

	}
}