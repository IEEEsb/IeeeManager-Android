package es.ieeesb.ieeemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ieeedooropener.R;

import es.ieeesb.ieeemanager.adapters.SettingsAdapter;
import es.ieeesb.ieeemanager.model.DoorUser;
import es.ieeesb.ieeemanager.model.Slic3rUser;

public class LoginActivity extends Activity {

	public static final String Tag = "Opener";
	private DoorUser doorUser;
	private Slic3rUser slic3rUser;
	private ListView cardList;
	private SettingsAdapter adapter;
	boolean initiated = false;
	boolean registered=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.login);
		generateUsers();
		initiateUI();
	}
	
	public void registerDoor(View v) {
		int cardIndex = getIndexOfParent(v);
		String name = ((EditText) cardList.getChildAt(cardIndex).findViewById(
				R.id.name)).getText().toString();
		String dni = ((EditText) cardList.getChildAt(cardIndex).findViewById(
				R.id.dni)).getText().toString();
		String registrationId = ((EditText) cardList.getChildAt(cardIndex)
				.findViewById(R.id.registrationId)).getText().toString();
		doorUser.setName(name);
		doorUser.setDni(dni);
		doorUser.setRegisterId(registrationId);
		doorUser.persist(PreferenceManager.getDefaultSharedPreferences(this));
		PostRegister task = new PostRegister(cardList.getChildAt(cardIndex)
				.findViewById(R.id.registerButton));
		task.execute(name, dni, registrationId);

	}
	
	public void saveEmail(View v){
		EditText emailField=(EditText) findViewById(R.id.emailField);
		String email = emailField.getText().toString();
		if(email!=null&&!email.equals("")){
			slic3rUser.setEmail(email);
			slic3rUser.persist(PreferenceManager.getDefaultSharedPreferences(this));
		}
		Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void generateUsers() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		doorUser = new DoorUser(preferences);
		slic3rUser=new Slic3rUser(preferences);
	}

	public void initiateUI() {
		cardList = (ListView) findViewById(R.id.settingsCardList);
		adapter = new SettingsAdapter(this, R.layout.settings_user_data_card_unregistered);
		if (initiated) {
			adapter.disableAnimations();
		}
		adapter.add(doorUser);
		adapter.add(slic3rUser);
		cardList.setAdapter(adapter);
		initiated = true;
	}

	public void logOff(View v) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Editor ed = PreferenceManager.getDefaultSharedPreferences(
							LoginActivity.this).edit();
					ed.putString(MainActivity.KEY_TOKEN, "");
					ed.commit();
					doorUser.setToken("");
					initiateUI();
					// dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// dialog.dismiss();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Estás seguro?")
				.setPositiveButton("Sí", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		adapter.resetAnimations();
		adapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onStop() {
		initiated = false;
		super.onStop();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	private void animateCards() {
		for (int i = 0; i < cardList.getCount(); i++) {
			adapter.animateCard(i, cardList.getChildAt(i));
		}
	}

	private int getIndexOfParent(View v) {
		for (int i = 0; i < cardList.getChildCount(); i++) {
			if (cardList.getChildAt(i).findViewById(v.getId()) != null)
				return i;
		}
		return 0;
	}
	
	public class PostRegister extends AsyncTask<String, Void, String> {
		private View registerButton;

		public PostRegister(View b) {
			this.registerButton = b;
		}

		@Override
		protected void onPreExecute() {
			registerButton.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String token = "";
			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						3);
				nameValuePairs.add(new BasicNameValuePair("Name", params[0]));
				nameValuePairs.add(new BasicNameValuePair("DNI", params[1]));
				nameValuePairs.add(new BasicNameValuePair("RegID", params[2]));

				URL url = new URL(MainActivity.URL_SERVER_DOOR);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setReadTimeout(30000);
				conn.setConnectTimeout(50000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(MainActivity.getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				int responseCode = conn.getResponseCode();
				BufferedReader in;
				if (responseCode == 404)
					in = new BufferedReader(new InputStreamReader(
							conn.getErrorStream()));
				else
					in = new BufferedReader(new InputStreamReader(
							conn.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				String responseS = response.toString();
				if (responseCode != 404) {
					token = responseS;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return token;
		}

		@Override
		protected void onPostExecute(String result) {
			registered = result.length() > 0;
			if (registered) {
				Editor edit = PreferenceManager.getDefaultSharedPreferences(
						LoginActivity.this).edit();
				edit.putString(MainActivity.KEY_TOKEN, result);
				edit.commit();
				doorUser.setToken(result);
				adapter.notifyDataSetChanged();
			}
			String text = registered ? "Registrado" : "Nope";
			Toast t = Toast
					.makeText(LoginActivity.this, text, Toast.LENGTH_LONG);
			t.show();
			registerButton.setEnabled(true);
			super.onPostExecute(result);
		}

	}


}
