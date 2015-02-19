package com.lhsoft.pda.ui.activities;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.ui.adapters.HomeListAdapter;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;
import com.senter.support.openapi.StBarcodeScanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HomeScreenActivity extends Activity {

	private static final String TAG = "HomeScreen";

	private TextView mPickingName; 
	private Switch mScanner;
	
	private ListView mHomeList;
	private HomeListAdapter mHomeListAdapter;

	AtomicBoolean isScanning = new AtomicBoolean(false);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		mPickingName = (TextView) findViewById(R.id.picking_name_edit);
		mScanner = (Switch)findViewById(R.id.scanner);
		
		mScanner.setChecked(SharedVars.mScanner);
		mScanner.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedVars.mScanner = isChecked;
			}
		});
		
		mHomeListAdapter = new HomeListAdapter(new HomeListAdapter.OnPickingClickListener() {
			
			@Override
			public void onClick(String pickingName) {
				checkPickingId(pickingName);
			}
		});
		mHomeList = (ListView)findViewById(R.id.home_table);
		mHomeList.setAdapter(mHomeListAdapter);
		
		SharedVars.mCurPickingId = null;
		SharedVars.mCurPicking = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		connectOerp();
	}

	private void connectOerp() {
		SharedVars.mInternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();		
		
		final Resources res = getResources();

		if (Oerp.getInstance().isConnected()) {
			getPickingData();
			return;
		}
		
		final ProgressDialog progressDialog = ProgressDialog.show(HomeScreenActivity.this, "", res.getString(R.string.connecting_oerp_server_message));
		progressDialog.show();

		Log.d(TAG, "Connection start");
		Oerp.getInstance().connect(new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {

				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(HomeScreenActivity.this, res.getString(R.string.connected_oerp_server_success_message), Toast.LENGTH_SHORT).show();
				
				getPickingData();
			}

			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}

				new AlertDialog.Builder(HomeScreenActivity.this)
				.setTitle("")
				.setMessage(res.getString(R.string.connected_oerp_server_failed_message))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						HomeScreenActivity.this.finish();
					}
				})
				.show();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode)
		{
		case 212://ST307左侧SCAN键健值为212
		case 221://ST308 ST309左侧SCAN键键值为221
			scan();
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void scan()
	{
		if (!SharedVars.mScanner)
			return;
		
		if (!Oerp.getInstance().isConnected())
			return;
		
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(HomeScreenActivity.this, "", res.getString(R.string.scanning_message));
		progressDialog.show();
		
		new Thread()
		{
			public void run()
			{
				if (isScanning.compareAndSet(false, true) == false) {//at the same time only one thread can be allowed to scan
					return;
				}

				try {
					StBarcodeScanner scanner = StBarcodeScanner.getInstance();
					if (scanner == null) {
						Log.e(TAG, "!!!!!!!!!!!!sdk is to old to work，please update sdk");
						return;
					}
					final String rslt = scanner.scan();//scan ,if failed,null will be return

					final String show = (rslt != null) ? rslt : "Failed";
					Log.e(TAG, "Scan code result:" + show);

					//update ui
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (progressDialog.isShowing()) {
								progressDialog.cancel();
							}
							if (rslt == null)
								Toast.makeText(HomeScreenActivity.this, res.getString(R.string.scanning_failed_message), Toast.LENGTH_SHORT).show();
							
							checkPickingId(rslt);
						}
					});
				} catch (InterruptedException e) {
					if (progressDialog.isShowing()) {
						progressDialog.cancel();
					}
					e.printStackTrace();
				} finally {
					if (progressDialog.isShowing()) {
						progressDialog.cancel();
					}
					isScanning.set(false);
				}
			};
		}.start();
	}
	
	private void checkPickingId(String pickingName) {
		
		final Resources res = getResources();
		
		if (pickingName == null) {
			mPickingName.setText(res.getString(R.string.default_picking_name));
			return;
		}
		
		mPickingName.setText(pickingName);
		
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();
		
		Oerp.getInstance().getPickingId(pickingName, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (result == null) {
					SharedVars.mCurPickingId = null;
					failed(res.getString(R.string.picking_name_not_found_message));
					return;
				}
				
				SharedVars.mCurPickingId = Integer.valueOf(result.toString());
				
				Log.d(TAG, "Picking Id = " + result.toString());
				
				Oerp.getInstance().getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {
					
					@Override
					public void succesed(Object result) {
						
						if (result == null) {
							failed(res.getString(R.string.no_picking_data_from_server_message));
							return;
						}
						Object[] ary = (Object[]) result;
						if (ary.length == 0) {
							failed(res.getString(R.string.no_picking_data_from_server_message));
							return;
						}
							
						SharedVars.mCurPicking = (HashMap<String, Object>) ary[0];
						
						String state = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_STATE).toString();
						if (state.equals(Oerp.PICKING_STATE_DONE)){
							failed(res.getString(R.string.picking_already_done_message));
							return;
						}
						
						boolean allow = Boolean.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_ALLOW).toString());
						if (!allow) {
							failed(res.getString(R.string.picking_not_allowed_message));
							return;
						}
						
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						
						SharedVars.logCurPicking();
						
						nextScreen(Oerp.BUTTON_SCAN);
					}
					
					@Override
					public void failed(String message) {
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						Toast.makeText(HomeScreenActivity.this, message, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(HomeScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void getPickingData() {
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();

		mHomeListAdapter.clear();
		
		Oerp.getInstance().getCurrentPickings(new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (result == null) {
					failed(res.getString(R.string.no_picking_data_from_server_message));
					return;
				}
				
				Object[] ary = (Object[]) result;
				if (ary.length == 0) {
					failed(res.getString(R.string.no_picking_data_from_server_message));
					return;
				}
				
				int i;
				
				Integer[] pIds = new Integer[ary.length];
				for (i = 0; i < ary.length; i ++) {
					pIds[i] = Integer.valueOf(ary[i].toString());
				}
				
				Oerp.getInstance().getPicking(pIds, new XMLRPCMethod.XMLRPCMethodCallback() {
					
					@Override
					public void succesed(Object result) {
						
						if (result == null) {
							failed(res.getString(R.string.no_picking_data_from_server_message));
							return;
						}
						Object[] ary = (Object[]) result;
						if (ary.length == 0) {
							failed(res.getString(R.string.no_picking_data_from_server_message));
							return;
						}
						
						int j;
						for (j = 0; j < ary.length; j++) {
							HashMap<String, Object> picking;
							picking = (HashMap<String, Object>) ary[j];
							
							String pickingName = picking.get(Oerp.PICKING_FIELD_NAME).toString();
							boolean stage1 = Boolean.valueOf(picking.get(Oerp.PICKING_FIELD_STAGE1).toString()).booleanValue();
							boolean stage2 = Boolean.valueOf(picking.get(Oerp.PICKING_FIELD_STAGE2).toString()).booleanValue();
							
							String carrier;
							Object objCarrier = picking.get(Oerp.PICKING_FIELD_DELIVERY_PARTNER);
							try {
								Object[] aryCarrier = (Object[]) objCarrier;
								carrier = aryCarrier[1].toString();
							} catch (Exception e) {
								carrier = "";
							}
							
							mHomeListAdapter.addHomeItem(pickingName, stage1, stage2, carrier);
							mHomeListAdapter.notifyDataSetChanged();
						}
						
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
					}

					@Override
					public void failed(String message) {
						Toast.makeText(HomeScreenActivity.this, message, Toast.LENGTH_SHORT).show();

						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
					}
				});
			}
			
			@Override
			public void failed(String message) {				
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(HomeScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void setPickingData(final String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		final Resources res = getResources();

		Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
		String qtyType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();

		Oerp.getInstance().updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				Oerp.getInstance().getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

					@Override
					public void succesed(Object result) {
						if (result == null) {
							Toast.makeText(HomeScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
							return;
						}
						Object[] ary = (Object[]) result;
						if (ary.length == 0) {
							Toast.makeText(HomeScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
							return;
						}

						SharedVars.mCurPicking = (HashMap<String, Object>) ary[0];
						String nextScreen = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NEXT_SCREEN).toString();

						callback.succesed(nextScreen);
					}

					@Override
					public void failed(String message) {
						callback.failed(message);
					}
				});
			}

			@Override
			public void failed(String message) {
				callback.failed(message);
			}
		});
	}
	
	private void nextScreen(String button) {
		Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.process_message));
		progressDialog.show();

		this.setPickingData(button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}

				String nextScreen = result.toString();
				
				ActivityManager.getInstance().showNextScreenActivity(HomeScreenActivity.this, nextScreen);
			}

			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(HomeScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void onRefresh(View v) {
		getPickingData();
	}
}
