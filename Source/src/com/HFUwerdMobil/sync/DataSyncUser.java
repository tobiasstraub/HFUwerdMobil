package com.HFUwerdMobil.sync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.HFUwerdMobil.memory.UserData;

/**
 * Synchronisiert die User Daten mit externen Services (RESTful API)
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataSyncUser extends Activity implements Runnable {
	/**
	 * Beinhaltet den Endpunkt für Nutzer-Aktionen an oder von der RESTful-API
	 */
	private static final String ENDPOINT_USER = "/user/";

	/**
	 * Beinhaltet die Zeit in Millisekunden, die bei einer fehlerhaften
	 * Übertragung an oder von der RESTful-API gewartet werden soll
	 */
	private static final int BREAK_TIME = 600000; // 10 Minuten

	/** Speicher Objekt für Setting Flags für die Datensychronisation **/
	private SharedPreferences syncData;

	/** Speicher Objekt der User Daten **/
	private SharedPreferences userData;

	/**
	 * {@link com.HFUwerdMobil.sync.DataSyncHandler} Objekt
	 */
	private DataSyncHandler syncHandler;

	/**
	 * Editor Objekt um die Daten der Speicher Objekte zu bearbeiten
	 */
	private SharedPreferences.Editor editor;

	/**
	 * Beinhaltet das JSON-Objekt, ohne führende und endende geschweifte
	 * Klammern
	 */
	private String jsonOut;

	/**
	 * Öffnet die Speicher Objekte, legt den Sync Handler (
	 * {@link com.HFUwerdMobil.sync.DataSyncHandler}) an und legt das
	 * JSON-Objekt fest
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 * @param jsonOut
	 *            JSON-Objekt ohne führende und endende geschweifte Klammern
	 */
	public DataSyncUser(Context context, String jsonOut) {
		syncData = context.getSharedPreferences("syncData", MODE_PRIVATE);
		userData = context.getSharedPreferences("userData", MODE_PRIVATE);
		syncHandler = new DataSyncHandler(new UserData(context).getUserMail());
		this.jsonOut = jsonOut;
	}

	/**
	 * Synchronisiert einen neuen Benutzer oder Änderung der Benutzerdaten.
	 * Meldet die API bei der Änderung der Benutzerdaten, dass der Benutzer auf
	 * dem Server nicht existiert, so wird der Nutzer mit den aktualisierten
	 * Daten an die API übertragen.
	 */
	public void run() {
		int userFlag = syncData.getInt("user", -1);
		if (userFlag == -1) {
			newUser();
			updatePref();
		} else if (userFlag == 0) {
			if (updateUser() == 424) {
				newUser();
			}
			updatePref();
		}
	}

	/**
	 * Schickt einen neuen Benutzer an die RESTful-API, und wartet auf einen
	 * Response Code. Bei fehlgeschlagener Übertragung wird der Request nach
	 * Ablauf einer Unterbrechungszeit erneut gesendet
	 * 
	 * @return Response-Code, oder -1 falls die Synchronisation aufgrund eines
	 *         Abbruch des Threads (bspw. Nutzer beendet App) nicht beendet
	 *         werden konnte.
	 */
	synchronized protected int newUser() {
		int responseCode = syncHandler.sendData(ENDPOINT_USER + "new", "POST", jsonOut);
		while (responseCode != 200 && responseCode != 409) {
			try {
				Thread.sleep(BREAK_TIME);
			} catch (InterruptedException e) {
				return -1;
			}
			responseCode = syncHandler.sendData(ENDPOINT_USER + "new", "POST", jsonOut);
		}
		return responseCode;
	}

	/**
	 * Updated einen Benutzer gegenüber der RESTful-API, und wartet auf einen
	 * Response Code. Bei fehlgeschlagener Übertragung wird der Request nach
	 * Ablauf einer Unterbrechungszeit erneut gesendet
	 * 
	 * @return Response-Code, oder -1 falls die Synchronisation aufgrund eines
	 *         Abbruch des Threads (bspw. Nutzer beendet App) nicht beendet
	 *         werden konnte.
	 */
	private int updateUser() {
		int responseCode = syncHandler.sendData(ENDPOINT_USER + getUserIdentification(), "POST", jsonOut);
		while (responseCode != 200 && responseCode != 424) {
			try {
				Thread.sleep(BREAK_TIME);
			} catch (InterruptedException e) {
				return -1;
			}
			responseCode = syncHandler.sendData(ENDPOINT_USER + getUserIdentification(), "POST", jsonOut);
		}
		return responseCode;
	}

	/**
	 * Updated die Synchronisations-Flags auf "Erfolgreich synchronisiert"
	 */
	private void updatePref() {
		editor = syncData.edit();
		editor.putInt("user", 1);
		editor.commit();
	}

	/**
	 * Gibt die E-Mail Adresse zur Autorisierung des Nutzers bei der RESTful-API
	 * zurück. Hat der Nutzer seine E-Mail Adresse geändert, muss bei der
	 * Änderung gegenüber der RESTful-API einmalig die alte E-Mail zur
	 * Autorisierung mitgegeben werden.
	 * 
	 * @return E-Mail zur Autorisierung der RESTful-API, nach Änderung der
	 *         Benutzerdaten
	 */
	private String getUserIdentification() {
		String userEmail = userData.getString("old_user_email", null);
		if (userEmail != null) {
			editor = userData.edit();
			editor.remove("old_user_email");
			editor.commit();
			return userEmail;
		} else {
			return userData.getString("user_email", null);
		}
	}
}
