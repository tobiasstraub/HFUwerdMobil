package com.HFUwerdMobil.obd2;

public class SocketError extends Exception {

	/**
	 * Diese Klasse ist der Fehler den die Klasse BluetoothStream auslösen kann,
	 * wenn kein gültiger Bluetoothsocket verbunden wird.
	 * 
	 * @author OBD-Gruppe
	 * @version 1.0
	 */
	private static final long serialVersionUID = 1L;

	public SocketError(String s) {
		super(s);
	}

}
