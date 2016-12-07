package com.HFUwerdMobil.sync;

import android.app.Activity;
import android.content.Context;

import com.HFUwerdMobil.memory.UserData;

/**
 * Fordert für eine unverifizierte E-Mail Adresse bei den externen Services
 * (RESTful API) eine Verifizeriungscode an
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataSyncVerifycode extends Activity implements Runnable {
	/**
	 * Unverifizierte E-Mail Adresse des Benutzers
	 */
	private String unverifiedEmail;

	/**
	 * Aktuell hinterlegte E-Mail Adresse des Benutzers
	 */
	private String userEmail;

	/**
	 * Von der API zurückgegebener Verifizierungscode
	 */
	public static String[] VERIFYCODE_RESPONSE = null;

	/**
	 * Speichert die unverifizierte E-Mail Adresse des Benutzers und das Context
	 * Objekt zwischen
	 * 
	 * @param unverifiedEmail
	 *            Unverifizierte E-Mail Adresse des Benutzers
	 * @param context
	 *            Context Objekt
	 */
	public DataSyncVerifycode(String unverifiedEmail, Context context) {
		this.unverifiedEmail = unverifiedEmail;
		this.userEmail = new UserData(context).getUserMail();
	}

	/**
	 * Schickt eine zu verifizierende E-Mail Adresse an die RESTful-API, und
	 * wartet auf einen Response Code und den zugehörigen Verifizierungscode.
	 * 
	 * @return String Array mit Response Code (Index 0) und Verifizierungscode
	 *         (Index 1)
	 */
	@Override
	public void run() {
		VERIFYCODE_RESPONSE = new DataSyncHandler(userEmail).receiveData("/verify/" + unverifiedEmail);
	}
}
