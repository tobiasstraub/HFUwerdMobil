package com.HFUwerdMobil.obd2;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.HFUwerdMobil.main.R;

/**
 * Die Mainactivity ist der Einstiegspunkt des Bluetoothfeatures. Durch diese
 * Activity wird das Paaren (pairing) zweier Geröte intern überwacht. Nach dem
 * pairing, werden Die Geräte miteinander Verbunden, diese Verbindung wird im
 * Attribute bluesocket gespeichert. Nach dem der Bluetoothsocket etabliert
 * wurde startet der OBD Service automatisch
 * 
 * @author OBD-Gruppe
 * @version 1.0
 * */
public class BluetoothActivity extends Activity {

	private ArrayAdapter<String> listAdapter;
	private ListView listView;
	private Activity view;
	private BluetoothAdapter bluetooth;
	private ArrayList<BluetoothDevice> devices;
	private IntentFilter filter;
	private BroadcastReceiver receiver, receiver1, receiver2, receiver3, receiverpair;
	private OnItemClickListener listener;
	public static BluetoothSocket bluesocket;
	private static boolean isturnonrequest = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		/**
		 * Hier werden ähnlich wie in einem Konstruktor Startdaten festgelegt.
		 * Es wird eine Layout verknüpft, das wir als XMLvordefiniert haben
		 * (select_bluetooth). Nach dem Layout werden die einzelnen
		 * LayoutElemente Verknüpft. Das wichtigste Element des Layouts ist die
		 * Liste, Sie zeigt später die verfügbaren Bluetoothgeräte an. eine
		 * Liste besteht aus einem GUI Element ListView und einem ListAdaper,
		 * der den Inhalt der Liste verwaltet.
		 * 
		 * Desweiteren wird hier der BluetoothAdapter bluetooth inizalisiert,
		 * indem der Bluetoothsensor per getDefaultAdapter zur Verfügung
		 * gestellt wird. Falls Bluetooth aus ist, oder kein Sensor vorhanden
		 * ist, hat bluetooth den Wert null. Ist das der Fall, wird Bluetoth
		 * angeschalten oder eine Meldung erscheint das kein Sensor vorhanden
		 * ist.
		 * 
		 * Zuletzt werden Intentfilter angemeldet. Diese werden durch Aktionen
		 * der Bluetoothfilters ausgelöst, um später die Fortschritte zu
		 * überwachen (tracing)
		 * */
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_select_bluetooth);
		view = this;
		setProgressBarIndeterminateVisibility(false);
		listView = (ListView) findViewById(R.id.b_listView);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		listView.setAdapter(listAdapter);
		listener = new ItemClickBT();
		listView.setOnItemClickListener(listener);

		bluetooth = BluetoothAdapter.getDefaultAdapter();
		devices = new ArrayList<BluetoothDevice>();

		if (bluetooth == null) {
			Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
			close();
		} else {
			if (!bluetooth.isEnabled()) {
				turnOnBT();
			} else
				startDiscovery();

		}
		for (BluetoothDevice d : bluetooth.getBondedDevices())
			unpairDevice(d);

		filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		receiverpair = new ReceiverBTPair();
		registerReceiver(receiverpair, filter);
		receiver = new ReceiverBT();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		receiver1 = new ReceiverBT();
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver1, filter);
		receiver2 = new ReceiverBT();
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver2, filter);
		receiver3 = new ReceiverBT();
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver3, filter);
		reset();
	}

	private void startDiscovery() {
		/**
		 * startet eine Bluetooth Gerätesuche, und beended die vorherige, falls
		 * vorhanden
		 */
		bluetooth.cancelDiscovery();
		bluetooth.startDiscovery();

	}

	private void reset() {
		/**
		 * Löscht die Liste der Bluetooth Geräte und startet einen neuen
		 * Gerätesuchlauf
		 */
		devices.clear();
		startDiscovery();
	}

	public void refresh(View view) {
		/**
		 * Wrapper der ResetMethode für einen Button (erwartet intern View
		 * Parameter)
		 */
		reset();
	}

	private void turnOnBT() {
		/**
		 * isturnonrequest wird auf true gesetzt, dannach wird eine
		 * Bluetoothanfrage ausgelöst
		 */
		isturnonrequest = true;
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/**
		 * Diese Methode wird aufgerufen, wenn die Methode ein Ergebnis
		 * zurückbekommet (Hier von der Bluetoothanfrage) falls das anschalten
		 * des Bluetooth nicht erfolg wird die App beendet falls das Bluetooth
		 * angeschalten wurde, wird das suchen nach Geräten gestartet.
		 * */
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
			close();
		}
		isturnonrequest = false;
		startDiscovery();
	}

	private class ReceiverBT extends BroadcastReceiver {
		/**
		 * Private Klasse ReceiverBT, diese Klasse arbeitet mit den Einkommenden
		 * Bluetooth Ereignissen. Der Receiver wurde oben mit IntentFiltern auf
		 * die Ereignisse spezialisiert.
		 * */

		@Override
		public void onReceive(Context context, Intent intent) {
			/**
			 * diese Methode entscheided was passiert, wenn ein angemeldetes
			 * Ereignis ausgelöst wird. 1) Ein neues Bluetoothgerät wird
			 * gefunden, falls es noch nicht vorhanden ist, wird es der Liste
			 * Hinzugefügt, dannach wird die Liste gelöscht und alle gefunden
			 * Geräte werden wieder eingetragen, damit Geräte die nicht mehr
			 * vorhanden sind auch nicht auftauchen. 2) Gerätesuche wird
			 * gestarte, Wartesymbol wird gesetzt. 3) Gerätesuche wird beendet,
			 * Wartesymbol wird versteckt. 4) Bluetooth wird deaktivert, eine
			 * Anfrage zur Bluetoothaktivierung wird gesendet.
			 * */
			switch (intent.getAction()) {
			case BluetoothDevice.ACTION_FOUND:
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (!devices.contains(device))
					devices.add(device);
				listAdapter.clear();
				for (BluetoothDevice dev : devices) {
					if (bluetooth.getBondedDevices().contains(dev)) {
						listAdapter.add(dev.getName() + "      paired      " + "\n" + dev.getAddress());
					} else {
						listAdapter.add(dev.getName() + "\n" + dev.getAddress());
					}
				}

				break;
			case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
				setProgressBarIndeterminateVisibility(true);
				break;
			case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
				setProgressBarIndeterminateVisibility(false);
				break;

			case BluetoothAdapter.ACTION_STATE_CHANGED:
				if (bluetooth.getState() == BluetoothAdapter.STATE_OFF && !isturnonrequest) {
					turnOnBT();
				}
			}
		}

	}

	private class ItemClickBT implements OnItemClickListener {
		/** Private Klasse die die Clicks auf Listensymbole auswertet */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			/**
			 * Diese Methode wird durch eine Klick auf eine Listenelement
			 * ausgelöst. das Gerätesuchen wird beendet. Dannach wird das Gerät
			 * gepaired, oder falls es schon gepaired ist unpaired.
			 * */
			if (bluetooth.isDiscovering()) {
				bluetooth.cancelDiscovery();
			}
			BluetoothDevice device = devices.get(position);
			if (bluetooth.getBondedDevices().contains(device)) {
				unpairDeciveConnected(device);
			} else {
				new AsyncPair().execute(device);
			}
		}

	}

	/** RMI METHODES OF BLUETOOTH **/
	private void unpairDevice(BluetoothDevice device) {
		/**
		 * Diese Methode löscht ein gepaired es Gerät. Um auch Geräte ab 3.0 Zu
		 * nutzten, wird die private Methode removeBond aufgerufen.
		 * */
		try {
			Method method = device.getClass().getMethod("removeBond", (Class[]) null);
			method.invoke(device, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unpairDeciveConnected(BluetoothDevice device) {
		/**
		 * Diese Methode löscht alle gepaired es Gerät und gibt einen Text aus.
		 * */
		unpairDevice(device);
		Toast.makeText(getApplicationContext(), "Pairing dissconnected", Toast.LENGTH_SHORT).show();
	}

	private void pairDevice(BluetoothDevice device) {
		/**
		 * Diese Methode paired ein Gerät. Um auch Geräte ab 3.0 Zu nutzten,
		 * wird die private Methode removeBond aufgerufen.
		 * */
		try {
			Method method = device.getClass().getMethod("createBond", (Class[]) null);
			method.invoke(device, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class AsyncPair extends AsyncTask<BluetoothDevice, BluetoothDevice, BluetoothDevice> {
		/**
		 * Dieser Thread führt das Paaren zweiter Bluetoothgeräte asynchrone aus
		 */

		@Override
		protected BluetoothDevice doInBackground(BluetoothDevice... params) {
			/**
			 * asynchrone werden alle Geräte gelöscht, dannach wir das richtige
			 * Geräte mit dem Device gepaired.
			 * */
			if (bluetooth.getBondedDevices().size() > 0)
				for (BluetoothDevice d : bluetooth.getBondedDevices()) {
					unpairDevice(d);
				}
			pairDevice(params[0]);
			return params[0];
		}

		@Override
		protected void onPreExecute() {
			/**
			 * Wird vor der ausführung des asynchronen Tasks ausgeführt, sorgt
			 * für das Bessere Userverständiss (Wartesymbol und Text was
			 * passiert)
			 * */
			super.onPreExecute();
			bluetooth.cancelDiscovery();
			view.setProgressBarIndeterminateVisibility(true);
			Toast.makeText(getApplicationContext(), "Pairing started", Toast.LENGTH_SHORT).show();
		}

	}

	private class ReceiverBTPair extends BroadcastReceiver {
		/**
		 * Receiver der wie davor die Ereignisse des Bluetoothgerätes abfängt,
		 * dieser Receiver ist auf den Verbindungsstatus von Bluetooth
		 * spezialisiert. 1) Wird ausgelöst falls etwas mit Bluetooth passiert
		 * 1.1) Falls das Gerät versucht hat sich zu paaren und nun gepaart ist,
		 * wird eine Meldung ausgegeben das Bluetooth gepaired ist, außerdem
		 * wird jetzt die verbindung (connection) gestartet. Und die Liste der
		 * Geräte gelöscht. 1.2) Falls das Gerät vom Zustand gepaart zu nicht
		 * gepaart wechselt wird hier die Liste der Geräte gelöscht
		 * 
		 * das löschen der Liste ähnelt einem update, da alle Werte erneut
		 * eingetragen werden.
		 * 
		 * */

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
				final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

				if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
					Toast.makeText(getApplicationContext(), "Pairing established", Toast.LENGTH_SHORT).show();
					reset();
					new AsyncConnect().execute((BluetoothDevice) bluetooth.getBondedDevices().toArray()[0]);

				} else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
					// gets also called when existing paired Devices get deleted
					reset();
				}
			}
		}
	}

	private class AsyncConnect extends AsyncTask<BluetoothDevice, BluetoothSocket, BluetoothSocket> {
		/** Diese Klasse ist eine Thread der asynchrone die Verbindung etabliert */

		@Override
		protected BluetoothSocket doInBackground(BluetoothDevice... params) {
			/**
			 * Diese Methode wird asyncrhone ausgeführ, hier wird ein Gerät das
			 * gepaart wurde verbunden, dabei wird die GeräteId des anderen
			 * Gerätes in die Methode createRfcommSocketToServiceRecord
			 * übergeben, diese Methode öffnet den Bluetoothsocket über den die
			 * Communication läuft. Dannach wird der Socket verbunden. Fehler
			 * werden abgefangen und gelogged. Der Socket wird aus zurückgegebn.
			 * */
			BluetoothSocket socket = null;
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				Log.e("error s1", socket + "");
				UUID deviceUuid = params[0].getUuids()[0].getUuid();
				Log.e("Uuid", deviceUuid + "");
				socket = params[0].createRfcommSocketToServiceRecord(deviceUuid);
				Log.e("error s2", socket + "");
				socket.connect();
				Log.e("error s3", socket + "");
			} catch (IOException e) {
				Log.e("Bluetooth run", "connect failed:/n" + e.getStackTrace());
				// Unable to connect; close the socket and get out
				try {
					socket.close();
					socket = null;
					close();
				} catch (IOException closeException) {
				}
			}

			return socket;
		}

		@Override
		protected void onPreExecute() {
			/**
			 * Diese Methode wird vor dem asynchronen Thread ausgefürht und
			 * informiert den Nutzer was gerade Passiert.
			 * */
			super.onPreExecute();
			view.setProgressBarIndeterminateVisibility(true);
			Toast.makeText(getApplicationContext(), "Connection started", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(BluetoothSocket result) {
			/**
			 * Diese Methode wird nach dem Beenden des asynchronen Threads
			 * ausgeführt, der Nutzer wird wieder informiert, desweiteren wird
			 * der OBD2 Service gestartet. Falls das Verbinden Fehlgeschlagen
			 * ist, erscheint eine Meldung.
			 * */
			super.onPostExecute(result);
			if (result != null) {
				bluesocket = result;
				Toast.makeText(getApplicationContext(), "Connection success", Toast.LENGTH_SHORT).show();
				view.setProgressBarIndeterminateVisibility(false);

				startService(new Intent(getApplicationContext(), com.HFUwerdMobil.obd2.OBDservice.class));
				close();
			} else
				Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onBackPressed() {
		/** die Action bei dem Rückwärtsbutton wird hier überschrieben */
		close();
	}

	public void close() {
		/**
		 * diese Funktion schließt die Activity und gibt zurück ob die
		 * Verbindung geklappt hat oder nicht
		 * */
		Intent data = new Intent();
		if (bluesocket != null)
			data.putExtra("ison", true);
		else
			data.putExtra("ison", false);
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, data);
		} else {
			getParent().setResult(Activity.RESULT_OK, data);
		}
		this.unregisterReceiver(receiver);
		this.unregisterReceiver(receiver1);
		this.unregisterReceiver(receiver2);
		this.unregisterReceiver(receiver3);
		this.unregisterReceiver(receiverpair);
		finish();
	}

}
