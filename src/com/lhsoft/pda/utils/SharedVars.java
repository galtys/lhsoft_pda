package com.lhsoft.pda.utils;

import java.util.HashMap;

import android.util.Log;

import com.lhsoft.pda.R;

public class SharedVars {
	static public Integer mCurPickingId = null;
	static public HashMap<String, Object> mCurPicking = null;
	static public boolean mScanner = true;
	static public String mInternalStoragePath = null;
	
	static public void logCurPicking() {
		if (mCurPicking != null) {
			String TAG = "Current Picking";
			
			String pickingName = mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
			Log.d(TAG, "Picking Name = " + pickingName);
			
			Integer packageCount = Integer.valueOf(mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
			Log.d(TAG, "Package Count = " + packageCount);
			
			String packageType = mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
			Log.d(TAG, "Package Type = " + packageType);
			
			String qtyType = mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();
			Log.d(TAG, "Qty Type = " + qtyType);
		}
	
	}
}
