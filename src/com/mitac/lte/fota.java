package com.mitac.lte;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncResult;
import android.util.Log;
import android.app.AlertDialog;
import android.os.SystemProperties;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;


public class fota extends Activity {
    private static final String TAG = "FOTA";
    private Phone mPhone = null;
    private EditText CmdRespText = null;
    private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
    private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;
    private static final int EVENT_RIL_SET_URING = 700;

    @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            setContentView(R.layout.fota_layout);
            // Get our main phone object.
            mPhone = PhoneFactory.getDefaultPhone();
            // Register for OEM raw notification.
            // mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
        }

    @Override
        public void onPause() {
            super.onPause();
            log("onPause()");
            // Unregister for OEM raw notification.
            // mPhone.mCM.unSetOnUnsolOemHookRaw(mHandler);
        }

    @Override
        public void onResume() {
            super.onResume();
            log("onResume()");
            // Register for OEM raw notification.
            // mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
        }

    public void onFTPConnect(View view) {
        if(true) {
            String[] oemhookstring = { "AT+UFTP=0,\"221.224.29.28\"\r" };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("AT+UFTP=0,\"221.224.29.28\"\n---Wait response---");
        }
    }

    public void onFTPUser(View view) {
        if(true) {
            String[] oemhookstring = { "AT+UFTP=2,\"MKL-IA-PE\"\r" };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("AT+UFTP=2,\"MKL-IA-PE\"\n---Wait response---");
        }
    }

    public void onFTPPassword(View view) {
        if(true) {
            String[] oemhookstring = { "AT+UFTP=3,\"pe@exchange\"\r" };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("AT+UFTP=3,\"pe@exchange\"\n---Wait response---");
        }
    }

    public void onFTPCLogin(View view) {
        if(true) {
            String[] oemhookstring = { "AT+UFTPC=1" + '\r' };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("AT+UFTPC=1\n---Wait response---");
        }
    }

    public void onFTPCRetrieve(View view) {
        if(true) {
            String[] oemhookstring = { "AT+UFTPC=4,\"Temp/NTAU1_v1707_5001.md5\",\"NTAU1_v1707_5001.md5\"" + '\r' };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("AT+UFTPC=4,\"Temp/NTAU1_v1707_5001.md5\",\"NTAU1_v1707_5001.md5\"\n---Wait response---");
        }
    }

    public int Reset3GPower(int enable) {
        int result = 0;
        try {
            FileWriter fw = new FileWriter("/sys/module/lte_power/parameters/lte_enable");
            fw.write(enable);
            fw.flush();
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
            log("Reset power of 3G module, IOException error");
            result = -1;
        }
        return result;
    }

    private void logRilOemHookResponse(AsyncResult ar) {
        log("received oem hook response");
        String str = new String("");
        if (ar.exception != null) {
            log("Exception:" + ar.exception);
            str += "Exception:" + ar.exception + "\n\n";
        }
        if (ar.result != null) {
            byte[] oemResponse = (byte[]) ar.result;
            int size = oemResponse.length;
            log("oemResponse length=[" + Integer.toString(size) + "]");
            str += "oemResponse length=[" + Integer.toString(size) + "]" + "\n";
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    byte myByte = oemResponse[i];
                    int myInt = (int) (myByte & 0xFF);
                    log("oemResponse[" + Integer.toString(i) + "]=[0x"
                            + Integer.toString(myInt, 16) + "]");
                    str += "oemResponse[" + Integer.toString(i) + "]=[0x"
                        + Integer.toString(myInt, 16) + "]" + "\n";
                }
            }
        } else {
            log("received NULL oem hook response");
            str += "received NULL oem hook response";
        }
        // Display message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str);
        builder.setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void logRilOemHookResponseString(AsyncResult ar) {
        log("received oem hook string response");
        String str = new String("");
        CmdRespText = (EditText) findViewById(R.id.edit_response);
        if (ar.exception != null) {
            log("Exception:" + ar.exception);
            str += "Exception:" + ar.exception + "\n\n";
        }
        if (ar.result != null) {
            String[] oemStrResponse = (String[]) ar.result;
            int sizeStr = oemStrResponse.length;
            log("oemResponseString[0] [" + oemStrResponse[0] + "]");
            CmdRespText.setText("" + oemStrResponse[0]);
        } else {
            log("received NULL oem hook response");
            CmdRespText.setText("No response or error received");
        }
    }

    private void log(String msg) {
        Log.d(TAG, "[RIL_HOOK_OEM_TESTAPP] " + msg);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE:
                    log("EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE");
                    ar = (AsyncResult) msg.obj;
                    logRilOemHookResponse(ar);
                    break;
                case EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE:
                    log("EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE");
                    ar = (AsyncResult) msg.obj;
                    logRilOemHookResponseString(ar);
                    break;
                case EVENT_UNSOL_RIL_OEM_HOOK_RAW:
                    break;
                case EVENT_UNSOL_RIL_OEM_HOOK_STR:
                    break;
                case EVENT_RIL_SET_URING:
                    break;
            }
        }
    };
}
