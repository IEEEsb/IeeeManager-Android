package es.ieeesb.ieeemanager.model;

import es.ieeesb.ieeemanager.MainActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class FridgeUser implements User {
	private String name;
	private String dni;
	private float budget;

	public FridgeUser(SharedPreferences preferences) {
		this.name = preferences.getString(MainActivity.KEY_NAME, "");
		this.dni = preferences.getString(MainActivity.KEY_DNI, "");
		this.budget = preferences.getFloat(MainActivity.KEY_BUDGET,
				Float.NEGATIVE_INFINITY);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDni() {
		return dni;
	}

	public float getBudget() {
		return this.budget;
	}

	public void setBudget(float budget) {
		if (budget > Float.NEGATIVE_INFINITY)
			this.budget = budget;
	}

	public void persist(SharedPreferences prefs) {
		Editor ed = prefs.edit();
		ed.putString(MainActivity.KEY_NAME, name);
		ed.putString(MainActivity.KEY_DNI, dni);
		ed.putFloat(MainActivity.KEY_BUDGET, budget);
		ed.commit();

	}

}
