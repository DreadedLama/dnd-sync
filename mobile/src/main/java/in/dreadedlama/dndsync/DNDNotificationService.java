package in.dreadedlama.dndsync;


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
        if(isDigitalWellBeingWindDownNotification(sbn)) {
            onNotificationAddedCallDNDSync(sbn);
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // if notifications is removed, disable bedtime mode
        if(isDigitalWellBeingWindDownNotification(sbn)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean syncBedTime = prefs.getBoolean("bedtime_sync_key", true);

            if (syncBedTime) {
                // 6 means bedtime OFF
                Log.d(TAG, "bedtime mode is off");
                int interruptionFilter = 6;
                new Thread(() -> sendDNDSync(interruptionFilter)).start();
            }
        }
    }

    private boolean isDigitalWellBeingWindDownNotification(StatusBarNotification sbn) {
        return sbn.getPackageName().equals("com.google.android.apps.wellbeing") &&
                sbn.getNotification().getChannelId().equals("wind_down_notifications");
    }

    private void onNotificationAddedCallDNDSync(StatusBarNotification sbn) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncBedTime = prefs.getBoolean("bedtime_sync_key", true);
        if(syncBedTime) {

            // depending on the number of actions in digital wellbeing notification
            // bedtime mode could be in "pause mode" or "on mode":
            // * If it is in "pause" mode, there is only one action ("Restart bedtime")
            // * If it is in "on" mode, there are two actions possible ("Pause it" and "De-activate it")
            boolean bedTimeModeIsOn = sbn.getNotification().actions.length == 2;
            boolean bedTimeModeIsPaused = sbn.getNotification().actions.length == 1;

            if (bedTimeModeIsOn) {
                // 5 means bedtime ON
                Log.d(TAG, "bedtime mode is on");
                int interruptionFilter = 5;
                new Thread(() -> sendDNDSync(interruptionFilter)).start();
            } else if (bedTimeModeIsPaused) {
                // 6 means bedtime OFF
                Log.d(TAG, "bedtime mode is off");
                int interruptionFilter = 6;
                new Thread(() -> sendDNDSync(interruptionFilter)).start();
            }
        }
    }

    @Override
    public void onInterruptionFilterChanged (int interruptionFilter) {
        Log.d(TAG, "interruption filter changed to " + interruptionFilter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncDnd = prefs.getBoolean("dnd_sync_key", true);
        if(syncDnd) {
            new Thread(() -> sendDNDSync(interruptionFilter)).start();
        }
    }

    private void sendDNDSync(int dndState) {
        Wearable.getDataClient(this)
                .putDataItem(PutDataRequest.create(DND_SYNC_MESSAGE_PATH)
                        .setData(new byte[]{(byte) dndState, 0})
                        // mark urgent, otherwise it could take up to 30 minutes to sync
                        .setUrgent()
                );
    }
}
