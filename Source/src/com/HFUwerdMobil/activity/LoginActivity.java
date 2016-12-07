package com.HFUwerdMobil.activity;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.UserData;
import com.HFUwerdMobil.sync.DataSyncRoute;
import com.HFUwerdMobil.sync.DataSyncUser;
import com.HFUwerdMobil.util.IssueNotification;

/**
 * Diese Klasse handelt die Login Activity, die bei jedem Start der App
 * aufgerufen wird.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class LoginActivity extends Activity {
	/**
	 * {@link com.HFUwerdMobil.memory.UserData} Objekt
	 */
	private UserData userData;

	/**
	 * Context Objekt, welches vom Syncronisationsthread benötigt wird, um die
	 * Systemservices nutzen zu können
	 */
	private static Context context;

	/** Speicher Objekt für Setting Flags für die Datensychronisation **/
	private SharedPreferences syncData;

	/**
	 * Thread für die Synchronisation der Userdaten zu den externen Services
	 */
	public Thread userDataSync;

	/**
	 * Thread für die Synchronisation der Routendaten
	 */
	Thread routeThread;

	/**
	 * Erzeugt die Activity. Führt ggf. noch ausstehende Synchronisationen aus.
	 * Prüft ob es sich um den ersten oder nicht ersten Login handelt und führt
	 * jeweils weitere Aktionen durch.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userData = new UserData(this);

		context = this;
		syncData = this.getSharedPreferences("syncData", MODE_PRIVATE);

		if (userData.isHfuMember()) {
			checkSync();
		}

		if (userData.getUserMail() != null) {
			notFirstLogin();
		} else {
			findViewById(R.id.btn_hfu_student).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					isHfuStudent(v);
				}
			});

			findViewById(R.id.btn_no_hfu_student).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					isNotHfuStudent(v);
				}
			});
		}
	}

	/**
	 * Prüft alle Synchronisationsflags, um ggf. noch ausstehende
	 * Synchronisationen durchzuführen, die beim letzten Durchlauf der App nicht
	 * abgeschlossen werden konnten, z.B. durch Beenden der App.
	 */
	private void checkSync() {
		if (syncData.getInt("user", -1) != 1) {
			userDataSync = new Thread(new DataSyncUser(context, userData.getJson()));
			userDataSync.start();
		}
		if (syncData.getInt("route", -1) != 1) {
			routeThread = new Thread(new DataSyncRoute(getApplicationContext()));
			// routeThread.start();
			// Anmerkung: Bitte erst entkommentieren, wenn die Backend-Gruppe,
			// die RESTful API vollständig funktionstüchtig hat und der GPS-JSON
			// eingebaut wurde.
		}
	}

	/**
	 * Blendet alle UI-Elemente aus, die für Nutzer die bereits "registriert"
	 * sind, unrelevant sind (Registrierungsbuttons). Prüft ob sich die E-Mail
	 * Adresse des Google Kontos des Nutzers geändert hat, und führ ggf. weitere
	 * Aktionen durch (
	 * {@link com.HFUwerdMobil.memory.UserData#checkMailChange()}), sofern der
	 * Nutzer nicht HFU Student ist (Prinzip: HFU Nutzer haben mehr Rechte, ein
	 * Downgrade auf eine E-Mail Adresse, die nicht der HFU angehört, soll
	 * verhindert werden). Gibt einen Text aus, der den User Willkommen zurück
	 * heißt und leitet ihn direkt zur
	 * {@link com.HFUwerdMobil.activity.ChooseRouteActivity} weiter.
	 */
	private void notFirstLogin() {
		findViewById(R.id.btn_no_hfu_student).setVisibility(View.GONE);
		findViewById(R.id.btn_hfu_student).setVisibility(View.GONE);
		TextView welcomeBack = (TextView) findViewById(R.id.text_welcome_back);
		welcomeBack.append(userData.getUserMail() + ".\nSchön dich wieder hier zu sehen.");
		findViewById(R.id.text_welcome_back).setVisibility(View.VISIBLE);
		if (!userData.isHfuMember()) {
			userData.checkMailChange();
		}
		startActivity(new Intent(getApplicationContext(), ChooseRouteActivity.class));
	}

	/**
	 * Blendet den Registrierungsbutton aus und blendet ein Eingabefeld ein, bei
	 * dem der Nutzer aufgefordert wird seine HFU E-Mail Adresse einzugeben, um
	 * die Registrierung abzuschließen. Dabei wird "@hs-furtwangen.de" fest
	 * codiert. Die E-Mail Adresse wird auf Syntax-Richtigkeit geprüft. Sofern
	 * die E-Mail valide ist, wird er zur
	 * {@link com.HFUwerdMobil.activity.UserVerifyActivity} weitergeleitet.
	 * 
	 * @param v
	 *            UI-Element, welches dieses Ereignis ausgelöst hat
	 */
	private void isHfuStudent(View v) {
		v.setVisibility(View.GONE);
		findViewById(R.id.btn_no_hfu_student).setVisibility(View.GONE);
		findViewById(R.id.text_email).setVisibility(View.VISIBLE);
		findViewById(R.id.text_email_hfu_string).setVisibility(View.VISIBLE);
		findViewById(R.id.input_email).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_parse_mail).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_parse_mail).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText unverifiedEmail = (EditText) findViewById(R.id.input_email);
				String uet = unverifiedEmail.getText().toString();
				if (uet.matches("[A-Za-z]+(.[A-Za-z]+)?(.[A-Za-z]+)?(.[A-Za-z]+)?")) {
					SharedPreferences userData = LoginActivity.this.getSharedPreferences("userData", MODE_PRIVATE);
					SharedPreferences.Editor editor = userData.edit();
					editor.putBoolean("user_hfu_student", true);
					editor.commit();
					startActivity(new Intent(getApplicationContext(), UserVerifyActivity.class).putExtra("unverified_email", uet
							+ "@hs-furtwangen.de"));
				} else {
					IssueNotification.emailNotValid(LoginActivity.this);
				}
			}
		});
	}

	/**
	 * Prüft ob die Google E-Mail Adresse ausgelesen werden kann und setzt diese
	 * als Registrierungsmail (es wird kein Verifizierungsverfahren
	 * durchgeführt, da die Google E-Mail grundsätzlich schon von Google
	 * validiert wurde). Ist die Google E-Mail Adresse nicht auslesbar (weil das
	 * Konto bspw. nicht existiert), werden passende UI-Elemente eingeblendet,
	 * die den Nutzer auffordern, eine E-Mail Adresse zu hinterlegen. Sollte
	 * diese valide sein, wird der Nutzer zur
	 * {@link com.HFUwerdMobil.activity.UserVerifyActivity} weitergeleitet.
	 * 
	 * @param v
	 *            UI-Element, welches dieses Ereignis ausgelöst hat
	 */
	private void isNotHfuStudent(View v) {
		String deviceMail = userData.getDeviceMail();
		if (deviceMail != null) {
			userData.setUserMail(deviceMail);
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		} else {
			v.setVisibility(View.GONE);
			findViewById(R.id.btn_hfu_student).setVisibility(View.GONE);
			findViewById(R.id.text_email).setVisibility(View.VISIBLE);
			findViewById(R.id.input_email).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_parse_mail).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_parse_mail).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Pattern emailRegex = Patterns.EMAIL_ADDRESS;
					EditText unverifiedEmail = (EditText) findViewById(R.id.input_email);
					String uet = unverifiedEmail.getText().toString();
					if (emailRegex.matcher(uet).matches()) {
						startActivity(new Intent(getApplicationContext(), UserVerifyActivity.class).putExtra("unverified_email", uet));
					} else {
						IssueNotification.emailNotValid(LoginActivity.this);
					}
				}
			});
		}
	}

	/**
	 * Gibt das aktuelle Context Objekt zurück
	 * 
	 * @return Context Objekt
	 */
	public static Context getLoginActivityContext() {
		return context;
	}

	/**
	 * Sobald die Activity in den Hintergrund rutscht, wird der möglicherweise
	 * derzeit ausgeführte Nutzer-Daten-Synchronisationsthread und der
	 * Routen-Daten-Synchronisationsthread, interruptet
	 */
	public void onPause() {
		super.onPause();
		if (userDataSync != null)
			userDataSync.interrupt();
		if (routeThread != null)
			routeThread.interrupt();
	}
}