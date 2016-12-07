package com.HFUwerdMobil.memory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.HFUwerdMobil.util.IssueNotification;

/**
 * Diese Klasse handelt den Datenzugriff zur und von der internen App-Datenbank.
 * Sie stellt dabei entsprechende Methoden zur Verfügung, die es erleichtern,
 * auf bestimmte Daten zuzugreifen, sowie bestimmte Daten in die Datenbank
 * einzutragen.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataAccessHandler extends Activity {
	/** {@link android.database.sqlite.SQLiteDatabase} Objekt **/
	private SQLiteDatabase db;

	/** {@link com.HFUwerdMobil.memory.DataAccessHelper} Objekt **/
	private DataAccessHelper sHelper;

	/** {@link android.content.Context} Objekt **/
	private Context context;

	/**
	 * Erzeugt ein Hilfsobjekt für Datenbank Operationen
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt zur Erstellung des
	 *            Hilfsobjekt
	 * @see com.HFUwerdMobil.memory.DataAccessHelper
	 */
	public DataAccessHandler(Context context) {
		sHelper = new DataAccessHelper(context);
	}

	/**
	 * Öffnet die App-Datenbank, oder erzeugt diese mit Hilfe
	 * {@link com.HFUwerdMobil.memory.DataAccessHelper#onCreate(SQLiteDatabase)}
	 * , sollte die App-Datenbank nicht bestehen oder führt bei Abweichung der
	 * {@link com.HFUwerdMobil.memory.DataAccessHelper#DATABASE_VERSION} ein
	 * Upgrade mit Hilfe
	 * {@link com.HFUwerdMobil.memory.DataAccessHelper#onUpgrade(SQLiteDatabase, int, int)}
	 * durch bzw. ein Downgrade mit Hilfe
	 * {@link com.HFUwerdMobil.memory.DataAccessHelper#onDowngrade(SQLiteDatabase, int, int)}
	 * . Kann die Datenbank nicht zum Schreiben geöffnet werden, wird dem
	 * Anwender das mitgeteilt und die App beendet.
	 * 
	 */
	public void openDB() {
		try {
			db = sHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			IssueNotification.fatalError(context);
		}
	}

	/**
	 * Schließt die Datenbank
	 */
	public void closeDB() {
		sHelper.close();
	}

	/**
	 * Speichert die Daten des übergebenen
	 * {@link com.HFUwerdMobil.memory.EntityRoute} Objekt in der internen
	 * App-Datenbank
	 * 
	 * @param route
	 *            {@link com.HFUwerdMobil.memory.EntityRoute} Objekt
	 * @return ID der neu zugefügten Route in der App-Datenbank, oder null falls
	 *         die Route nicht eingetragen werden konnte
	 */
	public String addRoute(EntityRoute route) {
		ContentValues values = new ContentValues();
		values.put(DataAccessHelper.ROUTES_CID, route.getRouteId());
		values.put(DataAccessHelper.ROUTES_C1, route.getTimestamp());
		values.put(DataAccessHelper.ROUTES_C2, route.getStartLat());
		values.put(DataAccessHelper.ROUTES_C3, route.getStartLon());
		values.put(DataAccessHelper.ROUTES_C4, route.getEndPoint());
		if (db.insert(DataAccessHelper.TABLE_ROUTES, null, values) != -1) {
			return route.getRouteId();
		}
		return null;
	}

	/**
	 * Liefert das {@link com.HFUwerdMobil.memory.EntityRoute} Objekt zur
	 * gegeben internal_route_id aus der App-Datenbank
	 * 
	 * @param internal_route_id
	 *            ID der Route, die aus der Datenbank gelesen werden soll
	 * @return {@link com.HFUwerdMobil.memory.EntityRoute} Objekt der gegebenen
	 *         internal_route_id, oder null falls internal_route_id nicht
	 *         gefunden wurde
	 */
	public EntityRoute getRoute(int internal_route_id) {
		Cursor cursor = db.query(DataAccessHelper.TABLE_ROUTES, new String[] { DataAccessHelper.ROUTES_C5, DataAccessHelper.ROUTES_C1,
				DataAccessHelper.ROUTES_C2, DataAccessHelper.ROUTES_C3, DataAccessHelper.ROUTES_C4, DataAccessHelper.ROUTES_CID },
				DataAccessHelper.ROUTES_C5 + "=?", new String[] { String.valueOf(internal_route_id) }, null, null, null, null);
		if (cursor.moveToFirst()) {
			return new EntityRoute(internal_route_id, Integer.parseInt(cursor.getString(1)), cursor.getString(2), cursor.getString(3),
					Integer.parseInt(cursor.getString(4)), cursor.getString(5));
		}
		return null;
	}

	/**
	 * Liefert das {@link com.HFUwerdMobil.memory.EntityRoute} Objekt zur
	 * gegeben route_id aus der App-Datenbank
	 * 
	 * @param route_id
	 *            ID der Route, die aus der Datenbank gelesen werden soll
	 * @return {@link com.HFUwerdMobil.memory.EntityRoute} Objekt der gegebenen
	 *         route_id, oder null falls route_id nicht gefunden wurde
	 */
	public EntityRoute getRoute(String route_id) {
		Cursor cursor = db.query(DataAccessHelper.TABLE_ROUTES, new String[] { DataAccessHelper.ROUTES_C5, DataAccessHelper.ROUTES_C1,
				DataAccessHelper.ROUTES_C2, DataAccessHelper.ROUTES_C3, DataAccessHelper.ROUTES_C4, DataAccessHelper.ROUTES_CID },
				DataAccessHelper.ROUTES_CID + "=?", new String[] { route_id }, null, null, null, null);
		if (cursor.moveToFirst()) {
			return new EntityRoute(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), cursor.getString(2),
					cursor.getString(3), Integer.parseInt(cursor.getString(4)), route_id);
		}
		return null;
	}

	/**
	 * Liefert alle {@link com.HFUwerdMobil.memory.EntityRoute} Objekte aus der
	 * App-Datenbank
	 * 
	 * @return Liste mit allen Routen Objekten vom Typ
	 *         {@link com.HFUwerdMobil.memory.EntityRoute}, oder null falls
	 *         keine Route gefunden wurde
	 */
	public List<EntityRoute> getAllRoutes() {
		List<EntityRoute> routeList = new ArrayList<EntityRoute>();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataAccessHelper.TABLE_ROUTES, null);
		if (cursor.moveToFirst()) {
			do {
				EntityRoute route = new EntityRoute(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)),
						cursor.getString(2), cursor.getString(3), Integer.parseInt(cursor.getString(4)), cursor.getString(5));
				routeList.add(route);
			} while (cursor.moveToNext());
			return routeList;
		}
		return null;
	}

	/**
	 * Liefert die interne ID der letzten in der App-Datenbank eingetragenen
	 * Route zurück
	 * 
	 * @return Letzte in der Datenbank eingetragene Route ID, oder null falls
	 *         keine Route verfügbar ist
	 */
	public int getLastAddedRouteId() {
		Cursor cursor = db.rawQuery("SELECT " + DataAccessHelper.ROUTES_C5 + " FROM " + DataAccessHelper.TABLE_ROUTES + " ORDER BY "
				+ DataAccessHelper.ROUTES_C5 + " DESC LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return Integer.parseInt(cursor.getString(0));
		}
		return -1;
	}

	/**
	 * Speichert die Daten des übergebenen
	 * {@link com.HFUwerdMobil.memory.EntityGPS} Objekt in der internen
	 * App-Datenbank
	 * 
	 * @param gps
	 *            {@link com.HFUwerdMobil.memory.EntityGPS} Objekt
	 * @return ID des neu zugefügten GPS-Satzes in der App-Datenbank, oder -1
	 *         falls der GPS-Satz nicht eingetragen werden konnte
	 */
	public long addGPS(EntityGPS gps) {
		ContentValues values = new ContentValues();
		values.put(DataAccessHelper.GPS_C1, gps.getRouteId());
		values.put(DataAccessHelper.GPS_C2, gps.getTimestamp());
		values.put(DataAccessHelper.GPS_C3, gps.getLat());
		values.put(DataAccessHelper.GPS_C4, gps.getLon());
		return db.insert(DataAccessHelper.TABLE_GPS, null, values);
	}

	/**
	 * Speichert die Daten des übergebenen
	 * {@link com.HFUwerdMobil.memory.EntityOBD} Objekt in der internen
	 * App-Datenbank
	 * 
	 * @param obd
	 *            {@link com.HFUwerdMobil.memory.EntityOBD} Objekt
	 * @return ID des neu zugefügten OBD-Datensatzes in der App-Datenbank, oder
	 *         -1 falls der OBD-Datensatz nicht eingetragen werden konnte
	 */
	public long addOBD(EntityOBD obd) {
		ContentValues values = new ContentValues();
		values.put(DataAccessHelper.OBD_C1, obd.getRouteId());
		values.put(DataAccessHelper.OBD_C2, obd.getCurrentSpeed());
		values.put(DataAccessHelper.OBD_C3, obd.getCurrentConsumption());
		return db.insert(DataAccessHelper.TABLE_OBD, null, values);
	}

	/**
	 * Liefert alle {@link com.HFUwerdMobil.memory.EntityGPS} Objekte zur
	 * gegeben route_id aus der App-Datenbank aus der App-Datenbank
	 * 
	 * @return Liste mit GPS Objekten vom Typ
	 *         {@link com.HFUwerdMobil.memory.EntityGPS}, oder null falls keine
	 *         GPS-Daten zur Route gefunden wurden
	 */
	public List<EntityGPS> getRouteGps(String route_id) {
		List<EntityGPS> routeGpsList = new ArrayList<EntityGPS>();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataAccessHelper.TABLE_GPS + " WHERE " + DataAccessHelper.GPS_C1 + " = '" + route_id + "'",
				null);
		if (cursor.moveToFirst()) {
			do {
				EntityGPS gps = new EntityGPS(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)),
						cursor.getString(3), cursor.getString(4));
				routeGpsList.add(gps);
			} while (cursor.moveToNext());
			return routeGpsList;
		}
		return null;
	}

	/**
	 * Liefert die ID des letzten in der App-Datenbank eingetragenen
	 * GPS-Datensatzes zurück
	 * 
	 * @return Letzte in der Datenbank eingetragene GPS ID, oder -1 falls kein
	 *         GPS-Datensatz verfügbar ist
	 */
	public int getLastAddedGpsId() {
		Cursor cursor = db.rawQuery("SELECT " + DataAccessHelper.GPS_CID + " FROM " + DataAccessHelper.TABLE_GPS + " ORDER BY "
				+ DataAccessHelper.GPS_CID + " DESC LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return Integer.parseInt(cursor.getString(0));
		}
		return -1;
	}

	/**
	 * Liefert alle {@link com.HFUwerdMobil.memory.EntityOBD} Objekte aus zur
	 * gegeben route_id aus der App-Datenbank aus der App-Datenbank
	 * 
	 * @return ArrayList mit Objekten vom Typ
	 *         {@link com.HFUwerdMobil.memory.EntityOBD}, oder null falls keine
	 *         OBD-Daten gefunden wurden
	 */
	public List<EntityOBD> getRouteOBD(String route_id) {
		List<EntityOBD> routeOBDlist = new ArrayList<EntityOBD>();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataAccessHelper.TABLE_OBD + " WHERE " + DataAccessHelper.OBD_C1 + " = '" + route_id + "'",
				null);
		if (cursor.moveToFirst()) {
			do {
				EntityOBD obd = new EntityOBD(Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)), Integer.parseInt(cursor
						.getString(3)));
				routeOBDlist.add(obd);
			} while (cursor.moveToNext());
			return routeOBDlist;
		}
		return null;
	}

	/**
	 * Liefert die ID des letzten in der App-Datenbank eingetragenen
	 * OBD-Datensatzes zurück
	 * 
	 * @return Letzte in der Datenbank eingetragene OBD ID, oder -1 falls kein
	 *         OBD-Datensatz verfügbar ist
	 */
	public int getLastAddedObdId() {
		Cursor cursor = db.rawQuery("SELECT " + DataAccessHelper.OBD_CID + " FROM " + DataAccessHelper.TABLE_OBD + " ORDER BY "
				+ DataAccessHelper.GPS_CID + " DESC LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return Integer.parseInt(cursor.getString(0));
		}
		return -1;
	}

	/**
	 * Berechnet die durchschnittliche Geschwindigkeit aus den aktuellen
	 * Geschwindigkeitsdaten die vom OBD2 erfasst wurden
	 * 
	 * @return Aktuelle Geschwindigkeit oder null, falls keine OBD Daten erfasst
	 *         wurden
	 */
	public String calculateAverageSpeed(String routeId) {
		List<EntityOBD> obdData = getRouteOBD(routeId);
		if (obdData != null) {
			int averageSpeed = 0;
			for (int i = 0; i < obdData.size(); i++) {
				averageSpeed = averageSpeed + obdData.get(i).getCurrentSpeed();
			}
			return Integer.toString(averageSpeed / obdData.size());
		}
		return null;
	}

	/**
	 * Berechnet den durchschnittlichen Verbrauch aus den aktuellen
	 * Verbrauchsdaten die vom OBD2 erfasst wurden
	 * 
	 * @return Aktueller Verbrauch oder null, falls keine OBD Daten erfasst
	 *         wurden
	 */
	public String calculateAverageConsumption(String routeId) {
		List<EntityOBD> obdData = getRouteOBD(routeId);
		if (obdData != null) {
			int averageConsumption = 0;
			for (int i = 0; i < obdData.size(); i++) {
				averageConsumption = averageConsumption + obdData.get(i).getCurrentConsumption();
			}
			return Integer.toString(averageConsumption / obdData.size());
		}
		return null;
	}

}
