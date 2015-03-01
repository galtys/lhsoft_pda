package com.lhsoft.pda.utils;

import java.util.HashMap;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lhsoft.pda.R;

public class SharedVars {
	static public Integer mCurPickingId = null;
	static public HashMap<String, Object> mCurPicking = null;
	static public boolean mScanner = true;
	
	static public final String DBX_APP_KEY = "wu5vnd55gat43nr";
	
	static public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(final View v, boolean hasFocus) {
			if (hasFocus) {
				Handler handler = new Handler();
				handler.post(new Runnable() {
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						EditText numEdit = (EditText) v;
						String text = numEdit.getText().toString();
						numEdit.setText(text);
						numEdit.selectAll();
					}
				});
			}
		}
	};
	
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
	
	static public void logPackageIds() {
		if (mCurPicking != null) {
			String TAG = "Current Picking Package Ids";
			String result = "";
			
			Object[] ary = (Object[]) mCurPicking.get(Oerp.PICKING_FIELD_PACK_IDS);
			int i;
			for (i = 0; i < ary.length; i++) {
				result += ary[i].toString() + " ";
			}
			
			Log.d(TAG, result);
		}
	}
}
