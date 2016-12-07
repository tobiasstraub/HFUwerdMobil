package com.HFUwerdMobil.util;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.HFUwerdMobil.main.R;

/**
 * Diese Klasse stellt Methoden bereit, um dem Nutzer Informationen über
 * Probleme und schwerwiegende Fehler in der App zu informieren.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class IssueNotification extends DialogFragment {
	/** {@link android.content.Context} Objekt **/
	static Context context;

	/** Neue E-Mail Adresse des Google Konto (Primärkonto) des Android Device **/
	static String email;

	/**
	 * Informiert den Nutzer per Dialog, dass ein schwerwiegender Fehler
	 * aufgetreten ist. Beim Bestätigen des Dialoges wird der
	 * Applikations-Prozess gekillt.
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 */
	public static void fatalError(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_fatalError_title);
		builder.setMessage(R.string.dialog_fatalError_message);
		builder.setNegativeButton(R.string.dialog_fatalError_terminate, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				// Kill App
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die E-Mail Adresse im Primärkonto
	 * des Android Device geändert wurde. Weiterhin soll der Nutzer angeben, ob
	 * die geänderte E-Mail Adresse als neue user_email gesetzt werden soll oder
	 * ob die alte beibehalten werden soll.
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 * @param email
	 *            Die neue E-Mail Adresse des Android Device Primärkonto
	 */
	public static void primaryMailChanged(Context context, String email) {
		IssueNotification.context = context;
		IssueNotification.email = email;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_primaryMailChanged_title);
		builder.setMessage(R.string.dialog_primaryMailChanged_message);
		builder.setPositiveButton(R.string.dialog_primaryMailChanged_yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				SharedPreferences userData = IssueNotification.context.getSharedPreferences("userData", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = userData.edit();
				editor.putString("user_email", IssueNotification.email);
				editor.commit();
				IssueNotification.primaryMailChangedSuccessfully();
			}
		});
		builder.setNegativeButton(R.string.dialog_primaryMailChanged_no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				SharedPreferences userData = IssueNotification.context.getSharedPreferences("userData", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = userData.edit();
				editor.putString("user_email_lastQueried", IssueNotification.email);
				editor.commit();
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass der eingegebene Verifizierungscode
	 * nicht korrekt ist
	 * 
	 * @param context
	 *            Context Objekt
	 */
	public static void verifycodeInputFailed(Context context) {
		IssueNotification.context = context;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_verifycodeInputFailed_title);
		builder.setMessage(R.string.dialog_verifycodeInputFailed_message);
		builder.setPositiveButton(R.string.dialog_verifycodeInputFailed_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die die App-E-Mail-Adresse durch
	 * die E-Mail Adresse des Primärkontos des Android Device substituiert
	 * wurde.
	 */
	private static void primaryMailChangedSuccessfully() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_primaryMailChangedSuccessfully_title);
		builder.setMessage(R.string.dialog_primaryMailChangedSuccessfully_message);
		builder.setPositiveButton(R.string.dialog_primaryMailChangedSuccessfully_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die Route nicht weiter
	 * aufgezeichnet wird.
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 */
	public static void locationProviderNotAvailable(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_locationProviderNotAvailable_title);
		builder.setMessage(R.string.dialog_locationProviderNotAvailable_message);
		builder.setNegativeButton(R.string.dialog_locationProviderNotAvailable_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die Verbindung zum Netzwerk
	 * fehlerhaft ist.
	 * 
	 * @param context
	 *            Context Objekt
	 */
	public static void networkFailed(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_networkFailed_title);
		builder.setMessage(R.string.dialog_networkFailed_message);
		builder.setNegativeButton(R.string.dialog_networkFailed_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				// Kill App
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die vom Nutzer eingegebene E-Mail
	 * nicht korrekt ist.
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 */
	public static void emailNotValid(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_emailNotValid_title);
		builder.setMessage(R.string.dialog_emailNotValid_message);
		builder.setNegativeButton(R.string.dialog_emailNotValid_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/**
	 * Informiert den Nutzer per Dialog, dass die vom Nutzer eingegebene
	 * Handynummer nicht korrekt ist.
	 * 
	 * @param context
	 *            {@link android.content.Context} Objekt
	 */
	public static void deviceNumberNotValid(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_deviceNumberNotValid_title);
		builder.setMessage(R.string.dialog_deviceNumberNotValid_message);
		builder.setNegativeButton(R.string.dialog_deviceNumberNotValid_okay, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}
}
