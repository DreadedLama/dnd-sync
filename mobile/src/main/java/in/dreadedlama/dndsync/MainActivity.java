package in.dreadedlama.dndsync;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

//    private RingerModeListenerService ringerModeListenerService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Log.d("MainActivity", "Registering RingerModeReceiver");
//        ringerModeListenerService = new RingerModeListenerService();
//        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
//        registerReceiver(ringerModeListenerService, filter);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (ringerModeListenerService != null) {
//            Log.d("MainActivity", "Unregistering RingerModeReceiver");
//            unregisterReceiver(ringerModeListenerService);
//        }
//    }
}
