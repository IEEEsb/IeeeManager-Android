package es.ieeesb.ieeemanager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;



import java.util.Collection;
import java.util.HashSet;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks{
    public static final String TAG="ieeesbmanager wear";
    public static final String OPEN_DOOR_PLS="/open";
    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
       initGoogleApiClient();


    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void sendOpenDoorMessage(String nodeId) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, nodeId, OPEN_DOOR_PLS, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG,"sending");
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }else{
                            Log.d(TAG,"sent");
                        }
                        MainActivity.this.finish();
                    }
                }
        );
    }
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"connected",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Connected: ");
        GetNodesTask task=new GetNodesTask();
        task.execute();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    private class GetNodesTask extends AsyncTask<Void,Void,Collection<String>>{

        @Override
        protected Collection<String> doInBackground(Void... params) {
           return getNodes();
        }

        @Override
        protected void onPostExecute(Collection<String> nodes) {
            super.onPostExecute(nodes);
            for(String s:nodes){
                sendOpenDoorMessage(s);
            }
        }
    }
}
