package com.lhsoft.pda.manager;

import com.lhsoft.pda.ui.activities.DimensionsScreenActivity;
import com.lhsoft.pda.ui.activities.HomeScreenActivity;
import com.lhsoft.pda.ui.activities.MainScreenActivity;
import com.lhsoft.pda.ui.activities.PickingScreenActivity;
import com.lhsoft.pda.ui.activities.TakePhotoScreenActivity;
import com.lhsoft.pda.utils.Oerp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class ActivityManager {
	static private final String TAG = "ActivityManager";
	
	static public final String EXTRA_ISRETURN = "isReturn";
	static private ActivityManager mInstance = null;
	
	public ActivityManager() {
		// TODO Auto-generated constructor stub
	}
	
	static public ActivityManager getInstance() {
		if (mInstance == null) {
			mInstance = new ActivityManager();
		}
		return mInstance;
	}
	
	public void showNextScreenActivity(Activity curActivity, String nextScreen) {
		Intent intent = null;
		if (nextScreen.equals(Oerp.SCREEN_HOME)) {
			intent = new Intent(curActivity, HomeScreenActivity.class);
		} else if (nextScreen.equals(Oerp.SCREEN_MAIN)) {
			intent = new Intent(curActivity, MainScreenActivity.class);
			intent.putExtra(EXTRA_ISRETURN, false);
		} else if (nextScreen.equals(Oerp.SCREEN_MAIN_RETURN)) {
			intent = new Intent(curActivity, MainScreenActivity.class);
			intent.putExtra(EXTRA_ISRETURN, true);
		} else if (nextScreen.equals(Oerp.SCREEN_DIMENSIONS)) {
			intent = new Intent(curActivity, DimensionsScreenActivity.class);
		} else if (nextScreen.equals(Oerp.SCREEN_PICKING_PARTIAL)) {
			intent = new Intent(curActivity, PickingScreenActivity.class);
			intent.putExtra(EXTRA_ISRETURN, false);
		} else if (nextScreen.equals(Oerp.SCREEN_RETURN_PARTIAL)) {
			intent = new Intent(curActivity, PickingScreenActivity.class);
			intent.putExtra(EXTRA_ISRETURN, true);
		} else if (nextScreen.equals(Oerp.SCREEN_PHOTO)) {
			intent = new Intent(curActivity, TakePhotoScreenActivity.class);
		}
		
		if (intent != null) {
			Log.e(TAG, "Next Screen is " + nextScreen);
			curActivity.finish();
			curActivity.startActivity(intent);
		}
	}
}
