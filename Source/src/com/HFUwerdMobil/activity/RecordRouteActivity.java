package com.HFUwerdMobil.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.UserData;
import com.HFUwerdMobil.obd2.BluetoothActivity;
import com.HFUwerdMobil.obd2.OBDservice;

/**
 * Diese Klasse handelt die Route aufzeichnen Activity.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class RecordRouteActivity extends Activity {
	/**
	 * Status des OBD Services
	 */
	private static boolean obdState = false;

	/**
	 * Context Objekt, welches vom OBD2 Service benötigt wird, um Daten in die
	 * Datenbank zu schreiben
	 */
	private static Context context;

	/**
	 * Erzeugt die Activity. Prüft den GPS Status und startet, sofern der Nutzer
	 * den OBD in den Settings aktiviert hat, die
	 * {@link com.HFUwerdMobil.obd2.BluetoothActivity}. Stoppt beim Klick auf
	 * den Beenden Button die Services, nimmt die Activity vom Stack und startet
	 * die {@link com.HFUwerdMobil.activity.RouteDetailsActivity}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_route);

		context = this;

		checkGps();

		if (new UserData(this).getUsesObd()) {
			if (!obdState) {
				obdOn();
				this.startActivityForResult(new Intent(this, BluetoothActivity.class), 0);
			} else {
				obdOff();
				stopService(new Intent(this, OBDservice.class));
			}
		}

		Button endRoute = (Button) findViewById(R.id.endRoute);
		endRoute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), RouteDetailsActivity.class));
				finish();
				stopService(new Intent(RecordRouteActivity.this, com.HFUwerdMobil.service.GPSListener.class));
				stopService(new Intent(RecordRouteActivity.this, com.HFUwerdMobil.obd2.OBDservice.class));
			}
		});
	}

	/**
	 * Handelt den Result der {@link com.HFUwerdMobil.obd2.BluetoothActivity}
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && !data.equals(null)) {
			if (!data.getExtras().getBoolean("ison")) {
				obdOff();
			}
		} else {
			obdOff();
		}
	}

	/**
	 * Stellt den OBD Status auf on
	 */
	private void obdOn() {
		obdState = true;
	}

	/**
	 * Stellt den OBD Status auf off
	 */
	private void obdOff() {
		obdState = false;
	}

	/**
	 * Prüft, ob GPS aktiviert ist. Sollte GPS deaktiviert sein, wird der Nutzer
	 * zur entsprechenden Settings Activity weitergeleitet, auf der er GPS
	 * aktivieren kann.
	 */
	private void checkGps() {
		if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getApplicationContext(), R.string.activate_gps, Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}
	}

	/**
	 * Gibt das aktuelle Context Objekt zurück
	 * 
	 * @return Context Objekt
	 */
	public static Context getRouteRecordContext() {
		return context;
	}
}
