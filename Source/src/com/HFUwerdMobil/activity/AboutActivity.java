package com.HFUwerdMobil.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.HFUwerdMobil.main.R;

/**
 * Diese Klasse handelt die About About Activity. Die About Activity enthält ein
 * Grid mit Fotos der Verantwortlichen Team-Member der App.
 * 
 * @author Tobias Straub, Susanne Roos, Matthias Feichtmeier, Thomas Lesinski,
 *         Veronika Kinzel
 * @version 1.0
 */
public class AboutActivity extends Activity {
	/**
	 * Zweidimensionale GridView
	 */
	GridView gvMain;

	/**
	 * Erzeugt die View
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		gvMain = (GridView) findViewById(R.id.gvMain);
		gvMain.setAdapter(new ImgAdapter(this));
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

	/**
	 * Adapterklasse, welche für die Auslieferung der Images zuständig ist
	 * 
	 * @author Tobias Straub, Susi Roos, Thomas Lesinski, Matthias Feichtmeier,
	 *         Veronika Kinzel, Elena Fedorow
	 * @version 1.0
	 * 
	 */
	private class ImgAdapter extends BaseAdapter {

		private List<Item> items = new ArrayList<Item>();
		private LayoutInflater inflater;

		public ImgAdapter(Context context) {
			inflater = LayoutInflater.from(context);

			items.add(new Item(getResources().getString(R.string.tobiasstraub), R.drawable.b3));
			items.add(new Item(getResources().getString(R.string.susanneroos), R.drawable.b4));
			items.add(new Item(getResources().getString(R.string.thomaslesinski), R.drawable.b8));
			items.add(new Item(getResources().getString(R.string.matthiasfeichtmeier), R.drawable.b6));
			items.add(new Item(getResources().getString(R.string.veronikakinzel), R.drawable.b5));
			items.add(new Item(getResources().getString(R.string.hfuwerdmobillogo), R.drawable.ic_launcher));

		}

		/**
		 * Gibt die Anzahl der Items in der Arrayliste zurück
		 */
		@Override
		public int getCount() {
			return items.size();
		}

		/**
		 * Gibt das Item an einer bestimmten Position in der Arraylist zurück
		 * 
		 * @param position
		 *            Position des Elements, welches zurückgegeben werden soll
		 */
		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		/**
		 * Gibt die Bild ID des Item an einer bestimmten Position in der
		 * Arraylist zurück
		 * 
		 * @param position
		 *            Position des Elements, welches die Bild Id zurückgeben
		 *            soll
		 */
		@Override
		public long getItemId(int position) {
			return items.get(position).drawableId;
		}

		/**
		 * Gibt die View zurück
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ImageView picture;
			TextView name;

			if (v == null) {
				v = inflater.inflate(R.layout.item, parent, false);
				v.setTag(R.id.picture, v.findViewById(R.id.picture));
				v.setTag(R.id.text, v.findViewById(R.id.text));
			}
			picture = (ImageView) v.getTag(R.id.picture);
			name = (TextView) v.getTag(R.id.text);
			Item item = (Item) getItem(position);
			picture.setImageResource(item.drawableId);
			name.setText(item.name);

			return v;
		}

	}

	/**
	 * Klasse, die ein einzelnes Item definiert
	 * 
	 * @author Tobias Straub, Susi Roos, Thomas Lesinski, Matthias Feichtmeier,
	 *         Veronika Kinzel, Elena Fedorow
	 * @version 1.0
	 * 
	 */
	private class Item {
		final String name;
		final int drawableId;

		Item(String name, int drawableId) {
			this.name = name;
			this.drawableId = drawableId;
		}
	}

}
