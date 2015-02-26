package es.ieeesb.ieeemanager.model;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import es.ieeesb.ieeemanager.MainActivity;

public class LatchUser implements User {
	private String name;
	private String dni;
	private String pairingToken;
	private boolean paired;
	
	public LatchUser(String nombre, String dni) {
		super();
		this.name = nombre;
		this.dni = dni;
		this.pairingToken="";
		this.setPaired(false);
	}
	

	public LatchUser(SharedPreferences preferences) {
		super();
		this.name = preferences.getString(MainActivity.KEY_NAME, "");
		this.dni = preferences.getString(MainActivity.KEY_DNI, "");
		this.pairingToken = preferences.getString(MainActivity.KEY_PAIRING_TOKEN, "");
		this.paired=preferences.getBoolean(MainActivity.KEY_PAIRED, false);
	}
	
	public String getDni() {
		return dni;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
	public String getName() {
		return name;
	}
	public void setName(String nombre) {
		this.name = nombre;
	}
	
	public void persist(SharedPreferences prefs){
		Editor ed=prefs.edit();
		ed.putString(MainActivity.KEY_NAME, name);
		ed.putString(MainActivity.KEY_DNI, dni);
		ed.putString(MainActivity.KEY_PAIRING_TOKEN,pairingToken);
		ed.putBoolean(MainActivity.KEY_PAIRED, paired);
		ed.commit();
	}


	public String getPairingToken() {
		return pairingToken;
	}


	public void setPairingToken(String pairingToken) {
		this.pairingToken = pairingToken;
	}


	public boolean isPaired() {
		return paired;
	}


	public void setPaired(boolean paired) {
		this.paired = paired;
	}

}
