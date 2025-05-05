package in.dreadedlama.dndsync;


import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class DNDNotificationService extends NotificationListenerService {
    private static final String TAG = "DNDNotificationService";
    private static final String DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync";

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "listener connected");
    }


    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "listener disconnected");
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
