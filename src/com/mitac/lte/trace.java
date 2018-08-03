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
import java.io.File;
import java.io.FileNotFoundException;


public class trace extends Activity {
    private static final String TAG = "TRACE";
    private Phone mPhone = null;
    private EditText CmdRespText = null;
    private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
    private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
    private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;
    private static final int EVENT_RIL_SET_URING = 700;
    private static int curstate = 0;
    private Handler rilhandler = new Handler();
    private Runnable rillog = new Runnable() {
        public void run() {
            if(curstate==3) {
                SystemProperties.set("gsm.ulog", "stop");
                curstate = 4;
                rilhandler.removeCallbacks(rillog);
                rilhandler.postDelayed(rillog, 5000);
            }
            else if(curstate==2 || curstate==4) {
                SystemProperties.set("gsm.ulog.service.init", "start");
                curstate = 3;
                rilhandler.removeCallbacks(rillog);
                rilhandler.postDelayed(rillog, 1800000);
            }
        }
    };

    @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            setContentView(R.layout.trace_layout);

            // Initially turn on first button.
            // Get our main phone object.
            mPhone = PhoneFactory.getDefaultPhone();
            // Register for OEM raw notification.
            // mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
            //stop trace log
            SystemProperties.set("gsm.ulog", "stop");
            curstate = 0;
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
            // mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW,
            // null);
        }


    public void onLogInit(View view) {
        if(curstate == 0) {
            (new File("/mnt/sdcard/ulog")).mkdirs();

            String[] oemhookstring = { "AT" + '\r' };
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Wait response---");
            curstate = 1;
        }
    }

    public void onLogEnabled(View view) {
        if(curstate==1 || curstate ==5) {
            //String[] oemhookstring = { "AT+TRACE=1,921600" + '\r' };//LISA U200
            String[] oemhookstring = { "AT+USYSTRACE=0,\"bb_sw=1\",\"bb_sw=sdl:th,tr,st,pr,mo,lt,db,li,gt,ae|fts:sdl(gprs,umts)|lte_stk:0x01,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF|lte_stk:0x02,0x801FFFFF|ims:1\",\"oct=4\",921600" + '\r' }; //LARA R211
            //AT+TRACE=1,460800,,,,,"CDC-ACM"
            //String[] oemhookstring = { "AT+TRACE=1,460800,,,,,\"CDC-ACM\"" + '\r' };//TOBY-L2
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Wait response---");
            curstate = 2;
        }
    }

    public void onLogStart(View view) {
        if(curstate==2 || curstate==4) {
            SystemProperties.set("gsm.ulog.service.init", "start");
            curstate = 3;
            //Toast.makeText(trace.this, "Start ulog service", Toast.LENGTH_LONG).show();
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Start ulog service---");
            return ;
        }
    }

    public void onLogStop(View view) {
        if(curstate==3) {
            SystemProperties.set("gsm.ulog", "stop");
            curstate = 4;
            rilhandler.removeCallbacks(rillog);
            //Toast.makeText(trace.this, "Stop ulog service", Toast.LENGTH_LONG).show();
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Stop ulog service---");
            return ;
        }
    }

    public void onLogAuto(View view) {
        if(curstate==3) {
            SystemProperties.set("gsm.ulog", "stop");
            curstate = 4;
            rilhandler.removeCallbacks(rillog);
            rilhandler.postDelayed(rillog, 5000);
        }
        else if(curstate==2 || curstate==4) {
            SystemProperties.set("gsm.ulog.service.init", "start");
            curstate = 3;
            rilhandler.removeCallbacks(rillog);
            rilhandler.postDelayed(rillog, 1800000);
        }
    }

    public void onLogDisabled(View view) {
        if(curstate == 4) {
            //String[] oemhookstring = { "AT+TRACE=0" + '\r' };//TOBY-L2
            String[] oemhookstring = { "AT+USYSTRACE=0" + '\r' }; //LARA R211
            // Create message
            Message msg = mHandler
                .obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
            // Send request
            mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
            CmdRespText = (EditText) findViewById(R.id.edit_response);
            CmdRespText.setText("---Wait response---");
            curstate = 0;
        }
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
        Log.d(TAG, "trace: " + msg);
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
