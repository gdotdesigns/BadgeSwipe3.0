package gdot.net.badgeswipe30;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Todd on 4/18/2017.
 */

public class NdefReaderTask extends AsyncTask<Tag,Void,String> {

    UpdateUIInterface updateUIInterface;


    public NdefReaderTask(Activity activity){
        updateUIInterface = (UpdateUIInterface) activity;
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            //do something
            if(result == "Work Profile"){
                updateUIInterface.updateUI(result);
            }
        }
        super.onPostExecute(result);
    }



    @Override
    protected String doInBackground(Tag... params) {

        Tag tag = params[0];
        Ndef ndef = Ndef.get(tag);
        if(ndef==null){
            return null;
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord:records ){
            if(ndefRecord.getTnf()==NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(),NdefRecord.RTD_TEXT)){
                try{
                    return readText(ndefRecord);
                }
                catch (UnsupportedEncodingException e){
                    //error
                }
            }
        }
        return null;
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException{
        byte[] payload = record.getPayload();
        String textEncoding = (((payload[0] & 128) == 0)) ? "UTF-8" : "UTF-16";
        int launguageCodeLength = payload[0] & 0063;
        return  new String(payload,launguageCodeLength+1,payload.length-launguageCodeLength-1,textEncoding);
    }
}
