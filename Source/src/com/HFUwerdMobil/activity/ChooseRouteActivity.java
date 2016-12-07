package com.HFUwerdMobil.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.HFUwerdMobil.main.R;
import com.HFUwerdMobil.memory.DataAccessHandler;
import com.HFUwerdMobil.memory.EntityRoute;
import com.HFUwerdMobil.util.Geocoding;

/**
 * Diese Klasse handelt die Auswahl eines Routenziels und listet die bereits
 * abgeschlossenen Routen auf.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class ChooseRouteActivity extends ListActivity {
	/**
	 * Deklariert die Liste, mit den bereits abgeschlossenen Routen
	 */
	ArrayList<String> list = new ArrayList<String>();

	/**
	 * Adapter für die Liste
	 */
	ArrayAdapter<String> adapter;

	/** {@link com.HFUwerdMobil.memory.DataAccessHandler} Objekt **/
	DataAccessHandler sHandler = new DataAccessHandler(this);

	/**
	 * Erzeugt die Activity und definiert einen Listener für den Button um die
	 * ausgewählte Route zu starten. Definiert zu den auswählbaren Ziele die
	 * Destination Codes und gibt beim Klick auf den Button den korrekten
	 * Destination Code an den {@link com.HFUwerdMobil.service.GPSListener}
	 * Service weiter. Prüft zusätzlich den GPS Status.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_route);
		sHandler.openDB();

		listRoutes(5);

		Button startRoute = (Button) findViewById(R.id.start_route);
		startRoute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), com.HFUwerdMobil.service.GPSListener.class);
				Spinner spinner = (Spinner) findViewById(R.id.locations);
				if (spinner.getSelectedItem().toString().equals(getString(R.string.furtwangen))) {
					i.putExtra("selected_destination", 1);
				}
				if (spinner.getSelectedItem().toString().equals(getString(R.string.schwenningen))) {
					i.putExtra("selected_destination", 2);
				}
				if (spinner.getSelectedItem().toString().equals(getString(R.string.tuttlingen))) {
					i.putExtra("selected_destination", 3);
				}
				if (spinner.getSelectedItem().toString().equals(getString(R.string.home))) {
					i.putExtra("selected_destination", 0);
				}
				startService(i);
				startActivity(new Intent(getApplicationContext(), RecordRouteActivity.class));
			}
		});

		checkGps();

		sHandler.closeDB();
	}

	/**
	 * Listet die bereits abgeschlossenen Routen in einer Liste auf. Kodiert
	 * dazu die Koordinaten und den Destination Code zu Ortsnamen. Setzt den
	 * Elementen außerdem Links zu der
	 * {@link com.HFUwerdMobil.activity.RouteDetailsActivity}, um detaillierte
	 * Informationen zu der bereits abgeschlossenen Route einzusehen.
	 * 
	 * @param max
	 *            Anzahl der maximal auszugebenden Listenelemente
	 */
	private void listRoutes(int max) {
		List<EntityRoute> routes = sHandler.getAllRoutes();
		adapter = new ArrayAdapter<String>(this, R.layout.recent_routes, list);
		if (routes != null) {
			if (routes.size() < max) {
				max = routes.size() - 1;
			}
			for (int i = 0; i <= max; i++) {
				try {
					String start = new Geocoding(routes.get(i).getStartLat(), routes.get(i).getStartLon()).execute().get();
					if (start == null) {
						start = "";
					}
					String dest = "";
					switch (routes.get(i).getEndPoint()) {
					case 0:
						dest = getResources().getString(R.string.home);
						break;
					case 1:
						dest = getResources().getString(R.string.furtwangen);
						break;
					case 2:
						dest = getResources().getString(R.string.schwenningen);
						break;
					case 3:
						dest = getResources().getString(R.string.tuttlingen);
						break;
					}
					adapter.add(start + "\n" + dest);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} else {
			adapter.add(getResources().getString(R.string.no_routes_to_show));
		}
		setListAdapter(adapter);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				sHandler.openDB();
				SharedPreferences userData = getApplicationContext().getSharedPreferences("userData", MODE_PRIVATE);
				SharedPreferences.Editor editor = userData.edit();
				editor.putString("route_id", sHandler.getRoute((int) id + 1).getRouteId());
				editor.commit();
				startActivity(new Intent(getApplicationContext(), RouteDetailsActivity.class));
				sHandler.closeDB();
			}
		});
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
}