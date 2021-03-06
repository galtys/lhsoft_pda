package com.lhsoft.pda.ui.activities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TakePhotoScreenActivity extends Activity {

	private static final String TAG = "TakePhotoScreen";
	
	private static final int REQUEST_TAKE_PHOTO = 1;
	
	private TextView mPickingName;
	
	private ListView mPhotoList;
	private PhotoListAdapter mPhotoListAdapter;
	
	private AtomicInteger mReadCount;
	
	private int mCurPosition;
	private int mCurNumber;
	private int mCurPhoto;
	
	private String mCurrentPhotoPath;
	
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
	    if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
	    	
	    	final Resources res = getResources();
			final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.uploading_photo_message));
			progressDialog.show();
			
	        //Bundle extras = data.getExtras();
	        //Bitmap imageBitmap = (Bitmap) extras.get("data");
	        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        //imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	        //byte[] byteArray = stream.toByteArray();
	        
	        final File file = new File(mCurrentPhotoPath);
	        FileInputStream fis = null;

	        try {
	        	fis = new FileInputStream(mCurrentPhotoPath);
	        	
	        	byte fileContent[] = new byte[(int) file.length()];
	        	fis.read(fileContent);
	        	String encoded = Base64.encodeToString(fileContent, Base64.DEFAULT);
		        
		        Oerp.getInstance(TakePhotoScreenActivity.this).uploadPhoto(SharedVars.mCurPickingId, mCurNumber, mCurPhoto + 1, encoded, new XMLRPCMethod.XMLRPCMethodCallback() {
					
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
						
						file.delete();
					}
					
					@Override
					public void failed(String message) {
						if (progressDialog.isShowing()) {
				    		progressDialog.cancel();
				    	}
						Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();
						
						file.delete();
					}
				});
		        
				fis.close();
		        
	        } catch (FileNotFoundException e) {
	        	Log.e(TAG, e.getMessage());
	        } catch (IOException ioe) {
	        	Log.e(TAG, ioe.getMessage());
	        }

	    }
	}
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	    
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = image.getAbsolutePath();
	    return image;
	}
	
	private void getPickingData() {
		final Resources res = getResources();
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();
		
		String pickingName = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
		mPickingName.setText(pickingName);
		
		int i;
		mReadCount = new AtomicInteger(0);

		final Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
		
		mPhotoListAdapter.setPackType(packageType);
		mPhotoListAdapter.setAdapterListener(new PhotoListAdapter.OnAdapterListener() {
			
			@Override
			public void onPhoto(int position, int number, int photo) {
				mCurPosition = position;
				mCurNumber = number;
				mCurPhoto = photo;
				
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			    	File photoFile = null;
			        try {
			            photoFile = createImageFile();
			        } catch (IOException ex) {
			            // Error occurred while creating the File
			            Log.d(TAG, ex.getMessage());
			        }
			        // Continue only if the File was successfully created
			        if (photoFile != null) {
			            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
			            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			        }
			    }
			}
		});
		
		SharedVars.logPackageIds();
		
		for (i = 0; i < packageCount; i ++) {
			final Integer number = Integer.valueOf(i + 1);
			Oerp.getInstance(TakePhotoScreenActivity.this).getTracking(SharedVars.mCurPickingId, number, new XMLRPCMethod.XMLRPCMethodCallback() {

				@Override
				public void succesed(Object result) {
					if (result != null) {
						Object[] ary = (Object[]) result;
						
						HashMap<String, Object> tracking = (HashMap<String, Object>) ary[0];
						
						boolean trash = Boolean.valueOf(tracking.get(Oerp.TRACKING_FIELD_TRASH).toString()).booleanValue();
						if (!trash) {
							mPhotoListAdapter.addPhotoItem(
									number, 
									Boolean.valueOf(tracking.get(Oerp.TRACKING_FIELD_PHOTO1).toString()).booleanValue(), 
									Boolean.valueOf(tracking.get(Oerp.TRACKING_FIELD_PHOTO2).toString()).booleanValue() 
									);
						}
					}
					
					if (packageCount.equals(mReadCount.incrementAndGet())) {
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						mPhotoListAdapter.sort();
						mPhotoListAdapter.notifyDataSetChanged();
					}
				}

				@Override
				public void failed(String message) {
					Toast.makeText(TakePhotoScreenActivity.this, message, Toast.LENGTH_SHORT).show();

					if (packageCount.equals(mReadCount.incrementAndGet())) {
						if (progressDialog.isShowing()) {
							progressDialog.cancel();
						}
						mPhotoListAdapter.sort();
						mPhotoListAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}
	
	private void setPickingData(String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		final Resources res = getResources();

		Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
		String qtyType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();

		Oerp.getInstance(TakePhotoScreenActivity.this).updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				Oerp.getInstance(TakePhotoScreenActivity.this).getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

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
				
		setPickingData(button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}

				boolean hasPhoto = Boolean.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_STAGE2).toString()).booleanValue(); 
				if (hasPhoto || !validation) {
					String nextScreen = result.toString();
	
					ActivityManager.getInstance().showNextScreenActivity(TakePhotoScreenActivity.this, nextScreen);
				} else {
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
