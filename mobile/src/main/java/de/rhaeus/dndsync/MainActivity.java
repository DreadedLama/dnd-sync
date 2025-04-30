package de.rhaeus.dndsync;

import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private RingerModeReceiver ringerModeReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Registering RingerModeReceiver");
        ringerModeReceiver = new RingerModeReceiver();
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerModeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringerModeReceiver != null) {
            Log.d("MainActivity", "Unregistering RingerModeReceiver");
            unregisterReceiver(ringerModeReceiver);
        }
    }
}
