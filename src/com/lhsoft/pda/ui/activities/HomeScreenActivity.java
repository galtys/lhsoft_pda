package com.lhsoft.pda.ui.activities;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.dropbox.chooser.android.DbxChooser;
import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.ui.adapters.HomeListAdapter;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;
import com.lhsoft.pda.utils.ConfFileParser;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;
import com.senter.support.openapi.StBarcodeScanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	static final int DBX_CHOOSER_REQUEST = 0;
	private DbxChooser mChooser;
	
	private AtomicBoolean isScanning = new AtomicBoolean(false);
	
	private boolean mShowDropbox = false;
	private boolean mFirstTime = true;
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
		
		mChooser = new DbxChooser(SharedVars.DBX_APP_KEY);
		
		SharedVars.mCurPickingId = null;
		SharedVars.mCurPicking = null;
	}
	
	private void prepareConnectOerp() {
		Resources res = getResources();
		
		if (Oerp.getInstance(HomeScreenActivity.this).isConnected()) {
			getPickingData();
		} else {
			int err = Oerp.getInstance(HomeScreenActivity.this).loadCredentials();
			if (err != ConfFileParser.PARSE_OK) {
				switch (err) {
				case ConfFileParser.PARSE_ERROR_OTHER:
					showFinishAlert(res.getString(R.string.invalid_storage_conf_file_message));
					break;
				case ConfFileParser.PARSE_ERROR_NOEXIST:
					Toast.makeText(HomeScreenActivity.this, res.getString(R.string.no_exist_conf_file_message), Toast.LENGTH_LONG).show();
					showDropBox();
					break;
				}
			} else {
				connectOerp();
			}
		}
	}
	
	private void showDropBox() {
		if (!mShowDropbox) {
			mChooser.forResultType(DbxChooser.ResultType.FILE_CONTENT).launch(HomeScreenActivity.this, DBX_CHOOSER_REQUEST);
			mShowDropbox = true;
		}
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Resources res = getResources();
		
        if (requestCode == DBX_CHOOSER_REQUEST) {
        	mShowDropbox = false;
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                String confFilePath = result.getLink().getPath();
                
                int err = Oerp.getInstance(HomeScreenActivity.this).loadCredentials(confFilePath);
    			if (err != ConfFileParser.PARSE_OK) {
    				switch (err) {
    				case ConfFileParser.PARSE_ERROR_OTHER:
    					showFinishAlert(res.getString(R.string.invalid_dropbox_conf_file_message));
    					break;
    				case ConfFileParser.PARSE_ERROR_NOEXIST:
    					showFinishAlert(res.getString(R.string.no_exist_conf_file_message));
    					break;
    				}
    			} else {
    				connectOerp();
    			}
                
                Log.d(TAG, "Dropbox: " + confFilePath);
            } else {
                // Failed or was cancelled by the user.
            	finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mFirstTime) {
			prepareConnectOerp();
			mFirstTime = false;
		}
	}

	private void showFinishAlert(String message) {
		new AlertDialog.Builder(HomeScreenActivity.this)
		.setTitle("")
		.setMessage(message)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				HomeScreenActivity.this.finish();
			}
		})
		.show();
	}
	
	private void connectOerp() {		
		
		final Resources res = getResources();

		if (Oerp.getInstance(HomeScreenActivity.this).isConnected()) {
			getPickingData();
			return;
		}
				
		final ProgressDialog progressDialog = ProgressDialog.show(HomeScreenActivity.this, "", res.getString(R.string.connecting_oerp_server_message));
		progressDialog.show();

		Log.d(TAG, "Connection start");
		Oerp.getInstance(HomeScreenActivity.this).connect(new XMLRPCMethod.XMLRPCMethodCallback() {

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

				showFinishAlert(res.getString(R.string.connected_oerp_server_failed_message) + "\n" + message);
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
		
		if (!Oerp.getInstance(HomeScreenActivity.this).isConnected())
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
		
		Oerp.getInstance(HomeScreenActivity.this).getPickingId(pickingName, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (result == null) {
					SharedVars.mCurPickingId = null;
					failed(res.getString(R.string.picking_name_not_found_message));
					return;
				}
				
				SharedVars.mCurPickingId = Integer.valueOf(result.toString());
				
				Log.d(TAG, "Picking Id = " + result.toString());
				
				Oerp.getInstance(HomeScreenActivity.this).getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {
					
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
		
		Oerp.getInstance(HomeScreenActivity.this).getCurrentPickings(new XMLRPCMethod.XMLRPCMethodCallback() {

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
				final HashMap<Integer, Integer> pIdOrder = new HashMap<Integer, Integer>();
				
				for (i = 0; i < ary.length; i ++) {
					pIds[i] = Integer.valueOf(ary[i].toString());
					pIdOrder.put(pIds[i], i);
					Log.d(TAG, "Orgin: " + pIds[i]);
				}
				
				Oerp.getInstance(HomeScreenActivity.this).getPicking(pIds, new XMLRPCMethod.XMLRPCMethodCallback() {
					
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
							
							Integer pId = Integer.valueOf(picking.get("id").toString());
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
							
							Integer order = pIdOrder.get(pId);
							Log.d(TAG, "New: " + pId + " " + order);
							mHomeListAdapter.addHomeItem(order, pickingName, stage1, stage2, carrier);
						}
						mHomeListAdapter.notifyDataSetChanged();
						
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

		Oerp.getInstance(HomeScreenActivity.this).updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				Oerp.getInstance(HomeScreenActivity.this).getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

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
