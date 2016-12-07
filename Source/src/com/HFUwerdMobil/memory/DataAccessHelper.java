package com.HFUwerdMobil.memory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Diese Klasse ist Hilfsklasse der Klasse
 * {@link com.HFUwerdMobil.memory.DataAccessHandler} und definiert die Struktur
 * für die interne SQLite Datenbank zur Speicherung App interner Daten. Sie
 * stellt weiterhin Methoden durch die Unterstützung der Klasse
 * {@link android.database.sqlite.SQLiteOpenHelper} zur Erzeugung sowie zum
 * Updaten der Datenbank bereit.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataAccessHelper extends SQLiteOpenHelper {

	/**
	 * Name der internen Datenbank
	 **/
	protected static final String DATABASE_NAME = "hfuwerdmobil.db";

	/**
	 * Version der internen SQLite Datenbank. Eine Änderung der Version hat zur
	 * Folge, dass die Methode {@link #onUpgrade(SQLiteDatabase, int, int)}
	 * aufgerufen wird, sofern die SQLite Datenbank auf dem auszuführenden
	 * Device besteht und eine hier nicht identische Datenbank Version enthält.
	 **/
	// ACHTUNG: Bevor die Versionsnummer geändert wird, bitte Hinweise bei
	// onUpgrade bzw. bei onDowngrade beachten!
	protected static final int DATABASE_VERSION = 1;

	/**
	 * Name der Tabelle "Routes" für die interne SQLite Datenbank.
	 */
	protected static final String TABLE_ROUTES = "routes";

	/**
	 * Spaltenname des Primärschlüssels der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_C5 = "internal_route_id";

	/**
	 * Spaltenname 1 der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_C1 = "timestamp";

	/**
	 * Spaltenname 2 der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_C2 = "start_lat";

	/**
	 * Spaltenname 3 der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_C3 = "start_lon";

	/**
	 * Spaltenname 4 der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_C4 = "end_point";

	/**
	 * Spaltenname 5 der Tabelle {@link #TABLE_ROUTES}
	 */
	protected static final String ROUTES_CID = "route_id";

	/**
	 * CREATE-Statement für die Tabelle {@link #TABLE_ROUTES}.
	 */
	protected static final String ROUTES_CREATE_STATEMENT = "CREATE TABLE " + TABLE_ROUTES + "(" + ROUTES_C5 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ ROUTES_C1 + " INTEGER NOT NULL, " + ROUTES_C2 + " TEXT NOT NULL, " + ROUTES_C3 + " TEXT NOT NULL, " + ROUTES_C4 + " INTEGER NOT NULL, "
			+ ROUTES_CID + " TEXT);";

	/**
	 * Name der Tabelle "GPS" für die interne SQLite Datenbank.
	 */
	protected static final String TABLE_GPS = "gps";

	/**
	 * Spaltenname des Primärschlüssels der Tabelle {@link #TABLE_GPS}
	 */
	protected static final String GPS_CID = "gps_id";

	/**
	 * Spaltenname 1 der Tabelle {@link #TABLE_GPS}
	 */
	protected static final String GPS_C1 = "route_id";

	/**
	 * Spaltenname 2 der Tabelle {@link #TABLE_GPS}
	 */
	protected static final String GPS_C2 = "timestamp";

	/**
	 * Spaltenname 3 der Tabelle {@link #TABLE_GPS}
	 */
	protected static final String GPS_C3 = "lat";

	/**
	 * Spaltenname 4 der Tabelle {@link #TABLE_GPS}
	 */
	protected static final String GPS_C4 = "lon";

	/**
	 * CREATE-Statement für die Tabelle {@link #TABLE_GPS}.
	 */
	protected static final String GPS_CREATE_STATEMENT = "CREATE TABLE " + TABLE_GPS + "(" + GPS_CID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GPS_C1 + " TEXT NOT NULL, " + GPS_C2 + " INTEGER NOT NULL, " + GPS_C3 + " TEXT NOT NULL, " + GPS_C4 + " TEXT NOT NULL);";

	/**
	 * Name der Tabelle "OBD" für die interne SQLite Datenbank.
	 */
	protected static final String TABLE_OBD = "obd";

	/**
	 * Spaltenname des Primärschlüssels der Tabelle {@link #TABLE_OBD}
	 */
	protected static final String OBD_CID = "obd_id";

	/**
	 * Spaltenname 1 der Tabelle {@link #TABLE_OBD}
	 */
	protected static final String OBD_C1 = "route_id";

	/**
	 * Spaltenname 1 der Tabelle {@link #TABLE_OBD}
	 */
	protected static final String OBD_C2 = "current_speed";

	/**
	 * Spaltenname 2 der Tabelle {@link #TABLE_OBD}
	 */
	protected static final String OBD_C3 = "current_consumption";

	/**
	 * CREATE-Statement für die Tabelle {@link #TABLE_OBD}.
	 */
	protected static final String OBD_CREATE_STATEMENT = "CREATE TABLE " + TABLE_OBD + "(" + OBD_CID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ OBD_C1 + " TEXT NOT NULL, " + OBD_C2 + " INTEGER NOT NULL, " + OBD_C3 + " INTEGER NOT NULL);";

	/**
	 * Erstellt ein Hilfsobjekt um Datenbenken erstellen, öffnen und managen zu
	 * können
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt zur Erstellung des
	 *            Hilfsobjekt
	 * @see android.database.sqlite.SQLiteOpenHelper#SQLiteOpenHelper(Context,
	 *      String, android.database.sqlite.SQLiteDatabase.CursorFactory, int)
	 */
	protected DataAccessHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Erstellt die Datenbank und führt die in {@link #ROUTES_CREATE_STATEMENT}
	 * und {@link #GPS_CREATE_STATEMENT} definierten CREATE-Statements aus,
	 * sofern die Datenbank auf dem Device nicht schon besteht
	 * 
	 * @param db
	 *            Datenbank Objekt
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ROUTES_CREATE_STATEMENT);
		db.execSQL(GPS_CREATE_STATEMENT);
		db.execSQL(OBD_CREATE_STATEMENT);
	}

	/**
	 * Sofern die Datenbank Version auf dem Device älter ist als die in
	 * {@link #DATABASE_VERSION} definierte Version, werden die Tabellen
	 * entfernt und neu angelegt. Daten werden dabei NICHT migriert.
	 * 
	 * @param db
	 *            Datenbank Objekt
	 * @param oldVersion
	 *            Die alte Datenbank Version
	 * @param newVersion
	 *            Die neue Datenbank Version
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// ACHTUNG: Wenn die Versionsnummer geupdatet wird, sollten die Daten
		// des aktuellen Versionsschematas sorgfältig migriert werden und das am
		// besten in einem eigenem Thread (da das viel Zeit kostet)
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBD);
		onCreate(db);
	}

	/**
	 * Sofern die Datenbank Version auf dem Device neuer ist als die in
	 * {@link #DATABASE_VERSION} definierte Version, werden die Tabellen
	 * entfernt und neu angelegt. Daten werden dabei NICHT migriert.
	 * 
	 * @param db
	 *            Datenbank Objekt
	 * @param oldVersion
	 *            Die alte Datenbank Version
	 * @param newVersion
	 *            Die neue Datenbank Version
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// ACHTUNG: Wenn die Versionsnummer gedowngradet wird, sollten die Daten
		// des aktuellen Versionsschematas sorgfältig migriert werden und das am
		// besten in einem eigenem Thread (da das viel Zeit kostet)
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBD);
		onCreate(db);
	}

}
