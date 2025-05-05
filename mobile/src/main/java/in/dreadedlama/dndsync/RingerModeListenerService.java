package in.dreadedlama.dndsync;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class RingerModeListenerService extends BroadcastReceiver {
    private static final String TAG = "RingerModeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean syncSilentDnd = prefs.getBoolean("silent_as_dnd_key", false);
        if(!syncSilentDnd) {
            Log.d(TAG, "Sync silent DND is disabled");
            return;
        }
        if (!AudioManager.RINGER_MODE_CHANGED_ACTION.equals(intent.getAction())) return;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        Log.d(TAG, "Ringer mode changed to: " + ringerMode);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                Log.d(TAG, "Setting DND mode (INTERRUPTION_FILTER_NONE) due to silent mode");
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            } else {
                Log.d(TAG, "Cannot set DND - Notification policy access not granted");
            }
        } else {
            if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                Log.d(TAG, "Setting DND mode (INTERRUPTION_FILTER_ALL) due to normal mode");
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            } else {
                Log.d(TAG, "Cannot set DND - Notification policy access not granted");
            }
        }
    }
}
