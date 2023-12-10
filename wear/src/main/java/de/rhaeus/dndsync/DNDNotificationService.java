package de.rhaeus.dndsync;


import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class DNDNotificationService extends NotificationListenerService {
    private static final String TAG = "DNDNotificationService";
    private static final String DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync";

    public static boolean running = false;

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "listener connected");
        running = true;

        //TODO enable/disable service based on app setting to save battery
//        // We don't want to run a background service so disable and stop it
//        // to avoid running this service in the background
//        disableServiceComponent();
//        Log.i(TAG, "Disabling service");
//
//        try {
//            stopSelf();
//        } catch(SecurityException e) {
//            Log.e(TAG, "Failed to stop service");
//        }
    }
//    private void disableServiceComponent() {
//        PackageManager p = getPackageManager();
//        ComponentName componentName = new ComponentName(this, DNDNotificationService.class);
//        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "listener disconnected");
        running = false;
    }


    @Override
    public void onInterruptionFilterChanged (int interruptionFilter) {
        Log.d(TAG, "interruption filter changed to " + interruptionFilter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncDnd = prefs.getBoolean("dnd_sync_key", true);
        if(syncDnd) {
            new Thread(new Runnable() {
                public void run() {
                    sendDNDSync(interruptionFilter);
                }
            }).start();
        }
    }

    private void sendDNDSync(int dndState) {
        // https://developer.android.com/training/wearables/data/data-items
        Wearable.getDataClient(this)
                .putDataItem(PutDataRequest.create(DND_SYNC_MESSAGE_PATH)
                        .setData(new byte[]{(byte) dndState, 0})
                        // mark urgent, otherwise it could take up to 30 minutes to sync
                        .setUrgent()
                );
    }
}
