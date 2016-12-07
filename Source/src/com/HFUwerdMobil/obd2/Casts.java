package com.HFUwerdMobil.obd2;

import android.util.Log;

/**
 * Diese Klasse Castet die Ergebnisse des Bluetoothstreams NOVALUE ist der
 * Rückgabewert bei falschen Eingaben
 * 
 * @author OBD-Gruppe
 * @version 1.0
 */

public class Casts {
	public static String NOVALUE = "NoData";
	private static String speedresponse = "410D";
	private static String rpmresponse = "410C";
	private static String iatresponse = "410F";
	private static String mapresponse = "410B";

	private static final float GASCONST = 8.314f;
	private static final float AIRMASS = 28.97f;
	private static final float VOLUMERATE = 0.8f;
	private static final float ENGINEDISPLACEMENT = 1.6f;

	public static String getString(byte... bs) {
		/** castet ein Byte Array zu einem String */
		String summe = "";
		for (byte b : bs) {
			summe += ((char) ((Byte) b).intValue());
		}
		return summe;
	}

	private static String clearString(String msg) {
		/** löscht alle Sonderzeichen aus einem String */
		msg = msg.trim();
		msg = msg.replaceAll("[ <>\\r\\n]", "");
		return msg;
	}

	public static String getSpeed(String msg) throws NumberFormatException {
		/**
		 * gibt einen String als Speed zurück falls das nicht klappt gibt die
		 * Funktion NOVALUE zurück
		 * */
		Log.e("msgspeed", msg);
		msg = clearString(msg);
		if (msg.contains(speedresponse)) {
			msg = msg.replace(speedresponse, "");
			return Integer.parseInt(msg, 16) + "";
		}
		return NOVALUE;
	}

	public static double getRPM(String msg) throws NumberFormatException {
		/**
		 * gibt einen Double als RMP zurück falls das nicht klappt gibt die
		 * Funktion 0 zurück
		 * */
		Log.e("rpm", msg);
		msg = clearString(msg);
		if (msg.contains(rpmresponse)) {
			msg = msg.replace(rpmresponse, "");
			int A = Integer.parseInt(msg.substring(0, 2), 16);
			int B = Integer.parseInt(msg.substring(2, 4), 16);
			double rpm = ((A * 256) + B) / 100;
			// Log.e("msgrpm", rpm+"");
			return rpm;
		}
		return 1;
	}

	public static double getIAT(String msg) throws NumberFormatException {
		/**
		 * gibt einen Double als RMP zurück falls das nicht klappt gibt die
		 * Funktion 0 zurück
		 * */
		Log.e("iat", msg);
		msg = clearString(msg);
		if (msg.contains(iatresponse)) {
			msg = msg.replace(iatresponse, "");
			double iat = Integer.parseInt(msg, 16) - 40;
			// Log.e("msgiat", iat+"");
			return iat;
		}
		return 1;
	}

	public static double getMAP(String msg) throws NumberFormatException {
		/**
		 * gibt einen Double als RMP zurück falls das nicht klappt gibt die
		 * Funktion 0 zurück
		 * */
		Log.e("map", msg);
		msg = clearString(msg);
		if (msg.contains(mapresponse)) {
			msg = msg.replace(mapresponse, "");
			double map = Integer.parseInt(msg, 16);
			// Log.e("msgmap", map+"");
			return map;
		}
		return 1;
	}

	private static double getMAF(double rpm, double map, double iat) {
		/**
		 * returns MAF (Air Flow Rate) because it is not always supported
		 **/
		double imap = rpm * map / iat / 2;
		double maf = (imap / 60) * VOLUMERATE * ENGINEDISPLACEMENT * (AIRMASS * GASCONST);
		return maf;
	}

	public static double getFuel(double rpm, double map, double iat) {
		/**
		 * returns fuel
		 **/
		double maf = getMAF(rpm, map, iat);
		double gof = (maf / 14.7 / 6.17 / 454);
		double lph = gof * 3.78541178 * 100;
		return lph;
	}
}
