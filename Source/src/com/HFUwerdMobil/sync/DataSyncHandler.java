package com.HFUwerdMobil.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.HFUwerdMobil.activity.LoginActivity;

/**
 * Diese Klasse handelt die Synchronisation zu externen Services (RESTful API).
 * Sie stellt die entsprechenden Methoden zur Verfügung um einfach Daten an die
 * Services zu senden, bzw. Daten von externen Services zu empfangen.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class DataSyncHandler {
	/**
	 * Enthält die "Home"-URL des REST-Services
	 */
	private static final String BASE_REST_URL = "";

	/**
	 * Enthält den "geheimen" API-Key zur Sicherung der Datenübertragung. Wird
	 * verwendet um ein Token für eine sichere Datenübertragung zu generieren.
	 */
	private static final String API_KEY = "";

	/**
	 * E-Mail Adresse des Benutzers, die für die meisten API-Endpunkte benötigt
	 * wird zur eindeutigen Identifikation
	 */
	private String userEmail;

	/**
	 * Empfängt und speichert die Benutzeremail zwischen
	 * 
	 * @param userEmail
	 *            Benutzer E-Mail Adresse
	 */
	public DataSyncHandler(String userEmail) {
		this.userEmail = userEmail;
	}

	/**
	 * Senden einen GET-Request mit vorheriger Sicherheitstoken Generierung an
	 * den übergebenen Endpunkt der RESTful API. Gibt anschließend den
	 * Response-Code und die JSON-Rückgabedaten zurück.
	 * 
	 * @param endpoint
	 *            API-Endpunkt (ohne "Home"-URL und ohne Sicherheitstoken) von
	 *            welchem Daten emfpangen werden sollen
	 * @return String Array mit dem Response Code an Index 0 und dem
	 *         Rückgabe-JSON an Index 1, oder null bei nicht bestehender
	 *         Internetverbindung oder Unerreichen des Servers
	 */
	protected String[] receiveData(String endpoint) {
		if (hasInternetCon()) {
			HttpURLConnection con = null;
			try {
				con = (HttpURLConnection) new URL(BASE_REST_URL + endpoint + "?token=" + createToken(endpoint, "")).openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "application/json");

				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String data = "";
				String dataTmp;
				while ((dataTmp = br.readLine()) != null) {
					data += dataTmp;
				}
				return new String[] { Integer.toString(con.getResponseCode()), data };
			} catch (IOException e) {
				return null;
			} catch (NullPointerException e) {
				return null;
			} finally {
				con.disconnect();
			}
		}
		return null;
	}

	/**
	 * Senden ein übergebenes JSON-Objekt als POST- oder PUT-Request mit
	 * Sicherheitstoken an den übergebenen Endpunkt der RESTful API. Gibt
	 * anschließend den Response-Code zurück.
	 * 
	 * @param endpoint
	 *            API-Endpunkt (ohne "Home"-URL und ohne Sicherheitstoken) an
	 *            welchen die Daten gesendet werden sollen
	 * @param httpMethod
	 *            HTTP-Methode zum Senden der Daten (POST oder PUT)
	 * @param dataOut
	 *            JSON-Objekt, ohne führende und endende geschweifte Klammern
	 *            (da Sicherheitstoken noch angehängt wird)
	 * @return Response-Code, oder -1 bei nicht bestehender Internetverbindung
	 *         oder Unerreichen des Servers
	 */
	protected int sendData(String endpoint, String httpMethod, String dataOut) {
		dataOut = "{\"" + dataOut + createToken(endpoint, dataOut) + "\"}";
		if (hasInternetCon()) {
			HttpURLConnection con = null;
			try {
				con = (HttpURLConnection) new URL(BASE_REST_URL + endpoint).openConnection();
				con.setDoOutput(true);
				con.setRequestMethod(httpMethod);
				con.setRequestProperty("Content-Type", "application/json");

				OutputStream outStream = con.getOutputStream();
				outStream.write(dataOut.getBytes());
				outStream.flush();

				return con.getResponseCode();
			} catch (NullPointerException e) {
				return -1;
			} catch (IOException e) {
				return -1;
			} finally {
				con.disconnect();
			}
		}
		return -1;
	}

	/**
	 * Generiert das Sicherheitstoken, welcher bei jedem Request von oder zur
	 * RESTful API generiert und mit gesendet wird, und durch gleiches
	 * Berechnungsverfahren auf Serverseite auf Korrektheit überprüft wird
	 * 
	 * @param endpoint
	 *            API-Endpunkt (ohne "Home"-URL)
	 * @param json
	 *            JSON-Objekt, ohne führende und endende geschweifte Klammern
	 * @return Sicherheitstoken, oder null falls die Generierung fehlschlug
	 */
	protected String createToken(String endpoint, String json) {
		String tokenData = userEmail + API_KEY + endpoint + json;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(tokenData.getBytes());
			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * Prüft, ob das Android Device derzeit Zugang zum Internet hat
	 * 
	 * @return true, wenn eine Internetverbindung besteht
	 */
	private boolean hasInternetCon() {
		ConnectivityManager cm = (ConnectivityManager) LoginActivity.getLoginActivityContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
}
