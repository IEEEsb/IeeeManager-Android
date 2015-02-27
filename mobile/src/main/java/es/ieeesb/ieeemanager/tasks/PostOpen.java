package es.ieeesb.ieeemanager.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static es.ieeesb.ieeemanager.MainActivity.*;


    public class PostOpen extends AsyncTask<String, Void, Boolean> {
        private View openButton;
        private Context ctx;

        public PostOpen(Context ctx,View b) {
            this.openButton = b;
            this.ctx=ctx;
        }
        public PostOpen(Context ctx) {
            this.ctx=ctx;
        }

        @Override
        protected void onPreExecute() {
            if(openButton!=null)
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
            Toast t = Toast.makeText(ctx, text,
                    Toast.LENGTH_SHORT);
            t.show();
            if(openButton!=null)
            openButton.setEnabled(true);
            super.onPostExecute(result);
        }

    }

