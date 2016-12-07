package com.HFUwerdMobil.obd2;

import java.util.concurrent.ExecutionException;

import android.util.Log;

import com.HFUwerdMobil.memory.DataAccessHandler;
import com.HFUwerdMobil.memory.EntityOBD;

/**
 * Diese Klasse kümmert sich um den Datenaustausch zwischen Handy und Gerät. Sie
 * verbirgt die Verbindung mit dem OBD2 Gerät. Dazu initialisiert Sie zuerst die
 * OBD Protokolle über einer Instanz der Klasse AutoInitObd2, dannach frägt Sie
 * verschiedene PIDS ab und schreibt Sie in eine Datenbank.
 * 
 * stream ist die Bluetoothverbindung zum OBD Adapter send ist eine Instanz des
 * privaten Threads BgSend pidsupport hält die Anzahl der unterstützen PIDs
 * 
 * @author OBD-Gruppe
 * @version 1.0
 * */
public class Connect {

	private BluetoothStream stream;
	private BgSend send;
	private int pidsupport;

	private DataAccessHandler sHandler = new DataAccessHandler(com.HFUwerdMobil.activity.RecordRouteActivity.getRouteRecordContext());

	public Connect() {
		/** erstellt die Instanzen */
		send = new BgSend();
		try {
			stream = new BluetoothStream(BluetoothActivity.bluesocket);
		} catch (SocketError e) {
			e.printStackTrace();
		}
	}

	public void start() {
		/** Startet die OBD2 Verbindung */
		send.start();
	}

	public void close() {
		/** Beended den Thread send */
		send.close();
	}

	private void saveToDB(String... msg) {
		/** Hier werden die übergeben Werte in die Datenbank gespeichert */
		sHandler.addOBD(new EntityOBD(sHandler.getLastAddedRouteId(), Integer.parseInt(msg[0]), Integer.parseInt(msg[1])));

	}

	private class BgSend extends Thread {
		/**
		 * @author Alex privater Thread, der neben der MAIN läuft um deren
		 *         Thread nicht zu blocken und so eine Exception zu werfen, die
		 *         die App abstürtzen lassen kann. Hier werden Daten an den OBD
		 *         Adapter gesendet und abgefragt
		 * 
		 *         running dient zum ausschalten des Threads
		 */
		public boolean running;

		public BgSend() {
			/** Thread wird angeschalten */
			running = true;
		}

		@Override
		public void run() {
			/**
			 * diese Methode läuft asynchrone zuerst wird OBD initialisiert,
			 * über eine Instanz von AutoInitObd2 dieser Thread wartet bis die
			 * Initalisierung abgeschlossen wird und prüft an den Rückgabewerten
			 * welche PIDS unterstütz werden in der folgenden while Schleife
			 * werden dann immer wieder speed und fuel vom OBD Adapter
			 * abgefragt. Die Ergebnisse werden über die Casts in den richtigen
			 * Datentyp konvertiert und dann an die saveToDB übergeben
			 * 
			 * Dauer für einen Durchlauf 8 Sekunden.
			 * */
			super.run();
			AutoInitObd2 init = new AutoInitObd2();
			init.execute(stream);
			try {
				pidsupport = init.get();
				OBD2E.sleep(OBD2E.MIDDLETIME);
				switch (pidsupport) {
				case 0:
					Log.i("PID SUPPORT", "NONE");
					break;
				case 1:
					Log.i("PID SUPPORT", "0-20");
					break;
				case 2:
					Log.i("PID SUPPORT", "20-40");
					break;
				case 3:
					Log.i("PID SUPPORT", "0-40");
					break;
				case 4:
					Log.i("PID SUPPORT", "0-20,40-60");
					break;
				case 5:
					Log.i("PID SUPPORT", "20-40,40-60");
					break;
				case 6:
					Log.i("PID SUPPORT", "0-60");
					break;
				}
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}
			String fuel = "";
			String speed = "";
			double rpm, iat, map;

			sHandler.openDB();
			while (running) {
				String num = OBD2E.Msg(OBD2E.sleepCommand(OBD2E.MIDDLETIME, OBD2E.M1_2_PID_SPEED));
				stream.write(num);
				Log.i("wrote: ", num);
				speed = readSpeed(OBD2E.READTIME);
				Log.i("read ", speed);

				num = OBD2E.Msg(OBD2E.sleepCommand(OBD2E.LONGTIME, OBD2E.M1_2_PID_RPM));
				stream.write(num);
				Log.i("wrote: ", num);
				rpm = readRPM(OBD2E.READTIME);
				Log.i("read ", rpm + "");

				num = OBD2E.Msg(OBD2E.sleepCommand(OBD2E.LONGTIME, OBD2E.M1_2_PID_IAT));
				stream.write(num);
				Log.i("wrote: ", num);
				iat = readIAT(OBD2E.READTIME);
				Log.i("read ", iat + "");

				num = OBD2E.Msg(OBD2E.sleepCommand(OBD2E.LONGTIME, OBD2E.M1_2_PID_MAP));
				stream.write(num);
				Log.i("wrote: ", num);
				map = readMAP(OBD2E.READTIME);
				Log.i("read ", map + "");

				fuel = Casts.getFuel(rpm, map, iat) + "";
				Log.i("fuel", fuel);
				saveToDB(speed, fuel);
			}
			sHandler.closeDB();
		}

		protected void close() {
			/** Thread wird beended */
			running = false;
		}

		private double readRPM(int timeout) {
			/** Methode die RPM liest */
			OBD2E.sleep(timeout);
			return Casts.getRPM(Casts.getString(stream.read()));
		}

		private double readIAT(int timeout) {
			/** Methode die IAT liest */
			OBD2E.sleep(timeout);
			return Casts.getIAT(Casts.getString(stream.read()));
		}

		private double readMAP(int timeout) {
			/** Methode die MAP liest */
			OBD2E.sleep(timeout);
			return Casts.getMAP(Casts.getString(stream.read()));
		}

		private String readSpeed(int timeout) {
			/** Die Methode gibt die Geschwindigkeit zurück */
			OBD2E.sleep(timeout);
			return Casts.getSpeed(Casts.getString(stream.read()));
		}

	}
}
