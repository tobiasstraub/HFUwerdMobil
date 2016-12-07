package com.HFUwerdMobil.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

/**
 * Diese Klasse codiert Postleitzahlen oder GPS Daten (Latitude, Longitude) zu
 * Ortsnamen
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class Geocoding extends AsyncTask<Void, Void, String> {
	/**
	 * Geocoding API URI
	 */
	String uri = null;

	/**
	 * Generiert die API URI, von der die kodierten Daten gelesen werden sollen
	 * 
	 * @param lat
	 *            Latitude
	 * @param lng
	 *            Longitude
	 */
	public Geocoding(String lat, String lng) {
		this.uri = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=false";
	}

	/**
	 * Generiert die API URI, von der die kodierten Daten gelesen werden sollen
	 * 
	 * @param zipCode
	 *            Postleitzahl
	 */
	public Geocoding(String zipCode) {
		this.uri = "http://maps.googleapis.com/maps/api/geocode/json?address=" + zipCode + "&region=de&sensor=false";
	}

	/**
	 * Liest anhand der generierten URI, das augerufene Dokument aus und erhält
	 * somit den kodierten Ortsnamen und gibt diesen zurück an den Task Aufrufer
	 */
	@Override
	protected String doInBackground(Void... params) {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			InputStream stream = new DefaultHttpClient().execute(new HttpGet(uri)).getEntity().getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
			JSONObject jsonObject = new JSONObject(stringBuilder.toString());
			try {
				int i = 0;
				do {
					if (jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(i).getString("types")
							.equals("[\"locality\",\"political\"]")) {
						return new String(jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(i)
								.getString("long_name").getBytes("ISO-8859-1"), "UTF-8");
					}
					i++;
				} while (true);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
