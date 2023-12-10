package de.rhaeus.dndsync;


import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
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
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "listener disconnected");
        running = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        onNotificationAddedOrRemovedCallDNDSync(sbn,5);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        onNotificationAddedOrRemovedCallDNDSync(sbn,6);
    }

    private void onNotificationAddedOrRemovedCallDNDSync(StatusBarNotification sbn, int interruptionFilter) {
        if(sbn.getPackageName().equals("com.google.android.apps.wellbeing")) {
            String title = sbn.getNotification().extras.getString("android.title");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean syncBedTime = prefs.getBoolean("bedtime_sync_key", true);
            if(syncBedTime && (title.contains("on") || title.contains("paused"))) {
                int updatedInterruptionFilter;
                //BedTime
                if (title.contains("paused")) {
                    updatedInterruptionFilter = (interruptionFilter == 5) ? 6 : 5;
                } else {
                    updatedInterruptionFilter = interruptionFilter;
                }
                new Thread(() -> sendDNDSync(updatedInterruptionFilter)).start();
            }
        }
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
