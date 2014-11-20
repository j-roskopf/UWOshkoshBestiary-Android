package database;

import java.io.Serializable;

public class Entry implements Serializable {

	private String photoPath;
	private String videoPath;
	private String audioPath;
	private String currentTime;
	private String ID;

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	private String group;
	private String photoTime;

	public String getPhotoTime() {
		return photoTime;
	}

	public void setPhotoTime(String photoTime) {
		this.photoTime = photoTime;
	}

	public String getVideoTime() {
		return videoTime;
	}

	public void setVideoTime(String videoTime) {
		this.videoTime = videoTime;
	}

	private String videoTime;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getAudioPath() {
		return audioPath;
	}

	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBehavorialDescription() {
		return behavorialDescription;
	}

	public void setBehavorialDescription(String behavorialDescription) {
		this.behavorialDescription = behavorialDescription;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getObservationalTechnique() {
		return observationalTechnique;
	}

	public void setObservationalTechnique(String observationalTechnique) {
		this.observationalTechnique = observationalTechnique;
	}

	public String getObservationalTechniqueOther() {
		return observationalTechniqueOther;
	}

	public void setObservationalTechniqueOther(
			String observationalTechniqueOther) {
		this.observationalTechniqueOther = observationalTechniqueOther;
	}

	public String getEcosystemType() {
		return ecosystemType;
	}

	public void setEcosystemType(String ecosystemType) {
		this.ecosystemType = ecosystemType;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getPrivacySetting() {
		return privacySetting;
	}

	public void setPrivacySetting(String privacySetting) {
		this.privacySetting = privacySetting;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
	}

	public String getPrecipitation() {
		return precipitation;
	}

	public void setPrecipitation(String precipitation) {
		this.precipitation = precipitation;
	}

	private String firstName;
	private String lastName;
	private String email;
	private String affiliation;
	private String commonName;
	private String species;
	private String amount;
	private String behavorialDescription;
	private String county;
	private String observationalTechnique;
	private String observationalTechniqueOther;
	private String ecosystemType;
	private String additionalInformation;
	private String latitude;
	private String longitude;
	private String altitude;
	private String privacySetting;
	private String temperature;
	private String windSpeed;
	private String windDirection;
	private String pressure;
	private String precipitation;
	private String precipitationMeasure;
	private boolean usingExistingPhotoOrVideo;


	public boolean getUsingExistingPhotoOrVideo() {
		return usingExistingPhotoOrVideo;
	}


	public void setUsingExistingPhotoOrVideo(boolean usingExistingPhotoOrVideo) {
		this.usingExistingPhotoOrVideo = usingExistingPhotoOrVideo;
	}

	public String getPrecipitationMeasure() {
		return precipitationMeasure;
	}

	public void setPrecipitationMeasure(String precipitationMeasure) {
		this.precipitationMeasure = precipitationMeasure;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

}
