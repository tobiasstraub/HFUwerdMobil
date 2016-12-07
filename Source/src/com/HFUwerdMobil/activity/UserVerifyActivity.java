package com.HFUwerdMobil.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.UserData;
import com.HFUwerdMobil.sync.DataSyncVerifycode;
import com.HFUwerdMobil.util.IssueNotification;

/**
 * Diese Klasse handelt die Benutzer Verifizierungs Activity
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class UserVerifyActivity extends Activity {
	/**
	 * Benutzerdaten Objekt
	 */
	UserData user;

	/**
	 * Unverifizierte E-Mail des Benutzers
	 */
	String userEmail;

	/**
	 * Thread der die Kommunikation zum Server anstößt, um die Verifizierung
	 * durchzuführen
	 */
	Thread verifyThread;

	/**
	 * Erzeugt die Activity. Wartet auf den Verifizierungscode vom Server, der
	 * zusätzlich per E-Mail an die unverifizierte E-Mail Adresse des Benutzers
	 * gesendet wurde. Wartet drauf, dass der Benutzer den korrekten
	 * Verifizierungscode eingibt. Ist das der Fall, wird versucht die
	 * Handynummer des Android Gerätes auszulesen. Ist das möglich wird diese
	 * als "Kontaktnummer" eingetragen, andernfalls ist der Nutzer aufgefordert
	 * eine seine "Kontaktnummer" manuell einzugeben. Dabei wird die
	 * "Kontaktnummer" validiert.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_verify);

		user = new UserData(this);
		userEmail = getIntent().getExtras().getString("unverified_email");

		final String verifycode = getVerifycode(this);
		if (verifycode != null) {
			findViewById(R.id.btn_send_verify_code).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText userVerifycode = (EditText) findViewById(R.id.input_verify_code);
					if (verifycode.equals(userVerifycode.getText().toString())) {
						String deviceNumber = user.getDeviceNumber();
						if (deviceNumber != null) {
							user.initUser(userEmail, deviceNumber);
							startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
						} else {
							Button send_verify_code = (Button) findViewById(R.id.btn_send_verify_code);
							TextView verifyUser = (TextView) findViewById(R.id.textView_verifyUser);

							userVerifycode.setVisibility(View.GONE);
							send_verify_code.setVisibility(View.GONE);
							verifyUser.setVisibility(View.GONE);

							TextView text_info_number = (TextView) findViewById(R.id.textView_info_number);
							TextView text_desc_number = (TextView) findViewById(R.id.textView_desc_number);
							final EditText number = (EditText) findViewById(R.id.input_number);
							Button send_number = (Button) findViewById(R.id.btn_send_number);

							text_info_number.setVisibility(View.VISIBLE);
							text_desc_number.setVisibility(View.VISIBLE);
							number.setVisibility(View.VISIBLE);
							send_number.setVisibility(View.VISIBLE);

							send_number.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									String deviceNumber = number.getText().toString();

									if (deviceNumber.matches("[0-9]{6,}")) {
										user.initUser(userEmail, deviceNumber);
										startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
									} else {
										IssueNotification.deviceNumberNotValid(UserVerifyActivity.this);
									}
								}
							});
						}
					} else {
						IssueNotification.verifycodeInputFailed(UserVerifyActivity.this);
					}
				}
			});
		} else {
			IssueNotification.networkFailed(this);
		}

	}

	/**
	 * Stößt einen {@link com.HFUwerdMobil.sync.DataSyncVerifycode} Thread an
	 * und wartet auf das Ergebnis (in der Regel Verifizierungscode)
	 * 
	 * @param context
	 *            Context Objekt
	 * @return Verifizierungscode oder null, wenn der Server kein oder einen
	 *         falschen Wert liefert
	 */
	private String getVerifycode(Context context) {
		verifyThread = new Thread(new DataSyncVerifycode(userEmail, context));
		verifyThread.start();
		try {
			verifyThread.join();
		} catch (InterruptedException e1) {
			return null;
		}

		String[] response = DataSyncVerifycode.VERIFYCODE_RESPONSE;
		if (response != null) {
			if (response[0].equals("200")) {
				try {
					return new JSONObject(response[1]).getString("verify_code");
				} catch (JSONException e) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Sobald die Activity in den Hintergrund rutscht, wird der
	 * Verifizierungsthread und der Synchronisationsthread, der durch
	 * {@link com.HFUwerdMobil.memory.UserData#initUser(String, String)}
	 * angestoßen wurde, interruptet
	 */
	public void onPause() {
		super.onPause();
		if (verifyThread != null)
			verifyThread.interrupt();
		if (user.userDataSync != null)
			user.userDataSync.interrupt();
	}
}
