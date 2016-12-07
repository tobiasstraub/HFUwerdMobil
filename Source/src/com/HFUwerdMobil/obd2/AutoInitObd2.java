package com.HFUwerdMobil.obd2;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Diese Klasse sorgt für die Initialisierung des ODB Adapters mit dem ELM
 * Protokoll. Dazu wird ELM über die AT Befehle wird das ELM Protokoll
 * zurückgesetzt, dannach werden die PIDS die benötigt werden initialisiert. Die
 * klasse hat einen BluetoothStream der schon mit dem OBD Adapter verbunden ist.
 * Die Klasse läuft als Thread.
 * 
 * @author OBD-Gruppe
 * @version 1.0
 * */
public class AutoInitObd2 extends AsyncTask<BluetoothStream, Integer, Integer> {

	private BluetoothStream stream;

	@Override
	protected Integer doInBackground(BluetoothStream... stream) {
		/**
		 * Initialisierung des ELM Protokolls. Dabei wird das Protokoll durch
		 * ATZ zurückgesetzt ATL0 aktiviert das senden von Headern ATSP0
		 * initialisiert das ELM Protokoll das das Auto unterstütz automatisch
		 * ATDP fragt ab ob die Abfrage automatisch passiert.
		 * 
		 * FIRSTCOMMAND initialisiert die ersten 20 PIDs SECOUNDCOMMAND
		 * initialisiert die PIDs 20-40 THIRDCOMMAND initialisiert die PIDs
		 * 40-60
		 * 
		 * Initialisierung dauert 22 Sekunden
		 * */
		this.stream = stream[0];

		int sup = 0;

		initMsg(0, OBD2E.INITATZ);
		initMsg(OBD2E.LONGTIME, OBD2E.INITATE0);
		initMsg(OBD2E.MIDDLETIME, OBD2E.INITATL0);

		initMsg(OBD2E.LONGTIME, OBD2E.AUTO_ATSP0);
		initMsg(OBD2E.LONGTIME, OBD2E.AUTO_ATDP);
		if (initMsg(OBD2E.LONGTIME, 3000, OBD2E.FIRSTCOMMAND, 2).contains("41"))
			sup += 1;
		if (initMsg(OBD2E.LONGTIME, 3000, OBD2E.SECOUNDCOMMAND, 1).contains("41"))
			sup += 2;
		if (initMsg(OBD2E.LONGTIME, 3000, OBD2E.THIRDCOMMAND, 1).contains("41"))
			sup += 3;
		return sup;
	}

	private void initMsg(int time, String wrote) {
		/** normale readtime und einmalige wiederholung */
		initMsg(time, OBD2E.READTIME, wrote, 1);
	}

	private String initMsg(int time, int readtime, String wrote, int readtimes) {
		/** schreibt eine Nachricht an den OBD Bluetooth Adapter */
		stream.write(OBD2E.sleepCommand(time, wrote));
		Log.e("wrote", wrote);
		String msg = "";
		for (int i = 0; i < readtimes; i++) {
			OBD2E.sleep(readtime);
			msg = Casts.getString(stream.read());
			Log.e("read", msg);
		}
		return msg;
	}

}
