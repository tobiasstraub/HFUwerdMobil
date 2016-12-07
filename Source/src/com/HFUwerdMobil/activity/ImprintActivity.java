package com.HFUwerdMobil.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.HFUwerdMobil.main.R;

/**
 * Diese Klasse handelt die Imprint Activity. Die Imprint Activity enthält das
 * Impressum der App.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class ImprintActivity extends Activity {
	/**
	 * Erzeugt die Activity als WebView
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imprint);
		WebView imprintContent = (WebView) findViewById(R.id.imprint_content);
		String text = "<html><body>" + "<p align=\"justify\">" + getString(R.string.imprint_content) + "</p>" + "</html>";
		imprintContent.loadData(text, "text/html", "utf-8");
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
