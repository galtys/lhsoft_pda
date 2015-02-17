package com.lhsoft.pda.ui.activities;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DimensionsScreenActivity extends Activity {

	private static final String TAG = "DimensionsScreen";

	private TextView mPickingName; 

	private ListView mDimensionsList;
	private DimensionsListAdapter mDimensionsListAdapter;

	private AtomicInteger mReadCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dimensions_screen);

		mPickingName = (TextView) findViewById(R.id.picking_name_edit);

		mDimensionsListAdapter = new DimensionsListAdapter();
		mDimensionsList = (ListView)findViewById(R.id.dimensions_table);
		mDimensionsList.setAdapter(mDimensionsListAdapter);
		
		getPickingData();
	}

	private void getPickingData() {
		Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();

		int i;
		String pickingName = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
		mPickingName.setText(pickingName);
		
		mReadCount = new AtomicInteger(0);

		final Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		for (i = 0; i < packageCount; i ++) {
			final Integer number = Integer.valueOf(i + 1);
			Oerp.getInstance().getDimension(SharedVars.mCurPickingId, number, new XMLRPCMethod.XMLRPCMethodCallback() {

				@Override
				public void succesed(Object result) {
					// TODO Auto-generated method stub
					if (result == null) {
						mDimensionsListAdapter.addDimensionItem(false, number, 0, 0, 0);
					} else {
						Object[] ary = (Object[]) result;
						
						HashMap<String, Object> dimension = (HashMap<String, Object>) ary[0]; 
						mDimensionsListAdapter.addDimensionItem(
								Boolean.valueOf(dimension.get(Oerp.DIMENSION_FIELD_TRASH).toString()).booleanValue(),
								number, 
								Integer.valueOf(dimension.get(Oerp.DIMENSION_FIELD_WIDTH).toString()), 
								Integer.valueOf(dimension.get(Oerp.DIMENSION_FIELD_DEPTH).toString()), 
								Integer.valueOf(dimension.get(Oerp.DIMENSION_FIELD_HEIGHT).toString())
								);
					}
					
					if (packageCount.equals(mReadCount.incrementAndGet())) {
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						mDimensionsListAdapter.notifyDataSetChanged();
					}
				}

				@Override
				public void failed(String message) {
					// TODO Auto-generated method stub
					Toast.makeText(DimensionsScreenActivity.this, message, Toast.LENGTH_SHORT).show();

					if (packageCount.equals(mReadCount.incrementAndGet())) {
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						mDimensionsListAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	private void setPickingData(final String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		final Resources res = getResources();
		int i;
		int count = mDimensionsListAdapter.getCount();
		Object[] dimensions = new Object[count];

		for (i = 0; i < count; i++) {
			DimensionItem di = mDimensionsListAdapter.getDimensionItem(i);
			HashMap<String, Object> dimension = new HashMap<String, Object>();

			dimension.put(Oerp.DIMENSION_FIELD_TRASH, di.trash);
			dimension.put(Oerp.DIMENSION_FIELD_NUMBER, di.number);
			dimension.put(Oerp.DIMENSION_FIELD_WIDTH, di.width);
			dimension.put(Oerp.DIMENSION_FIELD_DEPTH, di.depth);
			dimension.put(Oerp.DIMENSION_FIELD_HEIGHT, di.height);

			dimensions[i] = dimension;
		}

		Oerp.getInstance().updateDimensions(SharedVars.mCurPickingId, dimensions, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				// TODO Auto-generated method stub

				Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
				String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
				String qtyType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();

				Oerp.getInstance().updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

					@Override
					public void succesed(Object result) {
						// TODO Auto-generated method stub
						Oerp.getInstance().getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

							@Override
							public void succesed(Object result) {
								// TODO Auto-generated method stub
								if (result == null) {
									Toast.makeText(DimensionsScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
									return;
								}
								Object[] ary = (Object[]) result;
								if (ary.length == 0) {
									Toast.makeText(DimensionsScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
									return;
								}

								SharedVars.mCurPicking = (HashMap<String, Object>) ary[0];
								String nextScreen = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NEXT_SCREEN).toString();

								callback.succesed(nextScreen);
							}

							@Override
							public void failed(String message) {
								// TODO Auto-generated method stub
								callback.failed(message);
							}
						});
					}

					@Override
					public void failed(String message) {
						// TODO Auto-generated method stub
						callback.failed(message);
					}
				});
			}

			@Override
			public void failed(String message) {
				// TODO Auto-generated method stub
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
				// TODO Auto-generated method stub
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}

				String nextScreen = result.toString();

				ActivityManager.getInstance().showNextScreenActivity(DimensionsScreenActivity.this, nextScreen);
			}

			@Override
			public void failed(String message) {
				// TODO Auto-generated method stub
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(DimensionsScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public void onContinue(View v) {
		nextScreen(Oerp.BUTTON_CONTINUE);
	}
	
	public void onHome(View v) {
		nextScreen(Oerp.BUTTON_HOME);
	}
	
	public void onFetchDimensions(View v) {
		
	}
}
