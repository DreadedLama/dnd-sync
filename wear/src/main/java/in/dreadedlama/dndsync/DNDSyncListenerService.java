package in.dreadedlama.dndsync;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

public class DNDSyncListenerService extends WearableListenerService {
    private static final String TAG = "DNDSyncListenerService";
    public static final String SAMSUNG = "Samsung";

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        for (DataEvent dataEvent : dataEventBuffer) {

            // No need to filter by path, it is defined in the manifest
            // Android will make sure we only get our own messages

            boolean vibrate = prefs.getBoolean("vibrate_key", false);
            Log.d(TAG, "vibrate: " + vibrate);
            if (vibrate) {
                vibrate();
            }

            byte dndStatePhone = getPhoneDndState(dataEvent);
            Log.d(TAG, "dndStatePhone: " + dndStatePhone);

            // get dnd state
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int filterState = mNotificationManager.getCurrentInterruptionFilter();
            if (filterState < 0 || filterState > 4) {
                Log.d(TAG, "DNDSync weird current dnd state: " + filterState);
            }
            byte currentDndState = (byte) filterState;
            Log.d(TAG, "currentDndState: " + currentDndState);

            if(dndStatePhone == 5 || dndStatePhone ==6) {
                boolean useBedtimeMode = prefs.getBoolean("bedtime_key", true);
                Log.d(TAG, "useBedtimeMode: " + useBedtimeMode);
                if (useBedtimeMode) {
//                    String deviceName = android.os.Build.MODEL; // returns model name
                    String deviceManufacturer = android.os.Build.MANUFACTURER; // returns manufacturer
                    int bedTimeModeValue = (dndStatePhone ==5)?1:0;
                    boolean usePowerSaverMode = prefs.getBoolean("power_saver_key", true);
                    if(usePowerSaverMode) {
                        boolean lowPower = Settings.Global.putInt(
                                getApplicationContext().getContentResolver(), "low_power", bedTimeModeValue);
                        boolean restrictedDevicePerformance = Settings.Global.putInt(
                                getApplicationContext().getContentResolver(), "restricted_device_performance", bedTimeModeValue);
                        boolean lowPowerBackDataOff = Settings.Global.putInt(
                                getApplicationContext().getContentResolver(), "low_power_back_data_off", bedTimeModeValue);
                        boolean smConnectivityDisable = Settings.Secure.putInt(
                                getApplicationContext().getContentResolver(), "sm_connectivity_disable", bedTimeModeValue);
                        if(lowPower && restrictedDevicePerformance && lowPowerBackDataOff && smConnectivityDisable) {
                            Log.d(TAG, "Power Saver mode toggled");
                        } else {
                            Log.d(TAG, "Power Saver mode toggle failed");
                        }
                    }

                    boolean samsungBedtimeModeSuccess = false;
                    if(deviceManufacturer.equalsIgnoreCase(SAMSUNG)) {
                        samsungBedtimeModeSuccess = Settings.Global.putInt(
                                getApplicationContext().getContentResolver(), "setting_bedtime_mode_running_state", bedTimeModeValue);
                    }
                    boolean bedtimeModeSuccess = Settings.Global.putInt(
                        getApplicationContext().getContentResolver(), "bedtime_mode", bedTimeModeValue);
                    boolean zenModeSuccess = Settings.Global.putInt(
                            getApplicationContext().getContentResolver(), "zen_mode", bedTimeModeValue);
                    if (deviceManufacturer.equalsIgnoreCase(SAMSUNG) && bedtimeModeSuccess && samsungBedtimeModeSuccess && zenModeSuccess) {
                        new android.os.Handler(getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName(
                                    "com.google.android.apps.wearable.settings",
                                    "com.samsung.android.clockwork.settings.advanced.bedtimemode.StBedtimeModeReservedActivity"
                            ));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Log.d(TAG, "Bedtime mode activity started after 4-second delay");
                        }, 35   00); // delay in milliseconds
                        Log.d(TAG, "Bedtime mode value toggled");
                    } else if (!deviceManufacturer.equalsIgnoreCase(SAMSUNG) && bedtimeModeSuccess && zenModeSuccess) {
                        Log.d(TAG, "Bedtime mode value toggled");
                    } else {
                        Log.d(TAG, "Bedtime mode toggle failed");
                    }

                }
            }

            if ((dndStatePhone != currentDndState) && (dndStatePhone !=5 && dndStatePhone !=6)) {
                Log.d(TAG, "dndStatePhone != currentDndState: " + dndStatePhone + " != " + currentDndState);
                // set DND anyway, also in case bedtime toggle does not work to have at least DND
                if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                    mNotificationManager.setInterruptionFilter(dndStatePhone);
                    Log.d(TAG, "DND set to " + dndStatePhone);
                } else {
                    Log.d(TAG, "attempting to set DND but access not granted");
                }
            }
        }
    }

    private static byte getPhoneDndState(DataEvent dataEvent) {
        byte[] data = dataEvent.getDataItem().getData();
        // data[0] contains dnd mode of phone
        // 0 = INTERRUPTION_FILTER_UNKNOWN
        // 1 = INTERRUPTION_FILTER_ALL (all notifications pass)
        // 2 = INTERRUPTION_FILTER_PRIORITY
        // 3 = INTERRUPTION_FILTER_NONE (no notification passes)
        // 4 = INTERRUPTION_FILTER_ALARMS
        // Custom
        // 5 = BedTime Mode On
        // 6 = BedTime Mode Off
        return data[0];
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
    }

}
