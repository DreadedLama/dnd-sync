package in.dreadedlama.dndsync;

import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private RingerModeListenerService ringerModeListenerService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Registering RingerModeReceiver");
        ringerModeListenerService = new RingerModeListenerService();
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerModeListenerService, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringerModeListenerService != null) {
            Log.d("MainActivity", "Unregistering RingerModeReceiver");
            unregisterReceiver(ringerModeListenerService);
        }
    }
}
