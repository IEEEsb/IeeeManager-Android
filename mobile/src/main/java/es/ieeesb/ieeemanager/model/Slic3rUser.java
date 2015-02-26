package es.ieeesb.ieeemanager.model;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import es.ieeesb.ieeemanager.MainActivity;

public class Slic3rUser implements User {
	private String name;
	private String dni;
	private String stlPath;
	private String email;

	public Slic3rUser(SharedPreferences preferences) {
		this.name = preferences.getString(MainActivity.KEY_NAME, "");
		this.dni = preferences.getString(MainActivity.KEY_DNI, "");
		this.setEmail(preferences.getString(MainActivity.KEY_EMAIL, ""));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDni() {
		return dni;
	}

	public String getStlPath() {
		return stlPath;
	}

	public void setStlPath(String stlPath) {
		this.stlPath = stlPath;
	}

	public void persist(SharedPreferences prefs) {
		Editor ed = prefs.edit();
		ed.putString(MainActivity.KEY_NAME, name);
		ed.putString(MainActivity.KEY_DNI, dni);
		ed.putString(MainActivity.KEY_EMAIL, getEmail());
		ed.commit();

	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
