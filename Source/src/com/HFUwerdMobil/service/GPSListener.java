package com.HFUwerdMobil.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;

import com.HFUwerdMobil.activity.ChooseRouteActivity;
import com.HFUwerdMobil.activity.RouteDetailsActivity;
import com.HFUwerdMobil.memory.DataAccessHandler;
import com.HFUwerdMobil.memory.EntityGPS;
import com.HFUwerdMobil.memory.EntityRoute;
import com.HFUwerdMobil.sync.DataSyncRoute;
import com.HFUwerdMobil.util.IssueNotification;

/**
 * Diese Klasse trackt die GPS Daten als Service während der Routen
 * Aufzeichnung.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class GPSListener extends Service implements LocationListener {
	/**
	 * Location Provider Objekt
	 */
	private LocationManager lm;

	/**
	 * GPS Provider
	 */
	private String provider;

	/**
	 * DataAccessHandler Objekt
	 */
	private DataAccessHandler sHandler = new DataAccessHandler(this);

	/**
	 * Routen ID
	 */
	private String routeId;

	/**
	 * routeInit=true bedeutet, dass der Listener beim ersten GPS-Datensatz die
	 * Route in der Datenbank persistieren soll
	 */
	private boolean routeInit = true;

	/**
	 * Zielort des Benutzers. Standard: nach Hause (Code 0)
	 */
	private int userDestination = 0; // Standard

	/**
	 * Latitude des Zielorts Furtwangen (Code 1)
	 */
	private final double DES_FURTWANGEN_LAT = 48.0511197;

	/**
	 * Longitude des Zielorts Furtwangen (Code 1)
	 */
	private final double DES_FURTWANGEN_LON = 8.208085299999993;

	/**
	 * Latitude des Zielorts Schwenningen (Code 2)
	 */
	private final double DES_SCHWENNINGEN_LAT = 48.06107739999999;

	/**
	 * Longitude des Zielorts Schwenningen (Code 1)
	 */
	private final double DES_SCHWENNINGEN_LON = 8.53560570000002;

	/**
	 * Latitude des Zielorts Tuttlingen (Code 3)
	 */
	private final double DES_TUTTLINGEN_LAT = 47.9826537;

	/**
	 * Longitude des Zielorts Tuttlingen (Code 3)
	 */
	private final double DES_TUTTLINGEN_LON = 8.820721999999932;

	/**
	 * Thread für die Synchronisation der Routendaten
	 */
	Thread routeThread;

	/**
	 * Editor, um SharedPreferences zu bearbeiten
	 */
	private SharedPreferences.Editor editor;

	/**
	 * Wird beim expliziten Start des Services ausgeführt. Die Datenbank wird
	 * zum Schreiben geöffnet und das anzufahrende Ziel wird ausgelesen
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {
		sHandler.openDB();
		if (intent.getExtras() != null) {
			if (intent.getExtras().containsKey("selected_destination")) {
				userDestination = intent.getExtras().getInt("selected_destination");
			}
		}
		return START_STICKY;
	}

	/**
	 * Wird beim Erzeugen des Services ausgeführt. Es werden Kriterien zur
	 * Auswahl des Location Providers definiert und nach diesen Kriterien einen
	 * Provider gewählt, der nun in definiertem Intervallen
	 * {@link com.HFUwerdMobil.service.GPSListener#onLocationChanged(Location)}
	 * ausführt
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		provider = lm.getBestProvider(criteria, true);
		if (provider == null) {
			IssueNotification.locationProviderNotAvailable(getApplicationContext());
			startActivity(new Intent(getApplicationContext(), ChooseRouteActivity.class));
			this.stopSelf();
		}

		/*
		 * Fragt alle 4 Sekunden die aktuelle GPS Position (Listener) ab, sofern
		 * die GPS Position sich um mindestens 150 Meter verändert hat
		 */
		lm.requestLocationUpdates(provider, 4000, 150, this);
	}

	/**
	 * Startet den Routen Synchronisations Thread und erfasst die aktuellen
	 * GPS-Daten die vom Provider geliefert werden. Prüft weiterhin, ob das Ziel
	 * erreicht wurde und quittiert dieses dem Nutzer mit entsprechender
	 * Meldung.
	 */
	@Override
	public void onLocationChanged(Location location) {
		// Anmerkung: Bitte erst entkommentieren, wenn die Backend-Gruppe,
		// die RESTful API vollständig funktionstüchtig hat und der GPS-JSON
		// eingebaut wurde.
		routeThread = new Thread(new DataSyncRoute(getApplicationContext()));
		// routeThread.start();
		editor = getApplicationContext().getSharedPreferences("syncData", MODE_PRIVATE).edit();
		editor.putInt("route", -1);
		editor.commit();

		double lan = location.getLatitude();
		double lon = location.getLongitude();

		if (routeInit) {
			if (!isDestination(lan, lon)) {
				routeId = sHandler.addRoute(new EntityRoute(Double.toString(lan), Double.toString(lon), userDestination, getApplicationContext()));
				SharedPreferences userData = getApplicationContext().getSharedPreferences("userData", MODE_PRIVATE);
				editor = userData.edit();
				editor.putString("route_id", routeId);
				editor.commit();
				if (routeId == null)
					IssueNotification.fatalError(getApplicationContext());
				routeInit = false;
			} else {
				Intent i = new Intent(getApplicationContext(), RouteDetailsActivity.class);
				i.putExtra("is_destination_yet", true);
				startActivity(i);
				this.stopSelf();
			}
		} else {
			if (sHandler.addGPS(new EntityGPS(routeId, String.valueOf(lan), String.valueOf(lon))) == -1) {
				IssueNotification.fatalError(getApplicationContext());
			}
			if (isDestination(lan, lon)) {
				Intent i = new Intent(getApplicationContext(), RouteDetailsActivity.class);
				i.putExtra("is_destination", true);
				startActivity(i);
				this.stopSelf();
			}
		}
	}

	/**
	 * Prüft ob Nutzer den Zielort erreicht hat
	 * 
	 * @param currentLan
	 *            Langitude der aktuellen Position
	 * @param currentLon
	 *            Longitude der aktuellen Position
	 * @return true, wenn die aktuelle Position der Zielort ist
	 */
	private boolean isDestination(double currentLan, double currentLon) {
		if (userDestination == 0) {

		}
		if (userDestination == 1) {
			if (currentLan == DES_FURTWANGEN_LAT && currentLon == DES_FURTWANGEN_LON)
				return true;
			else
				return false;
		}
		if (userDestination == 2) {
			if (currentLan == DES_SCHWENNINGEN_LAT && currentLon == DES_SCHWENNINGEN_LON)
				return true;
			else
				return false;
		}
		if (userDestination == 3) {
			if (currentLan == DES_TUTTLINGEN_LAT && currentLon == DES_TUTTLINGEN_LON)
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * Wenn der Service zerstört wird, werden die Providerupdates entfernt und
	 * der Routen Synchrisationsthread interrupted. Außerdem wird die Datenbank
	 * geschloßen.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		lm.removeUpdates(this);
		if (routeThread != null) {
			routeThread.interrupt();
		}
		sHandler.closeDB();
	}

	/**
	 * Wird aufgerufen, wenn sich der Status des Location Providers verändert
	 * hat und gibt entsprechende Meldung an den Nutzer aus, wenn der Provider
	 * nicht weiter verfügbar ist.
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			IssueNotification.locationProviderNotAvailable(getApplicationContext());
			startActivity(new Intent(getApplicationContext(), RouteDetailsActivity.class));
			this.stopSelf();
		}
	}

	/**
	 * Wird aufgerufen, wenn ein Location Provider aktiviert wird
	 */
	@Override
	public void onProviderEnabled(String provider) {
	}

	/**
	 * Wird aufgerufen, wenn der Benutzer den Location Provider deaktiviert hat
	 * und gibt somit entsprechende Meldung an den Nutzer aus und beendet
	 * außerdem die Routenaufzeichnung
	 */
	@Override
	public void onProviderDisabled(String provider) {
		IssueNotification.locationProviderNotAvailable(getApplicationContext());
		startActivity(new Intent(getApplicationContext(), RouteDetailsActivity.class));
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
