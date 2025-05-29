package in.dreadedlama.dndsync;


import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import in.dreadedlama.dndsync.shared.WearSignal;

public class DNDNotificationService extends NotificationListenerService {
    private static final String TAG = "DNDNotificationService";
    private static final String DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync";


    @Override
    public void onInterruptionFilterChanged (int interruptionFilter) {
        Log.d(TAG, "interruption filter changed to " + interruptionFilter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncDnd = prefs.getBoolean("dnd_sync_key", true);
        if(syncDnd) {
            new Thread(() -> sendDNDSync(new WearSignal(interruptionFilter))).start();
        }
    }

    private void sendDNDSync(WearSignal wearSignal) {
        int dndState = wearSignal.dndState;
        Wearable.getDataClient(this)
                .putDataItem(PutDataRequest.create(DND_SYNC_MESSAGE_PATH)
                        .setData(new byte[]{(byte) dndState, 0})
                        // mark urgent, otherwise it could take up to 30 minutes to sync
                        .setUrgent()
                );
    }
}
