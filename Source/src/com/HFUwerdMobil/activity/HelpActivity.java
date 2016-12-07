package com.HFUwerdMobil.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.HFUwerdMobil.main.R;

/**
 * Diese Klasse handelt die Help Activity. Die Help Activity enthält
 * Hilfsinformationen zur Nutzung der App.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class HelpActivity extends Activity {
	/**
	 * Erzeugt die Activity als WebView
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		WebView helpContent = (WebView) findViewById(R.id.help_content);
		String text = "<html><body>" + "<p align=\"justify\">" + getString(R.string.help_content) + "</p>" + "</html>";
		helpContent.loadData(text, "text/html", "utf-8");
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
