package com.HFUwerdMobil.sync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.HFUwerdMobil.memory.DataAccessHandler;
import com.HFUwerdMobil.memory.EntityRoute;
import com.HFUwerdMobil.memory.UserData;

/**
 * Synchronisiert die Routen Daten mit externen Services (RESTful API)
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataSyncRoute extends Activity implements Runnable {
	/**
	 * Beinhaltet den Endpunkt für Nutzer-Aktionen an oder von der RESTful-API
	 */
	private static final String ENDPOINT_USER = "/user/";

	/**
	 * Beinhaltet den Endpunkt für Routen-Aktionen an oder von der RESTful-API
	 */
	private static final String ENDPOINT_ROUTE = "/route/";

	/**
	 * Beinhaltet die Zeit in Millisekunden, die bei einer fehlerhaften
	 * Übertragung an oder von der RESTful-API gewartet werden soll
	 */
	private static final int BREAK_TIME = 120000; // 2 Minuten

	/**
	 * Beinhaltet die Zeit in Millisekunden, die mit der Übertragung an oder von
	 * der RESTful-API gewartet werden soll, wenn der Provider den nächsten
	 * GPS-Datensatz noch nicht erfasst hat
	 */
	private static final int BREAK_TIME_GPS_REQUEST = 4000; // 4 Sekunden

	/**
	 * Synchronisations-Flags
	 */
	private SharedPreferences syncData;

	/**
	 * DataAccessHandler
	 */
	private DataAccessHandler sHandler;

	/**
	 * DataSyncHandler
	 */
	private DataSyncHandler syncHandler;

	/**
	 * Editor, um SharedPreferences zu bearbeiten
	 */
	private SharedPreferences.Editor editor;

	/**
	 * Nutzer-Daten Objekt
	 */
	private UserData userData;

	/**
	 * Sync-User Objekt
	 */
	private DataSyncUser syncUser;

	/**
	 * Öffnet die syncData und syncHandler Objekte
	 * 
	 * @param context
	 *            Context Objekt
	 */
	public DataSyncRoute(Context context) {
		syncData = context.getSharedPreferences("syncData", MODE_PRIVATE);
		syncHandler = new DataSyncHandler(new UserData(context).getUserMail());
	}

	/**
	 * Synchronisiert die Routen Daten mit den externen Services (RESTful API),
	 * dabei werden auch noch-nicht synchronisierte Daten, synchronisiert.
	 */
	@Override
	public void run() {
		int routePointer = syncData.getInt("route_pointer", 1);
		int gpsPointer = syncData.getInt("gps_pointer", 1);
		editor = syncData.edit();

		do {
			if (startRoute(sHandler.getRoute(routePointer)) == 424) {
				syncUser.newUser();
				startRoute(sHandler.getRoute(routePointer));
			}
			while (!Thread.interrupted()) {
				if (sHandler.getLastAddedGpsId() >= gpsPointer) {
					// GPS-Daten senden (siehe Spezifikation)
					// Noch nicht implementiert, da Backend-Gruppe diesen
					// Endpunkt
					// noch nicht realisiert hat
				} else {
					try {
						Thread.sleep(BREAK_TIME_GPS_REQUEST);
					} catch (InterruptedException e) {
						// Keine Behandlung nötig
					}
				}
				editor.putInt("gps_pointer", ++gpsPointer);
				editor.commit();
			}
			if (endRoute(sHandler.getRoute(routePointer)) != 424) {
				editor.putInt("route_pointer", ++routePointer);
				editor.putInt("gps_pointer", 1);
				editor.commit();
			}
		} while (routePointer <= sHandler.getLastAddedRouteId());
		editor.putInt("route", 1);
		editor.commit();
	}

	/**
	 * Überträgt das JSON-Objekt, welches Daten vom Routen Start enthält, an die
	 * externen Services (RESTful API)
	 * 
	 * @param routeData
	 *            Objekt, der aktuellen Route
	 * @return HTTP-Rückgabecode, oder -1 falls der Thread interruptet wurde
	 */
	private int startRoute(EntityRoute routeData) {
		int responseCode = syncHandler.sendData(ENDPOINT_USER + userData.getUserMail() + ENDPOINT_ROUTE + "new", "POST",
				routeData.getJsonForRouteStart());
		while (responseCode != 200 && responseCode != 409 && responseCode != 424) {
			try {
				Thread.sleep(BREAK_TIME);
			} catch (InterruptedException e) {
				return -1;
			}
			responseCode = syncHandler.sendData(ENDPOINT_USER + userData.getUserMail() + ENDPOINT_ROUTE + "new", "POST",
					routeData.getJsonForRouteStart());
		}
		return responseCode;
	}

	/**
	 * Überträgt das JSON-Objekt, welches Daten vom Routen Ende enthält, an die
	 * externen Services (RESTful API)
	 * 
	 * @param routeData
	 *            Objekt, der aktuellen Route
	 * @return HTTP-Rückgabecode, oder -1 falls der Thread interruptet wurde
	 */
	private int endRoute(EntityRoute routeData) {
		int responseCode = syncHandler.sendData(ENDPOINT_USER + userData.getUserMail() + ENDPOINT_ROUTE + "end", "POST",
				routeData.getJsonForRouteEnd());
		while (responseCode != 200 && responseCode != 409 && responseCode != 424) {
			try {
				Thread.sleep(BREAK_TIME);
			} catch (InterruptedException e) {
				return -1;
			}
			responseCode = syncHandler.sendData(ENDPOINT_USER + userData.getUserMail() + ENDPOINT_ROUTE + "end", "POST",
					routeData.getJsonForRouteEnd());
		}
		return responseCode;
	}
}
