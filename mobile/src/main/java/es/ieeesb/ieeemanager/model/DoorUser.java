package es.ieeesb.ieeemanager.model;

import es.ieeesb.ieeemanager.MainActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DoorUser implements User{
	private String name;
	private String dni;
	private String registerId;
	private String token;
	
	public DoorUser(String nombre, String dni, String registerId) {
		super();
		this.name = nombre;
		this.dni = dni;
		this.registerId = registerId;
		this.token="";
	}
	
	public DoorUser(String nombre, String dni, String registerId,String token) {
		super();
		this.name = nombre;
		this.dni = dni;
		this.registerId = registerId;
		this.token=token;
	}
	
	public DoorUser(SharedPreferences preferences) {
		super();
		this.name = preferences.getString(MainActivity.KEY_NAME, "");
		this.dni = preferences.getString(MainActivity.KEY_DNI, "");
		this.registerId = preferences.getString(MainActivity.KEY_REGID, "");
		this.token = preferences.getString(MainActivity.KEY_TOKEN, "");
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
	public String getRegisterId() {
		return registerId;
	}
	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public void persist(SharedPreferences prefs){
		Editor ed=prefs.edit();
		ed.putString(MainActivity.KEY_NAME, name);
		ed.putString(MainActivity.KEY_DNI, dni);
		ed.putString(MainActivity.KEY_REGID, registerId);
		ed.putString(MainActivity.KEY_TOKEN, token);
		ed.commit();
	}
	
	
}
