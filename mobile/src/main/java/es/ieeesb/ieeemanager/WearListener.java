package es.ieeesb.ieeemanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.prefs.PreferenceChangeEvent;

import es.ieeesb.ieeemanager.model.DoorUser;
import es.ieeesb.ieeemanager.tasks.PostOpen;

public class WearListener extends WearableListenerService {
    public static final String OPEN_DOOR_PLS="/open";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Toast.makeText(this,"Recibida orden de android wear",Toast.LENGTH_SHORT).show();
        Log.d("blabla","Recibido hoygan");
        if (messageEvent.getPath().equals(OPEN_DOOR_PLS)){
            DoorUser doorUser= new DoorUser(PreferenceManager.getDefaultSharedPreferences(this));
            PostOpen task=new PostOpen(this);
            task.execute(doorUser.getName(), doorUser.getDni(), doorUser.getToken());
        }
    }
}
