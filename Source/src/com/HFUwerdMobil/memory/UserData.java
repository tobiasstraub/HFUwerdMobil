package com.HFUwerdMobil.memory;

import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import com.HFUwerdMobil.sync.DataSyncUser;
import com.HFUwerdMobil.util.IssueNotification;

/**
 * Diese Klasse repräsentiert den App Nutzer. Sie enthält alle relevanten
 * Informationen zum Nutzer.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class UserData extends Activity {
	/** {@link android.content.Context} Objekt **/
	private Context context;

	/** Speicher Objekt der User Daten **/
	private SharedPreferences userData;

	/** Speicher Objekt für Setting Flags für die Datensychronisation **/
	private SharedPreferences syncData;

	/** Editor Objekt um die Daten der Speicher Objekte zu bearbeiten **/
	SharedPreferences.Editor editor;

	/**
	 * Thread für die Synchronisation der Userdaten zu den externen Services
	 */
	public Thread userDataSync;

	/**
	 * Öffnet Speicher Objekte
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 */
	public UserData(Context context) {
		this.context = context;
		userData = context.getSharedPreferences("userData", MODE_PRIVATE);
		syncData = context.getSharedPreferences("syncData", MODE_PRIVATE);
	}

	/**
	 * Gibt die im Android Gerät hinterlegte E-Mail Adresse zurück
	 * 
	 * @return Android Google Account E-Mail Adresse, oder null falls keine
	 *         hinterlegt
	 */
	public String getDeviceMail() {
		Pattern emailRegex = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (account.type.equals("com.google") && emailRegex.matcher(account.name).matches()) {
				return account.name;
			}
		}
		return null;
	}

	/**
	 * Gibt die im Android Device hinterlegte mobile Nummer zurück
	 * 
	 * @return Android Device mobile number, oder null falls keine hinterlegt
	 */
	public String getDeviceNumber() {
		TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tMgr.getLine1Number() == "") {
			return null;
		}
		return tMgr.getLine1Number();
	}

	/**
	 * Prüft ob der Nutzer ein HFU-Mitglied ist
	 * 
	 * @return true, wenn der Nutzer ein HFU-Mitglied ist
	 */
	public boolean isHfuMember() {
		return userData.getBoolean("user_hfu_student", false);
	}

	/**
	 * Belegt die Anwender Einstellungen mit Default Werten. Bis auf email und
	 * mobileNumber sind das null-Werte. Die E-Mail und die mobile Nummer sind
	 * Pflichtwerte. Desweiteren wird ein Synchronisation Thread angestoßen,
	 * welcher die Daten an die externen Services überträgt.
	 * 
	 * @param email
	 *            Gültige, verifizierte E-Mail Adresse des Nutzers
	 * @param mobileNumber
	 *            Gültige mobile Nummer des Nutzers
	 */
	public void initUser(String email, String mobileNumber) {
		SharedPreferences.Editor editor = userData.edit();
		editor.putString("user_email", email);
		editor.putString("firstname", null);
		editor.putString("lastname", null);
		editor.putString("mobile_number", mobileNumber);
		editor.putString("hometown_zip", null);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(-1);
		}
	}

	/**
	 * Überprüft, ob der App-Nutzer seine E-Mail Adresse im Google Account
	 * seines Devices verändert hat. Bei einer Änderung der Mail Adresse im
	 * Google Account wird die neue E-Mail Adresse im Speicher Objekt userData
	 * persistiert, sofern der Nutzer der Änderung zustimmt. Wurde der Google
	 * Account nicht gefunden, wird die Mail Adresse nicht geändert. Die E-Mail
	 * wird ebenfalls nicht geändert, wenn der Nutzer die Änderung bereits schon
	 * einmal abgelehnt hat. Desweiteren wird ein Synchronisation Thread
	 * angestoßen, welcher die geänderten Daten an die externen Services
	 * überträgt.
	 */
	public void checkMailChange() {
		String currentMail = userData.getString("user_email", null);
		String newMail = null;
		Pattern emailRegex = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (account.type.equals("com.google") && emailRegex.matcher(account.name).matches()) {
				newMail = account.name;
			}
		}

		if (currentMail != newMail && newMail != null && newMail != userData.getString("old_user_email", null) && !isHfuMember()) {
			SharedPreferences.Editor editor = userData.edit();
			IssueNotification.primaryMailChanged(context, newMail);

			editor.putString("old_user_email", currentMail);

			editor.commit();
			if (isHfuMember()) {
				syncUpdatedData(0);
			}
		}
	}

	/**
	 * Gibt das JSON-Objekt der User Daten zurück
	 * 
	 * @return JSON-Objekt der UserDaten
	 */
	public String getJson() {
		return "user_email\":\"" + getUserMail() + "\",\"firstname\":\"" + getFirstname() + "\",\"lastname\":\"" + getLastname()
				+ "\",\"mobile_number\":\"" + getMobileNumber() + "\",\"hometown_zip\":\"" + getHometownZip() + "\",\"token\":\"";
	}

	/**
	 * Gibt den Vornamen des Nutzers zurück
	 * 
	 * @return Vorname des Nutzers, oder null falls nicht gesetzt
	 */
	public String getFirstname() {
		return userData.getString("firstname", null);
	}

	/**
	 * Setzt den Vornamen des Nutzers und stößt einen Synchronisations Thread
	 * an, um die Änderungen den externen Services mitzuteilen.
	 * 
	 * @param firstname
	 *            Vorname des Nutzers
	 */
	public void setFirstname(String firstname) {
		editor = userData.edit();
		editor.putString("firstname", firstname);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Gibt den Nachnamen des Nutzers zurück
	 * 
	 * @return Nachname des Nutzers, oder null falls nicht gesetzt
	 */
	public String getLastname() {
		return userData.getString("lastname", null);
	}

	/**
	 * Setzt den Nachnamen des Nutzers und stößt einen Synchronisations Thread
	 * an, um die Änderungen den externen Services mitzuteilen.
	 * 
	 * @param lastname
	 *            Nachname des Nutzers
	 */
	public void setLastname(String lastname) {
		editor = userData.edit();
		editor.putString("lastname", lastname);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Gibt die Mobile Nummer des Nutzers zurück
	 * 
	 * @return Mobile Nummer des Nutzers, oder null falls nicht gesetzt
	 */
	public String getMobileNumber() {
		return userData.getString("mobile_number", null);
	}

	/**
	 * Setzt die Mobile Nummer des Nutzers und stößt einen Synchronisations
	 * Thread an, um die Änderungen den externen Services mitzuteilen.
	 * 
	 * @param mobile_number
	 *            Mobile Nummer des Nutzers
	 */
	public void setMobileNumber(String mobile_number) {
		editor = userData.edit();
		editor.putString("mobile_number", mobile_number);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Gibt die Postleitzahl des Heimatorts des Nutzers zurück
	 * 
	 * @return Postleitzahl des Heimatorts des Nutzers, oder null falls nicht
	 *         gesetzt
	 */
	public String getHometownZip() {
		return userData.getString("hometown_zip", null);
	}

	/**
	 * Setzt die Postleitzahl des Heimatorts des Nutzers und stößt einen
	 * Synchronisations Thread an, um die Änderungen den externen Services
	 * mitzuteilen.
	 * 
	 * @param hometownZip
	 *            Postleitzahl des Heimatorts des Nutzers
	 */
	public void setHometownZip(String hometownZip) {
		editor = userData.edit();
		editor.putString("hometown_zip", hometownZip);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Gibt den Namen des Heimatorts des Nutzers zurück
	 * 
	 * @return Namen des Heimatorts des Nutzers, oder null falls nicht gesetzt
	 */
	public String getHometownName() {
		return userData.getString("hometown_name", null);
	}

	/**
	 * Gibt die Longitude des Heimatorts des Nutzers zurück
	 * 
	 * @return Longitude des Heimatorts des Nutzers, oder null falls nicht
	 *         gesetzt
	 */
	public String getHometownLon() {
		return userData.getString("hometown_lon", null);
	}

	/**
	 * Gibt die Langitude des Heimatorts des Nutzers zurück
	 * 
	 * @return Langitude des Heimatorts des Nutzers, oder null falls nicht
	 *         gesetzt
	 */
	public String getHometownLan() {
		return userData.getString("hometown_lan", null);
	}

	/**
	 * Gibt die E-Mail Adresse des Nutzers zurück
	 * 
	 * @return E-Mail Adresse des Nutzers, oder null falls nicht gesetzt
	 */
	public String getUserMail() {
		return userData.getString("user_email", null);
	}

	/**
	 * Setzt die E-Mail Adresse des Nutzers und stößt einen Synchronisations
	 * Thread an, um die Änderungen den externen Services mitzuteilen. Sollte
	 * nur aufgerufen werden, wenn die E-Mail Adresse vom System nicht ermittelt
	 * werden konnte.
	 * 
	 * @param user_email
	 *            E-Mail Adresse des Nutzers
	 */
	public void setUserMail(String user_email) {
		editor = userData.edit();
		editor.putString("user_email", user_email);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Setzt den OBD-Status des Nutzers. Wird dieser Wert auf true gesetzt, so
	 * bedeutet dass, das beim Start einer Routenaufzeichnung der OBD gesucht
	 * wird.
	 * 
	 * @param on
	 *            OBD-Status
	 */
	public void setUsesObd(boolean on) {
		editor = userData.edit();
		editor.putBoolean("uses_obd", on);
		editor.commit();
		if (isHfuMember()) {
			syncUpdatedData(0);
		}
	}

	/**
	 * Gibt den OBD-Status des Nutzers zurück
	 * 
	 * @return true, wenn der Nutzer einen OBD-Adapter verwendet
	 */
	public boolean getUsesObd() {
		return userData.getBoolean("uses_obd", false);
	}

	/**
	 * Setzt das User-Datensynchronisations Flag auf 0 (=Update) und startet die
	 * Synchronisation
	 * 
	 * @param userFlag
	 *            Sync-Bit (0: Update; -1: Initialisierung; 1: kein Sync)
	 */
	private void syncUpdatedData(int userFlag) {
		editor = syncData.edit();
		editor.putInt("user", userFlag);
		editor.commit();
		userDataSync = new Thread(new DataSyncUser(context, getJson()));
		userDataSync.start();
	}

}