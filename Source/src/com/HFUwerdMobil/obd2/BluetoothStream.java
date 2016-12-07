package com.HFUwerdMobil.obd2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Die Stream Klasse bietet die Möglichkeit einen Bluetoothsocket einfach zu
 * benutzten dabei sorgt die Klasse immer dafür das der Socket verbunden ist,
 * und gleichzeitig die in und out put sockets benutzbar bleibe.
 * 
 * @author OBD-Gruppe
 * @version 1.0
 * */
public class BluetoothStream {

	private BluetoothSocket socket;
	private BluetoothDevice device;
	private InputStream in;
	private OutputStream out;

	public BluetoothStream(BluetoothSocket socket) throws SocketError {
		/**
		 * Konstruktor der Klasse BluetoothStram, welche einen BluetoothSocket
		 * entgegennimmt, der Socket enthält das Verbunde Gerät, falls kein Grät
		 * verbunden ist, wird ein SocketError ausgelöst, da offensichtlich kein
		 * Stream ohne zweites Gerät etabliert werden kann. Das verbunde Gerät
		 * wir zusätzlich im Attribute device gespeichert, falls die Verbindung
		 * abbricht, kann so der BluetoothSocket neu erstellt werden
		 * */
		if (socket == null)
			throw new SocketError("Socket is null, BluetoothStream is useless");
		this.socket = socket;
		device = socket.getRemoteDevice();

		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			// Should never been thrown, because socket is never null
			e.printStackTrace();
		}

	}

	public BluetoothDevice getDevice() {
		/** device Getter */
		return device;
	}

	public byte[] read() {
		/**
		 * Diese Methode prüft die Verbindung der Geräte, wenn diese Verbunden
		 * sind, werden die eingehenden Daten ausgelesen und als Bytes
		 * zurückgibt.
		 * */
		if (!isConnected()) {
			Log.e("read", "socket is not connected, but is should be");
			return null;
		}
		byte[] msg = null;

		try {
			msg = new byte[OBD2E.PIDRECEIVELENGTH];
			in.read(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}

	public boolean write(String sqz) {
		/**
		 * Diese Methode prüft die Verbindung der Geräte, wenn diese Verbunden
		 * sind, werden die übergebenen Daten als Bytes zum anderen Geräte
		 * übertragen.
		 * */
		if (!isConnected()) {
			Log.e("write", "socket is not connected, but is should be");
			return false;
		}
		try {
			out.write(sqz.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean isConnected() {
		/**
		 * Diese Methode prüft die Verbindung, falls keine Verbindung vorhanden
		 * ist, gibt wird einaml versucht sich mit dem gespeicherten Gerät zu
		 * verbinden. Das Ergebnis der Verbindung wird durch einen Boolean
		 * zurückgegen (true für verbudnen).
		 * */
		if (!socket.isConnected())
			try {
				socket.connect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return socket.isConnected();
	}

}
