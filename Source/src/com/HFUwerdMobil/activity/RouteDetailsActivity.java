package com.HFUwerdMobil.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.DataAccessHandler;
import com.HFUwerdMobil.memory.EntityGPS;
import com.HFUwerdMobil.util.Geocoding;
import com.HFUwerdMobil.util.IssueNotification;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Diese Klasse handelt die Routen Details Activity, welche alle Details zu
 * einer bereits gefahrenen Route visualisiert.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class RouteDetailsActivity extends Activity {
	/**
	 * DataAccessHandler Objekt
	 */
	private DataAccessHandler sHandler;

	/**
	 * Externe Route ID
	 */
	private String routeId;

	/**
	 * Liste mit den GPS Daten zu einer Route
	 */
	List<EntityGPS> gpsData;

	/**
	 * Erzeugt die Activity. Prüft die Intents und gibt entsprechende Meldungen
	 * aus, wenn der User das Ziel gerade erreicht hat oder bereits seit Beginn
	 * am Ziel war. Prüft ob eine Route gestartet wurde und Initialwerte vom GPS
	 * Provider erhalten hat. Und ob erste Wegpunkte erfasst wurden. Wenn das
	 * der Fall ist, werden die Routendaten ausgegeben, und die Google Maps
	 * Karte ausgegeben. Wenn das nicht der Fall ist, wird sofern die Route mit
	 * Initialwerten besteht, aus der Datenbank gelöscht (da sich das Auto nicht
	 * vom Ort bewegt hat und deshalb eine Route auch nicht existent ist), und
	 * der User wird wieder zur
	 * {@link com.HFUwerdMobil.activity.ChooseRouteActivity} weitergeleitet.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_details);
		SharedPreferences userData = getApplicationContext().getSharedPreferences("userData", MODE_PRIVATE);

		if (getIntent().getExtras() != null) {
			if (getIntent().getExtras().getBoolean("is_destination_yet") != true) {
				Toast.makeText(this, getResources().getString(R.string.is_destination_yet), Toast.LENGTH_LONG).show();
			}

			if (getIntent().getExtras().getBoolean("is_destination") == true) {
				Toast.makeText(this, getResources().getString(R.string.is_destination), Toast.LENGTH_LONG).show();
			}
		}

		if (userData.getString("route_id", null) != null) {
			sHandler = new DataAccessHandler(this);
			sHandler.openDB();
			routeId = userData.getString("route_id", null);
			SharedPreferences.Editor editor = userData.edit();
			editor.putString("route_id", null);
			editor.commit();

			sHandler.getRouteGps(routeId);
			gpsData = sHandler.getRouteGps(routeId);

			if (gpsData != null) {
				((TextView) findViewById(R.id.start_destination)).setText(printStartDestination());

				String timeDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(sHandler.getRoute(routeId).getTimestamp() * 1000L);
				((TextView) findViewById(R.id.route_details_date)).setText(timeDate);

				drawMap();
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
				startActivity(new Intent(getApplicationContext(), ChooseRouteActivity.class));
				finish();
				// Es macht Sinn, nachdem keine GPS-Daten erfasst wurden, die
				// Route hier wieder zu löschen. Außer es soll für statistische
				// Zwecke verwendet werden.
			}
			sHandler.closeDB();
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
			startActivity(new Intent(getApplicationContext(), ChooseRouteActivity.class));
			finish();
		}
	}

	/**
	 * Kodiert aus den Koordinaten und dem Destination Flag einen Ort
	 * 
	 * @return Ortskodierter Start- und Zielort als String im Format
	 *         "{startort} - {zielort}"
	 */
	private String printStartDestination() {
		String startPointString = "";
		try {
			startPointString = new Geocoding(sHandler.getRoute(routeId).getStartLat(), sHandler.getRoute(routeId).getStartLon()).execute().get();
		} catch (InterruptedException e) {
			IssueNotification.fatalError(this);
		} catch (ExecutionException e) {
			IssueNotification.fatalError(this);
		}

		String endPointString = "";
		switch (sHandler.getRoute(routeId).getEndPoint()) {
		case 0:
			endPointString = getResources().getString(R.string.home);
			break;
		case 1:
			endPointString = getResources().getString(R.string.furtwangen);
			break;
		case 2:
			endPointString = getResources().getString(R.string.schwenningen);
			break;
		case 3:
			endPointString = getResources().getString(R.string.tuttlingen);
			break;
		}
		return startPointString + " - " + endPointString;
	}

	/**
	 * Zeichnet die erfassten GPS-Daten zur Route in die Google Maps Karte und
	 * gibt die OBD-Daten aus (sofern erfasst)
	 */
	private void drawMap() {
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		ArrayList<EntityGPS> markerList = new ArrayList<EntityGPS>();
		markerList.addAll(gpsData);
		PolylineOptions line = new PolylineOptions();
		line.width(5);
		line.color(Color.RED);

		for (EntityGPS gps : markerList) {
			line.add(new LatLng(Double.parseDouble(gps.getLat()), Double.parseDouble(gps.getLon())));
		}
		map.addPolyline(line);
		EntityGPS gps = markerList.get(0);
		map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(gps.getLat()), Double.parseDouble(gps.getLon()))));
		gps = markerList.get(markerList.size() - 1);
		map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(gps.getLat()), Double.parseDouble(gps.getLon()))));

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(Double.parseDouble(gps.getLat()), Double.parseDouble(gps.getLon()))).zoom(12).tilt(40).build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		String averageSpeed = sHandler.calculateAverageSpeed(routeId);
		if (averageSpeed != null) {
			((TextView) findViewById(R.id.average_speed)).setText(" " + averageSpeed + " km/h");
		}

		String averageConsumption = sHandler.calculateAverageConsumption(routeId);
		if (averageConsumption != null) {
			((TextView) findViewById(R.id.average_consumption)).setText(" " + averageConsumption + " l");
		}
	}
}
