package com.HFUwerdMobil.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.UserData;

/**
 * Diese Klasse handelt die Einstellungs Activity. Die Activity enhält alle
 * Einstellungen die den Nutzer betreffen.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class SettingsActivity extends Activity {
	/**
	 * Nutzer-Daten Objekt
	 */
	UserData userData;

	/**
	 * Erzeugt und füllt die View mit den Werten des Nutzers
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		ImageView user_picture = (ImageView) findViewById(R.id.img_user_picture);
		user_picture.getLayoutParams().height = size.y / 3;

		userData = new UserData(this);

		EditText firstname = (EditText) findViewById(R.id.firstname);
		EditText lastname = (EditText) findViewById(R.id.lastname);
		EditText hometown = (EditText) findViewById(R.id.hometown);
		EditText phone = (EditText) findViewById(R.id.phone);
		EditText email = (EditText) findViewById(R.id.email);
		Switch obd = (Switch) findViewById(R.id.tBtn_obd);

		email.setKeyListener(null);
		email.setEnabled(false);

		firstname.setText(userData.getFirstname());
		lastname.setText(userData.getLastname());
		hometown.setText(userData.getHometownZip());
		phone.setText(userData.getMobileNumber());
		email.setText(userData.getUserMail());

		if (userData.getUsesObd()) {
			obd.setChecked(true);
		} else {
			obd.setChecked(false);
		}
	}

	/**
	 * Erzeugt das Optionmenü
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.popupmenu, menu);
		return true;
	}

	/**
	 * Handelt das Optionmenü
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_home:
			startActivity(new Intent(this, ChooseRouteActivity.class));
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.menu_imprint:
			startActivity(new Intent(this, ImprintActivity.class));
			return true;
		case R.id.menu_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		default:
			super.onOptionsItemSelected(item);
		}
		return false;
	}

	/**
	 * Speichert die Daten des Nutzers ab
	 * 
	 * @param v
	 *            UI-Element, welches die Speicherung ausgelöst hat
	 */
	public void saveSettings(View v) {
		EditText firstname = (EditText) findViewById(R.id.firstname);
		EditText lastname = (EditText) findViewById(R.id.lastname);
		EditText hometown = (EditText) findViewById(R.id.hometown);
		EditText phone = (EditText) findViewById(R.id.phone);
		Switch obd = (Switch) findViewById(R.id.tBtn_obd);

		boolean obdOn = obd.isChecked();
		if (obdOn) {
			userData.setUsesObd(true);
		} else {
			userData.setUsesObd(false);
		}

		userData.setFirstname(firstname.getText().toString());
		userData.setLastname(lastname.getText().toString());
		userData.setHometownZip(hometown.getText().toString());
		userData.setMobileNumber(phone.getText().toString());

		Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.savedSettings), Toast.LENGTH_LONG).show();
	}

	/**
	 * Interruptet den Synchronisationsthread der Benutzerdaten, sobald die
	 * Activity in den Hintergrund rückt
	 */
	public void onPause() {
		super.onPause();
		if (userData.userDataSync != null)
			userData.userDataSync.interrupt();
	}
}