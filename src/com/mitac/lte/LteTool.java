package com.mitac.lte;

//import java.util.List;


import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
import android.os.PowerManager;
import android.util.Log;
import android.content.Context;

public class LteTool extends PreferenceActivity {
//	private Button mBtnTesting;
//	private Button mBtnPower;

    private static final String LOG_TAG = "PowerReduction";
    private PowerManager.WakeLock wakeLock;
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

        private void log(String msg) {
                Log.d(LOG_TAG, "[RIL_HOOK_OEM_TESTAPP] " + msg);
        }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        addPreferencesFromResource(R.layout.main);
        //acquireWakeLock();        
//        mBtnTesting = (Button)findViewById(R.id.btnTesting);
//        mBtnPower = (Button)findViewById(R.id.btnPower);
//        
//        mBtnTesting.setOnClickListener(new Button.OnClickListener()
//        {
//        	public void onClick(View v)
//        	{
//        		try
//        		{
//        			Intent intent = new Intent(Intent.ACTION_VIEW);  
//        			intent.setClassName("com.android.settings",
//        			        "com.android.settings.TestingSettings");
//        			List<ResolveInfo> acts = getPackageManager().queryIntentActivities(  
//        			        intent, 0);  
//        			if (acts.size() > 0) {  
//        			    startActivity(intent);  
//        			} else {  
//        			    Toast.makeText(LteTool.this,  
//        			            getString(R.string.failed_to_resolve_activity),  
//        			            Toast.LENGTH_SHORT).show();  
//        			}  
//        		}
//        		catch (Exception e)
//        		{}
//        	}
//    	});
//        
//        mBtnPower.setOnClickListener(new Button.OnClickListener()
//        {
//        	public void onClick(View v)
//        	{
//        		try
//        		{
//        			Toast.makeText(LteTool.this,  
//        			            getString(R.string.failed_to_resolve_activity),  
//        			            Toast.LENGTH_SHORT).show();
//        			mBtnPower.setText(R.string.strPowerOff);       			
//        		}
//        		catch (Exception e)
//        		{}
//        	}
//    	});
    	
    }
        @Override
        public void onStop() {
                super.onStop();
                //releaseWakeLock();
        }
}
