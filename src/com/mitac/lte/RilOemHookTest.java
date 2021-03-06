package com.mitac.lte;

//import com.mitac.android.common.UsbControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.lang.*;


public class RilOemHookTest extends Activity {
    private static final String LOG_TAG = "RILOemHookTestApp";
    private RadioButton mRadioButtonAPI1 = null;
    private RadioGroup mRadioGroupAPI = null;
    private Phone mPhone = null;
    private EditText CmdRespText = null;
    private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
    private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;
    private static final int EVENT_RIL_SET_URING = 700;
    private static int curstate = 0;

    @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            setContentView(R.layout.riloemhook_layout);
            mRadioButtonAPI1 = (RadioButton) findViewById(R.id.radio_api1);
            mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
            // Initially turn on first button.
            mRadioButtonAPI1.toggle();
            // Get our main phone object.
            mPhone = PhoneFactory.getDefaultPhone();
            // Register for OEM raw notification.
            // mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
            // Capture text edit key press
            CmdRespText = (EditText) findViewById(R.id.edit_cmdstr);
            CmdRespText.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Toast.makeText(RilOemHookTest.this, CmdRespText.getText(),
                        Toast.LENGTH_SHORT).show();
                    return true;
                    }
                    return false;
                    }
                    });
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

    public void onRun(View view) {
        // Get the checked button
        int idButtonChecked = mRadioGroupAPI.getCheckedRadioButtonId();
        // Get the response field
        CmdRespText = (EditText) findViewById(R.id.edit_response);
        byte[] oemhook = null;
        switch (idButtonChecked) {
            case R.id.radio_api1:
                oemhook = new byte[1];
                oemhook[0] = (byte) 0xAA;
                break;
            case R.id.radio_api2:
                oemhook = new byte[2];
                oemhook[0] = (byte) 0xBB;
                oemhook[1] = (byte) 0x55;
                break;
            case R.id.radio_api3:
                // Send OEM notification (just echo the data bytes)
                oemhook = new byte[7];
                oemhook[0] = (byte) 0xCC;
                oemhook[1] = (byte) 0x12;
                oemhook[2] = (byte) 0x34;
                oemhook[3] = (byte) 0x56;
                oemhook[4] = (byte) 0x78;
                oemhook[5] = (byte) 0x9A;
                oemhook[6] = (byte) 0xBC;
                break;
            case R.id.radio_api4:
                // Send OEM command string
                break;
            default:
                log("unknown button selected");
                break;
        }
        if (idButtonChecked != R.id.radio_api4) {
            //Message msg = mHandler
            //		.obtainMessage(EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE);
            //mPhone.invokeOemRilRequestRaw(oemhook, msg);
            //CmdRespText.setText("");
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Not support yet---");
        } else {
            // Copy string from EditText and add carriage return
            String[] oemhookstring = {
                ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r'};
            //String[] oemhookstring = new String[2];
            //oemhookstring[0] = ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r';
            //oemhookstring[1] = "AT\r";

            log("############################# oemhookstring: "+oemhookstring[0]);
            CharSequence strAT="AT";
            if(oemhookstring[0].contains(strAT)) {
                // Create message
                Message msg = mHandler
                    .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
                // Send request
                mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
                CmdRespText = (EditText) findViewById(R.id.edit_response);
                CmdRespText.setText("---Wait response---");
            } else {
                CmdRespText = (EditText) findViewById(R.id.edit_response);
                CmdRespText.setText("---Not support yet---");
            }
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
        Log.d(LOG_TAG, "[RIL_HOOK_OEM_TESTAPP] " + msg);
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
