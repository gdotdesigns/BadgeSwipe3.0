package gdot.net.badgeswipe30;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class BadgeSwipe3 extends AppCompatActivity implements UpdateUIInterface {

    NfcAdapter nfcAdapter;
    TextView textView;
    WifiManager wifiManager;
    Context context;

    @Override
    protected void onResume() {
        super.onResume();
        setupForeGroundDispatch(this,nfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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
    protected void onPause() {
        stopForegroundDispatch(this,nfcAdapter);
        super.onPause();

    }

    private void stopForegroundDispatch(BadgeSwipe3 badgeSwipe3, NfcAdapter nfcAdapter) {
        nfcAdapter.disableForegroundDispatch(badgeSwipe3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_swipe3);
        textView = (TextView) findViewById(R.id.text);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        context=getApplicationContext();

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

    @Override
    public void updateUI(String string) {
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,255);
        textView.setText(string);
    }
}
