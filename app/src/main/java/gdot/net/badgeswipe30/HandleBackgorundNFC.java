package gdot.net.badgeswipe30;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by Todd on 5/2/2017.
 */

public class HandleBackgorundNFC extends BadgeSwipe3 implements UpdateUIInterface
{
    NfcAdapter nfcAdapter;
    WifiManager wifiManager;
    Context context;
    AudioManager audioManager;

    private static final int BRIGHTNESS_LEVEL = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_swipe3);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        context=getApplicationContext();
        audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter != null){
            handleIntent(getIntent());
        }
        else {
            Toast.makeText(this, "NFC not available on this device", Toast.LENGTH_LONG).show();
        }
    }

    private void handleIntent(Intent intent){

        String action = intent.getAction();
        if(nfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
            String type = intent.getType();
            if(type.equals("text/plain")){
                Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(tag);
            }
            else{
                //error
            }
        }

    }

    protected void onResume() {
        super.onResume();
        setupForeGroundDispatch(this,nfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }



    @Override
    protected void onPause() {
        stopForegroundDispatch(this,nfcAdapter);
        super.onPause();
    }

    private void stopForegroundDispatch(BadgeSwipe3 badgeSwipe3, NfcAdapter nfcAdapter) {
        nfcAdapter.disableForegroundDispatch(badgeSwipe3);
    }

    private void setupForeGroundDispatch(Activity activity, NfcAdapter nfcAdapter) {
        final Intent intent = new Intent(activity.getApplicationContext(),activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0,intent,0);
        IntentFilter[] filters = new IntentFilter[1];
        String [][] techlist = new String[][]{};
        filters [0] = new IntentFilter();
        filters [0].addAction(nfcAdapter.ACTION_NDEF_DISCOVERED);
        filters [0].addCategory(Intent.CATEGORY_DEFAULT);
        try{
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        nfcAdapter.enableForegroundDispatch(activity,pendingIntent,filters,techlist);


    }

    @Override
    public void updateUI(String string) {
        int brightness = -1;
        try {
            brightness= Settings.System.getInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (brightness == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
            Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,BRIGHTNESS_LEVEL);

        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }

        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }
}
