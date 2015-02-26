package es.ieeesb.ieeemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ieeedooropener.R;

import es.ieeesb.ieeemanager.adapters.CardAdapter;
import es.ieeesb.ieeemanager.model.DoorUser;
import es.ieeesb.ieeemanager.model.FridgeUser;
import es.ieeesb.ieeemanager.model.LatchUser;
import es.ieeesb.ieeemanager.model.Printer3dUser;
import es.ieeesb.ieeemanager.model.Slic3rUser;
import es.ieeesb.ieeemanager.tools.UriResolver;

public class MainActivity extends Activity {
	public static final String KEY_NAME = "name";
	public static final String KEY_DNI = "dni";
	public static final String KEY_REGID = "regid";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_BUDGET = "budget";
	public static final String KEY_PAIRING_TOKEN = "pairing_token";
	public static final String KEY_PAIRED = "paired";
	public static final String KEY_SLT_PATH = "stl_path";
	public static final String KEY_EMAIL = "email";
	public static final String URL_SERVER_DOOR = "http://rgnu.ieeesb.etsit.upm.es/door";
	public static final String URL_SERVER_FRIDGE = "http://rgnu.ieeesb.etsit.upm.es/fridge";
	public static final String URL_SERVER_PRINTER3d = "http://rgnu.ieeesb.etsit.upm.es/3dprinter";
	public static final String URL_SERVER_LATCH_PAIR = "http://rgnu.ieeesb.etsit.upm.es/latchPair";
	public static final String URL_SERVER_LATCH_UNPAIR = "http://rgnu.ieeesb.etsit.upm.es/latchUnpair";
	public static final String URL_SERVER_SLIC3R = "http://rgnu.ieeesb.etsit.upm.es/slic3r"; //
	public static final String URL_SERVER_SLIC3R_PROFILES = "http://rgnu.ieeesb.etsit.upm.es/slic3rProfiles";
	public static final int REQUEST_LOGGED = 0;
	public static final int REQUEST_STL = 1;

	public static final String Tag = "Opener";

	private boolean registered = false;
	private DoorUser doorUser;
	private FridgeUser fridgeUser;
	private Printer3dUser printer3dUser;
	private LatchUser latchUser;
	private Slic3rUser slic3rUser;
	private ListView cardList;
	private CardAdapter adapter;
	boolean initiated = false;
	private boolean awaitingResult = false;
	private AlertDialog alert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		generateUsers();
		registered = doorUser.getToken().length() > 0;
		if (registered)
			initiateUI();
		else
			showLogin();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			showLogin();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void generateUsers() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		doorUser = new DoorUser(preferences);
		fridgeUser = new FridgeUser(preferences);
		printer3dUser = new Printer3dUser(preferences);
		latchUser = new LatchUser(preferences);
		slic3rUser = new Slic3rUser(preferences);
	}

	private void showLogin() {
		awaitingResult = true;
		Intent i = new Intent(this, LoginActivity.class);
		startActivityForResult(i, REQUEST_LOGGED);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		awaitingResult = false;
		switch (requestCode) {
		case REQUEST_LOGGED:
			generateUsers();
			registered = doorUser.getToken().length() > 0;
			if (registered)
				initiateUI();
			else {
				findViewById(R.id.emptyList).setVisibility(View.VISIBLE);
			}
			break;
		case REQUEST_STL:
			if (resultCode == RESULT_OK) {
				slic3rUser
						.setStlPath(UriResolver.getPath(this, data.getData()));
				Button b = (Button) findViewById(R.id.stlUploadButton);
				b.setText("Sube el archivo");
			} else {
				Toast t = Toast
						.makeText(
								this,
								"Necesitas un gestor de archivos para seleccionar el STL",
								Toast.LENGTH_LONG);
				t.show();
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void initiateUI() {
		// getBudget();
		// getPrinterData();
		cardList = (ListView) findViewById(R.id.cardList);
		adapter = new CardAdapter(this,
				R.layout.settings_user_data_card_unregistered);
		adapter.add(doorUser);
		adapter.add(fridgeUser);
		adapter.add(printer3dUser);
		adapter.add(latchUser);
		adapter.add(slic3rUser);
		if (initiated) {
			adapter.disableAnimations();
		}
		cardList.setAdapter(adapter);
		initiated = true;
		findViewById(R.id.emptyList).setVisibility(View.INVISIBLE);
	}

	private void getPrinterData(int delay) {
		if (!doorUser.getToken().equals("")) {
			QueryPrinterData tsk = new QueryPrinterData(delay);
			tsk.execute();
		}
	}

	private void getPrinterData() {
		if (!doorUser.getToken().equals("")) {
			QueryPrinterData tsk = new QueryPrinterData();
			tsk.execute();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (!awaitingResult)
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

	private void hideCards() {
		for (int i = 0; i < cardList.getCount(); i++) {
			cardList.getChildAt(i).setVisibility(View.INVISIBLE);
		}
	}

	public void getBudget(int delay) {
		if (!doorUser.getToken().equals("")) {
			PostBudget tsk = new PostBudget(delay);
			tsk.execute(fridgeUser.getName(), fridgeUser.getDni());
		}
	}

	public void getBudget() {
		if (!doorUser.getToken().equals("")) {
			PostBudget tsk = new PostBudget();
			tsk.execute(fridgeUser.getName(), fridgeUser.getDni());
		}
	}

	public void getPrinterData(View v) {
		if (!doorUser.getToken().equals("")) {
			getPrinterData();
		}
	}

	public void getBudget(View v) {
		if (!doorUser.getToken().equals("")) {
			PostBudget tsk = new PostBudget(v);
			tsk.execute(fridgeUser.getName(), fridgeUser.getDni());
		}
	}

	public void openDoor(View v) {
		PostOpen task = new PostOpen(cardList.getChildAt(getIndexOfParent(v))
				.findViewById(R.id.openButton));
		task.execute(doorUser.getName(), doorUser.getDni(), doorUser.getToken());
	}

	public void unpairClicked(View v) {
		PostLatch tsk = new PostLatch(v, false);
		tsk.execute(latchUser.getName(), latchUser.getDni(),
				doorUser.getToken());

	}

	public void pairClicked(View v) {
		String pairingToken = ((EditText) findViewById(R.id.pairingTokenField))
				.getText().toString().trim();
		PostLatch tsk = new PostLatch(v, true);

		tsk.execute(latchUser.getName(), latchUser.getDni(), pairingToken,
				doorUser.getToken());

	}

	public void selectStlClicked(View v) {
		if (slic3rUser.getStlPath() == null) {
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.setType("file/*");
			startActivityForResult(i, REQUEST_STL);
		} else {
			PostSlic3r tsk = new PostSlic3r(v, PostSlic3r.TYPE_PROFILES_REQUEST);
			tsk.execute();
		}
	}

	private int getIndexOfParent(View v) {
		for (int i = 0; i < cardList.getChildCount(); i++) {
			if (cardList.getChildAt(i).findViewById(v.getId()) != null)
				return i;
		}
		return 0;
	}

	public static String getQuery(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	private String getStringFromIS(InputStream content) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(content));
		String inputLine;
		StringBuffer responseSB = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			responseSB.append(inputLine);
		}
		in.close();
		return responseSB.toString();
	}

	private HttpURLConnection createConnection(URL url, String type)
			throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(30000);
		conn.setConnectTimeout(50000);
		conn.setRequestMethod(type);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		return conn;
	}

	private void showDialogSlic3rProfile(String[] options, View toDisable) {
		final String[] choices = options;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Elige el perfil de Slic3r");
		final View button = toDisable;
		builder.setSingleChoiceItems(choices, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendStl(choices[which], button);
						alert.dismiss();
					}
				});
		alert = builder.create();
		alert.show();
	}

	private void sendStl(String profile, View toDisable) {
		String email = slic3rUser.getEmail();
		if (email != null && !email.equals("")) {
			PostSlic3r tsk = new PostSlic3r(toDisable, PostSlic3r.TYPE_STL_SEND);
			tsk.execute(slic3rUser.getName(), slic3rUser.getDni(),
					doorUser.getToken(), email, profile,
					slic3rUser.getStlPath());
		} else {
			Toast.makeText(this, "Introduce tu email en ajustes", Toast.LENGTH_LONG).show();
		}
	}

	public class PostOpen extends AsyncTask<String, Void, Boolean> {
		private View openButton;

		public PostOpen(View b) {
			this.openButton = b;
		}

		@Override
		protected void onPreExecute() {
			openButton.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			boolean abierta = false;
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						3);
				nameValuePairs.add(new BasicNameValuePair("Name", params[0]));
				nameValuePairs.add(new BasicNameValuePair("DNI", params[1]));
				nameValuePairs.add(new BasicNameValuePair("Token", params[2]));
				URL url = new URL(URL_SERVER_DOOR);
				HttpURLConnection conn = createConnection(url, "POST");
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode != 404) {
					abierta = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return abierta;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			String text = result ? "Abierta" : "Nope";
			Toast t = Toast.makeText(MainActivity.this, text,
					Toast.LENGTH_SHORT);
			t.show();
			openButton.setEnabled(true);
			super.onPostExecute(result);
		}

	}

	public class PostBudget extends AsyncTask<String, Void, Float> {
		private View reloadButton;
		private int delay = 0;

		public PostBudget(View b) {
			this.reloadButton = b;
		}

		public PostBudget(int delay) {
			this.delay = delay;
		}

		public PostBudget() {
			this.delay = 0;
		}

		@Override
		protected void onPreExecute() {
			if (reloadButton != null)
				reloadButton.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected Float doInBackground(String... params) {
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			float budget = Float.NEGATIVE_INFINITY;
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						3);
				nameValuePairs.add(new BasicNameValuePair("Name", params[0]));
				nameValuePairs.add(new BasicNameValuePair("DNI", params[1]));

				URL url = new URL(URL_SERVER_FRIDGE);
				HttpURLConnection conn = createConnection(url, "POST");

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode != 404) {
					try {
						budget = Float.parseFloat(getStringFromIS(conn
								.getInputStream()));
					} catch (Exception e) {
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return budget;
		}

		@Override
		protected void onPostExecute(Float result) {
			if (reloadButton != null)
				reloadButton.setEnabled(true);
			float lastBudget = fridgeUser.getBudget();
			if (lastBudget != result.floatValue()) {
				fridgeUser.setBudget(result);
				fridgeUser.persist(PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this));
				adapter.notifyDataSetChanged();
			}
			super.onPostExecute(result);
		}

	}

	public class QueryPrinterData extends AsyncTask<String, Void, String> {
		private View reloadButton;
		private int delay = Integer.MIN_VALUE;

		public QueryPrinterData(View b) {
			this.reloadButton = b;
		}

		public QueryPrinterData(int delay) {
			this.delay = delay;
		}

		public QueryPrinterData() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onPreExecute() {
			if (reloadButton != null)
				reloadButton.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String printerData = "";
			try {
				URL url = new URL(URL_SERVER_PRINTER3d);
				HttpURLConnection conn = createConnection(url, "POST");
				conn.setUseCaches(false);
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode != 404) {
					try {
						printerData = getStringFromIS(conn.getInputStream());
					} catch (Exception e) {
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return printerData;
		}

		@Override
		protected void onPostExecute(String result) {
			if (reloadButton != null)
				reloadButton.setEnabled(true);
			fillPrinter3dUser(result, printer3dUser);
			adapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

	}

	public void fillPrinter3dUser(String result, Printer3dUser user) {
		try {
			JSONObject obj = new JSONObject(result);
			user.setStatus(obj.getString("state"));
			user.setFile(obj.getJSONObject("job").getJSONObject("file")
					.getString("name"));
			user.setProgress((int) (obj.getJSONObject("progress")
					.getDouble("completion")));
			user.setTime(obj.getJSONObject("progress").getInt("printTimeLeft"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class PostLatch extends AsyncTask<String, Void, Boolean> {
		private View reloadButton;
		private boolean pair;
		private String pairingToken;

		public PostLatch(View b, boolean pair) {
			this.reloadButton = b;
			this.pair = pair;
		}

		@Override
		protected void onPreExecute() {
			if (reloadButton != null)
				reloadButton.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean accepted = false;
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						3);
				if (pair) {
					nameValuePairs
							.add(new BasicNameValuePair("Name", params[0]));
					nameValuePairs
							.add(new BasicNameValuePair("DNI", params[1]));
					nameValuePairs.add(new BasicNameValuePair("PairingToken",
							params[2]));
					this.pairingToken = params[2];
					nameValuePairs.add(new BasicNameValuePair("Token",
							params[3]));
				} else {
					nameValuePairs
							.add(new BasicNameValuePair("Name", params[0]));
					nameValuePairs
							.add(new BasicNameValuePair("DNI", params[1]));
					nameValuePairs.add(new BasicNameValuePair("Token",
							params[2]));
				}
				URL url;
				if (pair)
					url = new URL(URL_SERVER_LATCH_PAIR);
				else
					url = new URL(URL_SERVER_LATCH_UNPAIR);
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
				writer.write(getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode != 404) {
					accepted = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return accepted;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (reloadButton != null)
				reloadButton.setEnabled(true);

			Toast t;
			if (result) {
				t = Toast.makeText(MainActivity.this, "Success",
						Toast.LENGTH_SHORT);
				if (pair) {
					latchUser.setPairingToken(pairingToken);
					latchUser.setPaired(true);
				} else {
					latchUser.setPairingToken("");
					latchUser.setPaired(false);
				}
				latchUser.persist(PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this));
				adapter.notifyDataSetChanged();
			} else {
				t = Toast.makeText(MainActivity.this, "Nope",
						Toast.LENGTH_SHORT);
			}
			t.show();
			super.onPostExecute(result);
		}
	}

	public class PostSlic3r extends AsyncTask<String, Void, String[]> {

		public static final int TYPE_PROFILES_REQUEST = 0;
		public static final int TYPE_STL_SEND = 1;
		public static final String SENT_OK = "sentok";
		public static final String SENT_FAIL = "sentfail";

		private boolean send = false;
		private View v;

		public PostSlic3r(View v, int type) {
			send = type == TYPE_STL_SEND;
			this.v = v;
		}

		@Override
		protected void onPreExecute() {
			if (v != null)
				this.v.setEnabled(false);
			super.onPreExecute();
		}

		@Override
		protected String[] doInBackground(String... params) {
			String[] resultArray = new String[1];
			resultArray[0] = SENT_FAIL;
			DefaultHttpClient client = new DefaultHttpClient();
			try {
				if (send) {
					client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
							0, false));
					HttpPost post = new HttpPost(URL_SERVER_SLIC3R);
					MultipartEntityBuilder builder = MultipartEntityBuilder
							.create();
					builder.setMode(HttpMultipartMode.STRICT);
					builder.addPart("Name", new StringBody(params[0],
							ContentType.TEXT_PLAIN));
					builder.addPart("DNI", new StringBody(params[1],
							ContentType.TEXT_PLAIN));
					builder.addPart("Token", new StringBody(params[2],
							ContentType.TEXT_PLAIN));
					builder.addPart("Email", new StringBody(params[3],
							ContentType.TEXT_PLAIN));
					builder.addPart("Profile", new StringBody(params[4],
							ContentType.TEXT_PLAIN));
					File toSend = new File(params[5]);
					builder.addPart("attachment",
							new FileBody(toSend,
									ContentType.APPLICATION_OCTET_STREAM,
									toSend.getName()));

					post.setEntity(builder.build());
					HttpResponse response = client.execute(post);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != 404)
						resultArray[0] = SENT_OK;
				} else {
					HttpGet get = new HttpGet(URL_SERVER_SLIC3R_PROFILES);
					HttpResponse response = client.execute(get);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != 404) {
						resultArray = getStringFromIS(
								response.getEntity().getContent()).split(",");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return resultArray;
		}

		@Override
		protected void onPostExecute(String[] result) {
			if (send) {
				Toast t = null;
				String text = result[0].equals(SENT_OK) ? "Success sending file"
						: "Error sending file";
				t = Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
				t.show();
				slic3rUser.setStlPath(null);
				Button b = (Button) findViewById(R.id.stlUploadButton);
				b.setText(R.string.stlSelectButton);
			} else {
				if (result.length > 0) {
					showDialogSlic3rProfile(result, v);
				} else {
					Toast t = Toast.makeText(MainActivity.this, "??????",
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
			if (v != null)
				v.setEnabled(true);
			super.onPostExecute(result);
		}
	}
}
