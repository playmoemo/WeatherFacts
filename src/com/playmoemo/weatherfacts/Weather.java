package com.playmoemo.weatherfacts;

/*
 * holder på informasjon om nedlasted vær
 */

public class Weather {
	private int id;
	private long time;
	private String city;
	private double temperature;
	private double farenheit;
	private double kelvin;
	private String readableTime;
	private double latitude, longitude;
	private String icon;
	
	public Weather() {}
	
	public Weather(long timeIn, String city, double temperature) {
		this.time = timeIn;
		this.city = city;
		this.temperature = temperature;
	}
	
	public Weather(String city, double temperature, double farenheit, double kelvin) {
		this.city = city;
		this.temperature = temperature;
		this.farenheit = farenheit;
		this.kelvin = kelvin;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTime() {
		return this.time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temp) {
		this.temperature = temp;
	}
	
	public double getFarenheit() {
		return farenheit;
	}

	public void setFarenheit(double temp) {
		this.farenheit = Math.round((temp * 1.800) + 32);
	}

	public double getKelvin() {
		return kelvin;
	}

	public void setKelvin(double temp) {
		this.kelvin = Math.round(temp + 273.15);
	}
	
	public String getReadableTime() {
		return this.readableTime;
	}
	
	public void setReadableTime(String time) {
		this.readableTime = time;
	}
	
	
	public void setLatitude(double lat) {
		this.latitude = lat;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongitude(double lon) {
		this.longitude = lon;
	}
	
	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return this.readableTime + "\n" + this.city + "\n" + this.temperature;
	}
	
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getIcon() {
		return icon;
	}
	
}
