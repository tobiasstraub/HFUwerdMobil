package com.HFUwerdMobil.memory;

/**
 * Diese Klasse repräsentiert ein Satz von GPS-Daten die einer bestimmten Route
 * ({@link com.HFUwerdMobil.memory.EntityRoute}) zugeordnert werden.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class EntityGPS {
	/**
	 * GPS ID des Datensatzes
	 */
	private int gps_id;

	/**
	 * Route ID, welche der GPS-Datensatz zugeordnet ist
	 */
	private String route_id;

	/**
	 * Zeitstempel im UNIX Format der Objekt-Konstruktion
	 */
	private long timestamp;

	/**
	 * Latitude des Datensatzes
	 */
	private String lat;

	/**
	 * Longitude des Datensatzes
	 */
	private String lon;

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityGPS} Objekt zusammen. Diese Methode
	 * sollte nur verwendet werden, um Objekte mit den Daten aus der Datenbank
	 * zu konstruieren. Sollen Daten Objekte für die Datenbank konstruiert
	 * werden, sollte der
	 * {@link com.HFUwerdMobil.memory.EntityGPS#EntityGPS(int, String, String)}
	 * Konstruktor verwendet werden.
	 * 
	 * @param gps_id
	 *            Primärschlüssel des GPS-Datensatzes
	 * @param route_id
	 *            In Datenbank vorhandene Route ID, welche der GPS-Datensatz
	 *            zugeordnet wird
	 * @param timestamp
	 *            Zeitstempel der Objekt-Konstruktion im UNIX Format
	 * @param lat
	 *            Latitude
	 * @param lon
	 *            Longitude
	 */
	protected EntityGPS(int gps_id, String route_id, long timestamp, String lat, String lon) {
		this.gps_id = gps_id;
		this.route_id = route_id;
		this.timestamp = timestamp;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityGPS} Objekt zusammen.
	 * 
	 * @param route_id
	 *            In Datenbank vorhandene Route ID, welche der GPS-Datensatz
	 *            zugeordnet wird
	 * @param lat
	 *            Latitude
	 * @param lon
	 *            Longitude
	 */
	public EntityGPS(String route_id, String lat, String lon) {
		this.route_id = route_id;
		this.timestamp = System.currentTimeMillis() / 1000L;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * Gibt die GPS ID des Datensatzes zurück
	 * 
	 * @return GPS ID des Datensatzes
	 */
	public int getGpsId() {
		return gps_id;
	}

	/**
	 * Gibt die Route ID zurück, welche der GPS-Datensatz zugeordnet ist
	 * 
	 * @return Route ID zu der die GPS-Daten zugehörig sind
	 */
	public String getRouteId() {
		return route_id;
	}

	/**
	 * Gibt den Zeitstempel der Objekt-Konstruktion zurück
	 * 
	 * @return UNIX Zeistempel des Datensatzes
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Gibt die Latitude des GPS-Datensatzes zurück
	 * 
	 * @return Latitude des Datensatzes
	 */
	public String getLat() {
		return lat;
	}

	/**
	 * Gibt die Longitude des Datensatzes zurück
	 * 
	 * @return Longitude des Datensatzes
	 */
	public String getLon() {
		return lon;
	}
}
