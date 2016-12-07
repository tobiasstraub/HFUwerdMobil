package com.HFUwerdMobil.memory;

/**
 * Diese Klasse repräsentiert ein Satz von OBD-Daten die einer bestimmten Route
 * ({@link com.HFUwerdMobil.memory.EntityRoute}) zugeordnert werden.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class EntityOBD {
	/**
	 * OBD ID des Datensatzes
	 */
	private int obd_id;

	/**
	 * Route ID, welche der OBD-Datensatz zugeordnet ist
	 */
	private int route_id;

	/**
	 * Aktuelle Geschwindigkeit des OBD-Datensatzes
	 */
	private int current_speed;

	/**
	 * Aktueller Verbrauch des OBD-Datensatzes
	 */
	private int current_consumption;

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityOBD} Objekt zusammen. Diese Methode
	 * sollte nur verwendet werden, um Objekte mit den Daten aus der Datenbank
	 * zu konstruieren. Sollen Daten Objekte für die Datenbank konstruiert
	 * werden, sollte der
	 * {@link com.HFUwerdMobil.memory.EntityOBD#EntityOBD(int, int, int)}
	 * Konstruktor verwendet werden.
	 * 
	 * @param obd_id
	 *            Primärschlüssel des OBD-Datensatzes
	 * @param route_id
	 *            In Datenbank vorhandene Route ID, welche der OBD-Datensatz
	 *            zugeordnet wird
	 * @param current_speed
	 *            Aktuelle Geschwindigkeit
	 * @param current_consumption
	 *            Aktueller Verbrauch
	 */
	protected EntityOBD(int obd_id, int route_id, int current_speed, int current_consumption) {
		this.obd_id = obd_id;
		this.route_id = route_id;
		this.current_speed = current_speed;
		this.current_consumption = current_consumption;
	}

	/**
	 * Setzt mit den übergebenen Daten das
	 * {@link com.HFUwerdMobil.memory.EntityOBD} Objekt zusammen.
	 * 
	 * @param route_id
	 *            In Datenbank vorhandene Route ID, welche der OBD-Datensatz
	 *            zugeordnet wird
	 * @param current_speed
	 *            Aktuelle Geschwindigkeit
	 * @param current_consumption
	 *            Aktueller Verbrauch
	 */
	public EntityOBD(int route_id, int current_speed, int current_consumption) {
		this.route_id = route_id;
		this.current_speed = current_speed;
		this.current_consumption = current_consumption;
	}

	/**
	 * Gibt die OBD ID des Datensatzes zurück
	 * 
	 * @return OBD ID des Datensatzes
	 */
	public int getObdId() {
		return obd_id;
	}

	/**
	 * Gibt die Route ID zurück, welche der OBD-Datensatz zugeordnet ist
	 * 
	 * @return Route ID zu der die OBS-Daten zugehörig sind
	 */
	public int getRouteId() {
		return route_id;
	}

	/**
	 * Gibt die aktuelle Geschwindigkeit des OBD-Datensatzes zurück
	 * 
	 * @return Aktuelle Geschwindigkeit des Datensatzes
	 */
	public int getCurrentSpeed() {
		return current_speed;
	}

	/**
	 * Gibt den aktuellen Verbrauch des OBD-Datensatzes zurück
	 * 
	 * @return Aktueller Verbrauch des Datensatzes
	 */
	public int getCurrentConsumption() {
		return current_consumption;
	}
}