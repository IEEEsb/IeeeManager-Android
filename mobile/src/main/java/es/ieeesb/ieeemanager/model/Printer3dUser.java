package es.ieeesb.ieeemanager.model;

import android.content.SharedPreferences;
import es.ieeesb.ieeemanager.MainActivity;

public class Printer3dUser implements User {
	private String name;
	private String dni;
	private String status;
	private String file;
	private int progress;
	private int time;
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public Printer3dUser(SharedPreferences preferences) {
		this.name = preferences.getString(MainActivity.KEY_NAME, "");
		this.dni = preferences.getString(MainActivity.KEY_DNI, "");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDni() {
		return dni;
	}


}
