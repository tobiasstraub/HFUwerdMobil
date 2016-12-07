package com.HFUwerdMobil.memory;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;

import com.HFUwerdMobil.activity.LoginActivity;

/**
 * Diese Klasse repräsentiert eine Route und beinhaltet grundlegende
 * Informationen zu dieser.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class EntityRoute extends Activity {

	/**
	 * Die interne fortlaufende Route ID
	 */
	private int internal_route_id;

	/**
	 * Die für externe Services bestimmte Route ID
	 */
	private String route_id;

	/**
	 * Zeitstempel im UNIX Format der Objekt-Konstruktion
	 */
	private long timestamp;

	/**
	 * Latitude des Startorts des Datensatzes
	 */
	private String startLat;

	/**
	 * Longitude des Startorts des Datensatzes
	 */
	private String startLon;

	/**
	 * Destination-Flag des Zielort (0: in den App-Settings eingetragener
	 * Heimatort des App-Nutzers; 1: Furtwangen; 2: Schwenningen; 3: Tuttlingen)
	 */
	private int endPoint;

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityRoute} Objekt zusammen. Diese
	 * Methode sollte nur verwendet werden, um Objekte mit den Daten aus der
	 * Datenbank zu konstruieren. Sollen Daten Objekte für die Datenbank
	 * konstruiert werden, sollte der
	 * {@link com.HFUwerdMobil.memory.EntityRoute#EntityRoute(String, String, int, Context)}
	 * Konstruktor verwendet werden.
	 * 
	 * @param internal_route_id
	 *            Interne Route ID
	 * @param timestamp
	 *            Zeitstempel der Objekt-Konstruktion im UNIX Format
	 * @param startLat
	 *            Latitude des Startorts der Route
	 * @param startLon
	 *            Longitude des Startorts der Route
	 * @param endPoint
	 *            Destination-Flag des Zielort (0: in den App-Settings
	 *            eingetragener Heimatort des App-Nutzers; 1: Furtwangen; 2:
	 *            Schwenningen; 3: Tuttlingen)
	 * @param route_id
	 *            Die für externe Services verwendete Route ID, um bspw. auf dem
	 *            Server eine wirklich eindeutige ID zu haben. Da bei jedem
	 *            Nutzer der die App verwendet, intern ab 0 hochgezählt wird,
	 *            würden die externen Services von jedem Nutzer eine Route mit
	 *            der ID 0 erhalten. Um dies zu verhindern, wird für die
	 *            externen Services eine wirkliche eindeutige ID generiert, die
	 *            sich zusammensetzt auf Android Device ID + timestamp. Die
	 *            Zusammensetzung mit zusätzlichem timestamp soll es außerdem
	 *            ermöglichen, dass der gleiche Benutzer die App auf allen
	 *            seinen Android Geräten verwenden kann, und dass ggf. sogar
	 *            (fast) gleichzeitig (timestamp).
	 */
	protected EntityRoute(int internal_route_id, int timestamp, String startLat, String startLon, int endPoint, String route_id) {
		this.internal_route_id = internal_route_id;
		this.timestamp = timestamp;
		this.startLat = startLat;
		this.startLon = startLon;
		this.endPoint = endPoint;
		this.route_id = route_id;
	}

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityRoute} Objekt zusammen.
	 * 
	 * @param startLat
	 *            Latitude des Startorts der Route
	 * @param startLon
	 *            Longitude des Startorts der Route
	 * @param endPoint
	 *            Destination-Flag des Zielorts der Route (0: in den
	 *            App-Settings eingetragener Heimatort des App-Nutzers; 1:
	 *            Furtwangen; 2: Schwenningen; 3: Tuttlingen)
	 * @param context
	 *            Context Objekt
	 */
	public EntityRoute(String startLat, String startLon, int endPoint, Context context) {
		this.route_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) + (System.currentTimeMillis() / 1000L);
		this.timestamp = (System.currentTimeMillis() / 1000L);
		this.startLat = startLat;
		this.startLon = startLon;
		this.endPoint = endPoint;
	}

	/**
	 * Gibt die Route ID des Datensatzes zurück
	 * 
	 * @return Route ID des Datensatzes
	 */
	public String getRouteId() {
		return route_id;
	}

	/**
	 * Gibt die interne ROute ID des Datensatzes zurück
	 * 
	 * @return Interne Route ID des Datensatzes
	 */
	public int getInternalRouteId() {
		return internal_route_id;
	}

	/**
	 * Gibt den Zeitstempel der Objekt-Konstruktion zurück
	 * 
	 * @return UNIX Zeitstempel des Datensatzes
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Gibt die Latitude des Startorts der Route zurück
	 * 
	 * @return Latitude des Startorts der Route
	 */
	public String getStartLat() {
		return startLat;
	}

	/**
	 * Gibt die Longitude des Startorts der Route zurück
	 * 
	 * @return Longitude des Startorts der Route
	 */
	public String getStartLon() {
		return startLon;
	}

	/**
	 * Gibt das Destination-Flag des Zielorts der Route zurück
	 * 
	 * @return Destination-Flag des Zielorts der Route (0: in den App-Settings
	 *         eingetragener Heimatort des App-Nutzers; 1: Furtwangen; 2:
	 *         Schwenningen; 3: Tuttlingen)
	 */
	public int getEndPoint() {
		return endPoint;
	}

	/**
	 * JSON-Objekt, welches beim Start einer Route an die externen Services
	 * übertragen werden
	 * 
	 * @return JSON-Objekt vom Routen Start
	 */
	public String getJsonForRouteStart() {
		return "\"route_id\":\"" + getRouteId() + "\",\"timestamp\":\"" + (System.currentTimeMillis() / 1000L)
				+ "\",\"start_point\":{\"latitude\":\"" + getStartLat() + "\",\"longitude\":\"" + getStartLon() + "\"},\"end_point\":\""
				+ getEndPoint() + "\",\"token\":\"";
	}

	/**
	 * JSON-Objekt, welches beim Beenden der Route an die externen Services
	 * übertragen wird
	 * 
	 * @return JSON-Objekt vom Routen Ende
	 */
	public String getJsonForRouteEnd() {
		DataAccessHandler sHandler = new DataAccessHandler(LoginActivity.getLoginActivityContext());
		return "\"route_id\":\"" + getRouteId() + "\",\"timestamp\":\"" + (System.currentTimeMillis() / 1000L) + "\",\"total_duration_seconds\":\""
				+ xyz() + "\",\"total_kilometers\":\"" + xyz() + "\",\"obd_info\":{\"average_speed\":\""
				+ sHandler.calculateAverageSpeed(getRouteId()) + "\",\"average_consumption\":\"" + sHandler.calculateAverageConsumption(getRouteId())
				+ "\"},\"token\":\"";
	}

	// total_duration_seconds & total_kilometers aus gps daten berechnen
	private String xyz() {
		return "0";
	}
}