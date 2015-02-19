package com.lhsoft.pda.ui.activities;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;

import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.ui.adapters.PhotoListAdapter;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TakePhotoScreenActivity extends Activity {

	private static final String TAG = "TakePhotoScreen";
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	
	private TextView mPickingName;
	
	private ListView mPhotoList;
	private PhotoListAdapter mPhotoListAdapter;
	
	private int mCurPosition;
	private int mCurPhoto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_screen);
		
		mPickingName = (TextView) findViewById(R.id.picking_name_edit);
		mPhotoList = (ListView)findViewById(R.id.photo_list);
		mPhotoListAdapter = new PhotoListAdapter();
		
		mPhotoList.setAdapter(mPhotoListAdapter);
		
		getPickingData();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	    	
	    	final Resources res = getResources();
			final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.uploading_photo_message));
			progressDialog.show();
			
	        Bundle extras = data.getExtras();
	        Bitmap imageBitmap = (Bitmap) extras.get("data");

	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
	        byte[] byteArray = stream.toByteArray(); 
	        
	        Oerp.getInstance().uploadPhoto(SharedVars.mCurPickingId, mCurPosition + 1, mCurPhoto + 1, byteArray, new XMLRPCMethod.XMLRPCMethodCallback() {
				
				@Override
				public void succesed(Object result) {
					Log.d(TAG, "Upload photo result = " + result.toString());
					
					boolean success = Boolean.valueOf(result.toString());
					
					if (progressDialog.isShowing()) {
			    		progressDialog.cancel();
			    	}
					
					if (success) {
						mPhotoListAdapter.setCheck(mCurPosition, mCurPhoto);
						mPhotoListAdapter.notifyDataSetChanged();
						Toast.makeText(TakePhotoScreenActivity.this, res.getString(R.string.upload_photo_success_message), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(TakePhotoScreenActivity.this, res.getString(R.string.upload_photo_fail_message), Toast.LENGTH_SHORT).show();
					}
			    	
				}
				
				@Override
				public void failed(String message) {
					if (progressDialog.isShowing()) {
			    		progressDialog.cancel();
			    	}
					Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			});
	    }
	}
	
	private void getPickingData() {
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();
		
		String pickingName = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
		mPickingName.setText(pickingName);
		
		Oerp.getInstance().getPhotos(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {
			
			@Override
			public void succesed(Object result) {
				Log.d(TAG, result.toString());
				
				String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
				Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
				
				mPhotoListAdapter.setData(packageType, packageCount);
				mPhotoListAdapter.setAdapterListener(new PhotoListAdapter.OnAdapterListener() {
					
					@Override
					public void onPhoto(int position, int photo) {
						mCurPosition = position;
						mCurPhoto = photo;
						
						Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
					        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
					    }
					}
				});
				
				Object[] ary1 = (Object[]) result;
				int i, j;
				for (i = 0; i < ary1.length; i++) {
					Object[] ary2 = (Object[]) ary1[i];
					for (j = 0; j < ary2.length; j++) {
						
						if (Boolean.valueOf(ary2[j].toString())) {
							mPhotoListAdapter.setCheck(j, i);
							mPhotoListAdapter.notifyDataSetChanged();
						}
					}
				}
				
				//TEST CODE
				Object[] package_ids = (Object[]) SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_IDS);
				for (j = 0; j < package_ids.length; j++) {
					Log.d(TAG, "Package Id = " + package_ids[j].toString());
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
				Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	private void setPickingData(String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
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
							Toast.makeText(TakePhotoScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
							return;
						}
						Object[] ary = (Object[]) result;
						if (ary.length == 0) {
							Toast.makeText(TakePhotoScreenActivity.this, res.getString(R.string.no_picking_data_from_server_message), Toast.LENGTH_SHORT).show();
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
		nextScreen(button, true);
	}
	
	private void nextScreen(final String button, final boolean validation) {
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.process_message));
		progressDialog.show();
		
		Oerp.getInstance().testHasPhotos(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {
			
			@Override
			public void succesed(Object result) {
				boolean hasPhoto = Boolean.valueOf(result.toString());
				Log.d(TAG, "has photo = " + hasPhoto);
				if (hasPhoto || !validation) {
					setPickingData(button, new XMLRPCMethod.XMLRPCMethodCallback() {

						@Override
						public void succesed(Object result) {
							if (progressDialog.isShowing()) {
								progressDialog.cancel();
							}

							String nextScreen = result.toString();

							ActivityManager.getInstance().showNextScreenActivity(TakePhotoScreenActivity.this, nextScreen);
						}

						@Override
						public void failed(String message) {
							if (progressDialog.isShowing()) {
								progressDialog.cancel();
							}
							Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					if (progressDialog.isShowing()) {
						progressDialog.cancel();
					}
					Toast.makeText(TakePhotoScreenActivity.this, res.getString(R.string.take_all_photo_message), Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void failed(String message) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public void onContinue(View v) {
		nextScreen(Oerp.BUTTON_CONTINUE);
	}
	
	public void onHome(View v) {
		nextScreen(Oerp.BUTTON_HOME, false);
	}
}
