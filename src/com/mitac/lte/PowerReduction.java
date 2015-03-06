package com.mitac.lte;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.view.View.OnKeyListener;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncResult;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.app.AlertDialog;
import android.os.UEventObserver;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.SystemClock;
import android.view.WindowManager;
import android.os.PowerManager;

public class PowerReduction extends Activity  implements TextWatcher{	
    private static final String LOG_TAG = "PowerReduction";
    private TextView mPSensor = null;
    private EditText mGPRS_Profile;
    private EditText mEDGE_Profile;
    private EditText m3G;
    private EditText m2G_850;
    private EditText m2G_900;
    private EditText m2G_1800;
    private EditText m2G_1900;
    private Button mButtonRun;
    private EditText mRun;
	private Phone mPhone = null;
	private EditText CmdRespText = null;
	private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
	private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;

        private static boolean bGPRSProfile;
        private static boolean bEDGEProfile;
        private static boolean b3G;
        private static boolean b2G_850;
        private static boolean b2G_900;
        private static boolean b2G_1800;
        private static boolean b2G_1900;

        private  static String strGPRSProfile;
        private  static String strEDGEProfile;
        private  static String str3G;
        private  static String str2G_850;
        private  static String str2G_900;
        private  static String str2G_1800;
        private  static String str2G_1900;
        private  static String strProperty;
    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private long curTimeout;

    private static boolean bSarExist = false;
    private static final int REFRESH = 0x1234;
    private static boolean bRunSet = false;
    private Handler hRefresh = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case REFRESH:
                if(mPSensor==null) mPSensor = (TextView)findViewById(R.id.psensor);
		strProperty=SystemProperties.get("ril.lte.txpower.reduce", "image");
                if(bSarExist) {
		    if(mPSensor!=null) mPSensor.setText("Person is detected!!!");
		    if(bRunSet == false) {
			bRunSet = true;
		        if(strProperty.equals("tool")) 	onSet();
		    }
                    SystemProperties.set("ril.psensor.event.active", "1");
                } else {
                    if(mPSensor!=null) mPSensor.setText("Person is out!!!");
		    if(bRunSet == true) {
			bRunSet = false;
                        if(strProperty.equals("tool"))  onRestore();
		    }
                    SystemProperties.set("ril.psensor.event.active", "0");
                }
                break;
                default: break;
            }
        }
    };

    private UEventObserver mWwanObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) { //wakesource=sar
            boolean waked = "sar".equals(event.get("wakesource")) ? true : false;
            if (waked) {
                if("detect".equals(event.get("status")))
                    bSarExist = true;
                else
                    bSarExist = false;
            }
            hRefresh.sendEmptyMessage(REFRESH);
        }
    };

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
                    //simulate click event
                    if(mRun == null) mRun = (EditText) findViewById(R.id.edit_GPRS_profile);
                    mRun.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,mRun.getLeft()+5,mRun.getTop()+5, 0));
                    mRun.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,mRun.getLeft()+5, mRun.getTop()+5, 0)); 

		    handler.postDelayed(this, 15000);//1700000);
		}
	};

    PowerManager.WakeLock wakeLock;
    private void acquireWakeLock() {
         if (wakeLock ==null) {
                log("Acquiring wake lock");
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
                wakeLock.acquire();
            }
    }
    private void releaseWakeLock() {
        if (wakeLock !=null&& wakeLock.isHeld()) {
            log("Release wake lock");
            wakeLock.release();
            wakeLock =null;
        }
    }

    private void holdScreenOffTimeout() {
                ContentResolver resolver = getContentResolver();
                curTimeout = Settings.System.getLong(resolver, SCREEN_OFF_TIMEOUT,
                    FALLBACK_SCREEN_TIMEOUT_VALUE);
                try {
                    Settings.System.putInt(resolver, SCREEN_OFF_TIMEOUT,-1);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "could not persist screen timeout setting", e);
                }
   }

   private void releaseScreenOffTimeout() {
                try {
                    ContentResolver resolver = getContentResolver();
                    Settings.System.putInt(resolver, SCREEN_OFF_TIMEOUT,(int)curTimeout);
                } catch (NumberFormatException e) {
                     Log.e(LOG_TAG, "could not persist screen timeout setting", e);
                }
   }


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.power_control);
                mPSensor = (TextView)findViewById(R.id.psensor);
		mGPRS_Profile = (EditText)findViewById(R.id.edit_GPRS_profile);
		mEDGE_Profile = (EditText)findViewById(R.id.edit_EDGE_profile);
		m3G = (EditText)findViewById(R.id.edit_3G);
		m2G_850 = (EditText)findViewById(R.id.edit_2G_850);
		m2G_900 = (EditText) findViewById(R.id.edit_2G_900);
		m2G_1800 = (EditText) findViewById(R.id.edit_2G_1800);
		m2G_1900 = (EditText) findViewById(R.id.edit_2G_1900);
		mButtonRun = (Button) findViewById(R.id.run);

		mGPRS_Profile.addTextChangedListener(this);
        mEDGE_Profile.addTextChangedListener(this);
        m3G.addTextChangedListener(this);
        m2G_850.addTextChangedListener(this);
        m2G_900.addTextChangedListener(this);
        m2G_1800.addTextChangedListener(this);
        m2G_1900.addTextChangedListener(this);
        mWwanObserver.startObserving("SUBSYSTEM=platform");
        
        mButtonRun.setEnabled(false);
        
        // Get our main phone object.
        mPhone = PhoneFactory.getDefaultPhone();
	bRunSet = false;
        acquireWakeLock();
        //holdScreenOffTimeout();
		// Register for OEM raw notification.
		// mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
		
		// Capture text edit key press
//		CmdRespText = (EditText) findViewById(R.id.edit_cmdstr);
//		CmdRespText.setOnKeyListener(new OnKeyListener() {
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// If the event is a key-down event on the "enter" button
//				if ((event.getAction() == KeyEvent.ACTION_DOWN)
//						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
//					// Perform action on key press
//					Toast.makeText(PowerReduction.this, CmdRespText.getText(),
//							Toast.LENGTH_SHORT).show();
//					return true;
//				}
//				return false;
//			}
//		});
            SystemProperties.set("ril.psensor.event.pop", "1");
	}

	@Override
	public void onPause() {
		super.onPause();
		log("onPause()");
		// Unregister for OEM raw notification.
		// mPhone.mCM.unSetOnUnsolOemHookRaw(mHandler);

                //handler.removeCallbacks(runnable);
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onResume() {
		super.onResume();
		log("onResume()");
		// Register for OEM raw notification.
		// mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW,
		// null);

                //handler.postDelayed(runnable, 1000);
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

        @Override
        public void onStop() {
                super.onStop();
                log("onStop()");
		finish();
		
                SystemProperties.set("ril.lte.txpower.reduce", "image");

	        bGPRSProfile = false;
        	bEDGEProfile = false;
	        b3G = false;
        	b2G_850 = false;
	        b2G_900 = false;
        	b2G_1800 = false;
	        b2G_1900 = false;

                releaseWakeLock();
		//releaseScreenOffTimeout();

		//System.exit(0);
                onRestore(); //Niel requested it, 2014/05/14
        }


    public void afterTextChanged(Editable s) {
        validateFields();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public static boolean requiredFieldValid(TextView view) {
        return view.getText() != null && view.getText().length() > 0;
    }
    /*
     * TODO disabled this method globally. It is used in all the settings screens but I just
     * noticed that an unrelated icon was dimmed. Android must share drawables internally.
     */
    public static void setCompoundDrawablesAlpha(TextView view, int alpha) {
//        Drawable[] drawables = view.getCompoundDrawables();
//        for (Drawable drawable : drawables) {
//            if (drawable != null) {
//                drawable.setAlpha(alpha);
//            }
//        }
    }

    private void validateFields() {
        boolean valid = requiredFieldValid(mGPRS_Profile)
                && requiredFieldValid(mEDGE_Profile)
                && requiredFieldValid(m3G)
                && requiredFieldValid(m2G_850)
                && requiredFieldValid(m2G_900)
                && requiredFieldValid(m2G_1800)
                && requiredFieldValid(m2G_1900);
                //&& mEmailValidator.isValid(mEmailView.getText().toString().trim());
        mButtonRun.setEnabled(valid);
        /*
         * Dim the next button's icon to 50% if the button is disabled.
         * TODO this can probably be done with a stateful drawable. Check into it.
         * android:state_enabled
         */
        setCompoundDrawablesAlpha(mButtonRun, mButtonRun.isEnabled() ? 255 : 128);
    }

    private boolean checkValue(EditText edit, int min, int max) {
        boolean isValid = true;
        try {
            String item = edit.getText().toString().trim();
            int n = Integer.parseInt(item);
            log("item: "+item+" num:"+n+" min:"+min+" max:"+max);
            if(n>max || n<min) isValid = false;
       } catch (Exception e) {
            return false;
       }
       return isValid;
    }

    public void onRun(View view) {
        log("************* Save **********");

        bGPRSProfile = checkValue(mGPRS_Profile, 0, 3);
        bEDGEProfile = checkValue(mEDGE_Profile, 0, 3);
        b3G = checkValue(m3G, 0, 32);
        b2G_850 = checkValue(m2G_850, 0, 48);
        b2G_900 = checkValue(m2G_900, 0, 48);
        b2G_1800 = checkValue(m2G_1800, 0, 48);
        b2G_1900 = checkValue(m2G_1900, 0, 48);

	if (bGPRSProfile && bEDGEProfile) {
        	strGPRSProfile = mGPRS_Profile.getText().toString().trim();
	        strEDGEProfile = mEDGE_Profile.getText().toString().trim();
                log("Gprs: "+strGPRSProfile+" EDGE: "+strEDGEProfile);
	}

	if (b3G && b2G_850 && b2G_900 && b2G_1800 && b2G_1900) {
	        str3G = m3G.getText().toString().trim();
        	str2G_850 = m2G_850.getText().toString().trim();
	        str2G_900 = m2G_900.getText().toString().trim();
        	str2G_1800 = m2G_1800.getText().toString().trim();
	        str2G_1900 = m2G_1900.getText().toString().trim();
                log("3G:"+str3G+" 2G_850:"+str2G_850);
                log("2G_900:"+str2G_900+" 2G_1800:"+str2G_1800+" 2G_1900:"+str2G_1900);
	}

        SystemProperties.set("ril.lte.txpower.reduce", "tool");
        onSet(); //Niel requested it, 2014/05/14
    }
    public void onSet() {
	// AT command
        // Get the response field
        log("************* onSet **********");
		CmdRespText = (EditText) findViewById(R.id.edit_response);
                log("Gprs: "+bGPRSProfile+" EDGE: "+bEDGEProfile);

		if (bGPRSProfile && bEDGEProfile) {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { "AT+UDCONF=40,"
					+strGPRSProfile
					+','
					+strEDGEProfile
					+'\r' };
			log("AT command: "+oemhookstring[0]);
			// Create message
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			CmdRespText = (EditText) findViewById(R.id.edit_response);
			CmdRespText.setText("---Wait response---");
		}
		log("b3G:"+b3G+" b2G_850:"+b2G_850);
                log("b2G_900:"+b2G_900+" b2G_1800:"+b2G_1800+" b2G_1900:"+b2G_1900);
		if (b3G && b2G_850 && b2G_900 && b2G_1800 && b2G_1900) {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { "AT+UMAXPWR="
					+str3G     +','
					+str2G_850 +','
					+str2G_900 +','
					+str2G_1800+','
					+str2G_1900
					+'\r' };
			log("AT command: "+oemhookstring[0]);
			// Create message
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			CmdRespText = (EditText) findViewById(R.id.edit_response);
			CmdRespText.setText("---Wait response---");
		}
	}
    public void onRestore() {
	// AT command
        // Get the response field
        log("************* onRestore **********");
		CmdRespText = (EditText) findViewById(R.id.edit_response);
                log("Gprs: "+bGPRSProfile+" EDGE: "+bEDGEProfile);

		if (bGPRSProfile && bEDGEProfile) {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { "AT+UDCONF=40,"
					+"0" 
					+','
					+"0" 
					+'\r' };
			log("AT command: "+oemhookstring[0]);
			// Create message
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			CmdRespText = (EditText) findViewById(R.id.edit_response);
			CmdRespText.setText("---Wait response---");
		}
		log("b3G:"+b3G+" b2G_850:"+b2G_850);
                log("b2G_900:"+b2G_900+" b2G_1800:"+b2G_1800+" b2G_1900:"+b2G_1900);
		if (b3G && b2G_850 && b2G_900 && b2G_1800 && b2G_1900) {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { "AT+UMAXPWR="
					+"0" +','
					+"0" +','
					+"0" +','
					+"0" +','
					+"0" +'\r' };
			log("AT command: "+oemhookstring[0]);
			// Create message
			Message msg = mHandler
					.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			CmdRespText = (EditText) findViewById(R.id.edit_response);
			CmdRespText.setText("---Wait response---");
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
			}
		}
	};
}
