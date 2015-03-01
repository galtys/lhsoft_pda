package com.lhsoft.pda.ui.activities;

import java.util.HashMap;

import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.ui.adapters.PickingListAdapter;
import com.lhsoft.pda.ui.adapters.PickingListAdapter.PickingItem;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PickingScreenActivity extends Activity {

	private static final String TAG = "PickingScreen";

	private TextView mPickingName;
	private boolean isReturn = false;
	
	private ListView mPickingList;
	private PickingListAdapter mPickingListAdapter;

	private int mWriteCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picking_screen);

		Intent intent = getIntent();
		isReturn = intent.getBooleanExtra(ActivityManager.EXTRA_ISRETURN, false);
		
		mPickingName = (TextView) findViewById(R.id.picking_name_edit);
		
		if (!isReturn) {
			getLayoutInflater().inflate(R.layout.picking_table, (ViewGroup) findViewById(R.id.table_view_group));
			mPickingList = (ListView)findViewById(R.id.picking_table);
		} else {
			getLayoutInflater().inflate(R.layout.picking_return_table, (ViewGroup) findViewById(R.id.table_view_group));
			mPickingList = (ListView)findViewById(R.id.picking_return_table);
		}
		
		mPickingListAdapter = new PickingListAdapter(isReturn);
		mPickingList.setAdapter(mPickingListAdapter);
		
		getPickingData();
	}
	
	private Integer[] getMoveIds() {
		int i;
		
		Object[] obj = (Object[]) SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_MOVE_LINES);
		Integer[] moveIds = new Integer[obj.length];
		for (i = 0; i < obj.length; i++) {
			moveIds[i] = Integer.valueOf(obj[i].toString());
		}
		
		return moveIds;
	}
	
	private void getPickingData() {
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();
		
		String pickingName = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
		mPickingName.setText(pickingName);
		
		Integer[] moveIds = this.getMoveIds();
		
		Oerp.getInstance(PickingScreenActivity.this).getStockMoves(moveIds, new XMLRPCMethod.XMLRPCMethodCallback() {
			
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
				for (i = 0; i < ary.length; i ++) {
					HashMap<String, Object> picking = (HashMap<String, Object>) ary[i];
					
					if (isReturn) {
						mPickingListAdapter.addPickingItem(
								Double.valueOf(picking.get(Oerp.STOCK_MOVE_FIELD_PRODUCT_QTY).toString()).intValue(),
								picking.get(Oerp.STOCK_MOVE_FIELD_NAME).toString(),
								0,
								Integer.valueOf(picking.get(Oerp.STOCK_MOVE_FIELD_RETURNED).toString()),
								Integer.valueOf(picking.get(Oerp.STOCK_MOVE_FIELD_REPAIR).toString())
						);
					} else {
						mPickingListAdapter.addPickingItem(
								Double.valueOf(picking.get(Oerp.STOCK_MOVE_FIELD_PRODUCT_QTY).toString()).intValue(),
								picking.get(Oerp.STOCK_MOVE_FIELD_NAME).toString(),
								Integer.valueOf(picking.get(Oerp.STOCK_MOVE_FIELD_TO_FOLLOW).toString()),
								0,
								0
						);
					}
					mPickingListAdapter.notifyDataSetChanged();
				}
				
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
			}
			
			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(PickingScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void updatePicking(String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		final Resources res = getResources();

		Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
		String qtyType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();

		Oerp.getInstance(PickingScreenActivity.this).updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				Oerp.getInstance(PickingScreenActivity.this).getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

					@Override
					public void succesed(Object result) {
						if (result == null) {
							Toast.makeText(PickingScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
							return;
						}
						Object[] ary = (Object[]) result;
						if (ary.length == 0) {
							Toast.makeText(PickingScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
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
	
	private void setPickingData(final String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		int i;
		final int count = mPickingListAdapter.getCount();
		
		Integer[] moveIds = this.getMoveIds();
		
		mWriteCount = 0;
		
		for (i = 0; i < count; i++) {
			final int index = i;
			PickingItem pi = mPickingListAdapter.getPickingItem(index);
			
			if (isReturn) {
				Oerp.getInstance(PickingScreenActivity.this).updateReturnRepair(moveIds[index], pi.returned, pi.repair, new XMLRPCMethod.XMLRPCMethodCallback() {
					
					@Override
					public void succesed(Object result) {
						mWriteCount++;
						if (mWriteCount == count) {
							updatePicking(button, callback);
						}
					}
					
					@Override
					public void failed(String message) {
						// callback.failed(message);
						mWriteCount++;
						if (mWriteCount == count) {
							updatePicking(button, callback);
						}
					}
				});
			} else {
				Oerp.getInstance(PickingScreenActivity.this).updateToFollow(moveIds[index], pi.toFollow, new XMLRPCMethod.XMLRPCMethodCallback() {
					
					@Override
					public void succesed(Object result) {
						mWriteCount++;
						if (mWriteCount == count) {
							updatePicking(button, callback);
						}
					}
					
					@Override
					public void failed(String message) {
						// callback.failed(message);
						mWriteCount++;
						if (mWriteCount == count) {
							updatePicking(button, callback);
						}
					}
				});
			}
		}
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

				ActivityManager.getInstance().showNextScreenActivity(PickingScreenActivity.this, nextScreen);
			}

			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(PickingScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public void onContinue(View v) {
		nextScreen(Oerp.BUTTON_CONTINUE);
	}
	
	public void onHome(View v) {
		nextScreen(Oerp.BUTTON_HOME);
	}
	
}
