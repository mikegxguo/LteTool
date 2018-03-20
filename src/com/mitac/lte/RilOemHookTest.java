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
        //private UsbControl usbcon;
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
		setContentView(R.layout.riloemhook_layout);
		mRadioButtonAPI1 = (RadioButton) findViewById(R.id.radio_api1);
		mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
		// Initially turn on first button.
		mRadioButtonAPI1.toggle();
		// Get our main phone object.
		mPhone = PhoneFactory.getDefaultPhone();
		// Register for OEM raw notification.
		// mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW,
		// null);
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
                //usbcon = new UsbControl();
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
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE);
			mPhone.invokeOemRilRequestRaw(oemhook, msg);
			CmdRespText.setText("");
		} else {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = {
                                ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r'};
                        //String[] oemhookstring = new String[2];
                        //oemhookstring[0] = ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r';
                        //oemhookstring[1] = "AT\r";

                        log("############################# oemhookstring: "+oemhookstring[0]);
			// Create message
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			CmdRespText = (EditText) findViewById(R.id.edit_response);
			CmdRespText.setText("---Wait response---");
		}
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

        public void onLogUSIO(View view) {
                if(curstate == 0) {
                    String[] oemhookstring = { "AT+USIO=0" + '\r' };
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
            //    String[] oemhookstring = { "AT+USYSTRACE=0,\"bb_sw=1\",\"bb_sw=sdl:th,tr,st,pr,mo,lt,db,li,gt,ae|fts:sdl(gprs,umts)|lte_stk:0x01,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF|lte_stk:0x02,0x801FFFFF|ims:1\",\"oct=4\",921600" + '\r' }; //LARA R211
            //AT+TRACE=1,460800,,,,,"CDC-ACM"
            String[] oemhookstring = { "AT+TRACE=1,460800,,,,,\"CDC-ACM\"" + '\r' };//TOBY-L2
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
                }
        }

        public void onLogStop(View view) {
                if(curstate==3) {
                    SystemProperties.set("gsm.ulog", "stop");
                    curstate = 4;
                    rilhandler.removeCallbacks(rillog);
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
		            String[] oemhookstring = { "AT+TRACE=0" + '\r' };//TOBY-L2
            //        String[] oemhookstring = { "AT+USYSTRACE=0" + '\r' }; //LARA R211
		    // Create message
		    Message msg = mHandler
				.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
		    // Send request
		    mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
		    CmdRespText = (EditText) findViewById(R.id.edit_response);
		    CmdRespText.setText("---Wait response---");
                    curstate = 5;
                }
	}

        public void onDisableDataWkup(View view) {
            //Message msg = mHandler.obtainMessage(EVENT_RIL_SET_URING);
            //mPhone.setUring(0, msg);
        }
        public void onEnableDataWkup(View view) {
            //Message msg = mHandler.obtainMessage(EVENT_RIL_SET_URING);
            //mPhone.setUring(2, msg);
        }

        public void onDisconnect3G(View view) {
            //usbcon.SetHostPortPower(0xD); //1-1.2
            //usbcon.SetHostPortPower(0xB); //1-1.3
            //for(int i=0; i<500; i++){
            //    AutoTest();
            //}
        }


        public void onConnect3G(View view) {
            log("onConnect3G");

            //usbcon.SetHostPortPower(0xF);
            //MyThread t1 = new MyThread("AutoTest");
            //t1.start();
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
/*
        public void AutoTest() {
            usbcon.SetHostPortPower(0xD);
            Reset3GPower(1);
            usbcon.SetHostPortPower(0xF);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	class MyThread extends Thread {
	    MyThread(String s) {
	        super(s);
	    }
	    public void run() {
	        for(int i = 1; i <= 500; i++) {
		    try {
                        usbcon.SetHostPortPower(0xD);
                        sleep(1000);
                        Reset3GPower(1);
                        sleep(3000);
                        usbcon.SetHostPortPower(0xF);
                        sleep(10000);
		    }catch (InterruptedException e) {
		        e.printStackTrace();
		    }
	        }
            }
        }
*/
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
