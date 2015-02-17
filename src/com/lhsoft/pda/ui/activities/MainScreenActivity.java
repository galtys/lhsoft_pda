package com.lhsoft.pda.ui.activities;

import java.util.HashMap;

import com.lhsoft.pda.R;
import com.lhsoft.pda.manager.ActivityManager;
import com.lhsoft.pda.utils.Oerp;
import com.lhsoft.pda.utils.SharedVars;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainScreenActivity extends Activity {

	private static final String TAG = "MainScreen";

	private TextView mPickingLabel;
	private TextView mPickingName;
	private EditText mSplitCountEdit;

	private RadioGroup mPackTypeGroup;
	private RadioButton mBoxesButton;
	private RadioButton mPalletsButton;

	private RadioGroup mQuantityTypeGroup;
	private RadioButton mFullyButton;
	private RadioButton mPartiallyButton;

	private Button mTakePhotoButton;

	private boolean isReturn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);

		mPickingLabel = (TextView)findViewById(R.id.picking_label);
		mPickingName = (TextView)findViewById(R.id.picking_name_edit);

		mSplitCountEdit = (EditText)findViewById(R.id.split_count_edit);

		mPackTypeGroup = (RadioGroup)findViewById(R.id.pack_type_radio_group);
		mBoxesButton = (RadioButton)findViewById(R.id.boxes_button);
		mPalletsButton = (RadioButton)findViewById(R.id.pallets_button);

		mQuantityTypeGroup = (RadioGroup)findViewById(R.id.quantity_type_radio_group);
		mFullyButton = (RadioButton)findViewById(R.id.fully_button);
		mPartiallyButton = (RadioButton)findViewById(R.id.partially_button);

		mTakePhotoButton = (Button)findViewById(R.id.take_photo_button);

		InputFilter filter = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				for (int i = start; i < end; i++) {
					if (!Character.isDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		mSplitCountEdit.setFilters(new InputFilter[] { filter });

		Intent intent = getIntent();
		isReturn = intent.getBooleanExtra(ActivityManager.EXTRA_ISRETURN, false);

		this.updateUi();

		getPickingData();
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

				ActivityManager.getInstance().showNextScreenActivity(MainScreenActivity.this, nextScreen);
			}

			@Override
			public void failed(String message) {
				// TODO Auto-generated method stub
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
				Toast.makeText(MainScreenActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void onTakePhoto(View v) {
		nextScreen(Oerp.BUTTON_PHOTO);
	}

	public void onContinue(View v) {
		nextScreen(Oerp.BUTTON_CONTINUE);
	}

	public void onHome(View v) {
		nextScreen(Oerp.BUTTON_HOME);
	}
	
	private void resetPackTypeButtons() {
		mPackTypeGroup.clearCheck();
	}

	private void resetQtyTypeButtons() {
		mQuantityTypeGroup.clearCheck();
	}

	private void updateUi() {
		Resources res = getResources();

		mPickingName.setText(getResources().getString(R.string.default_picking_name));
		mSplitCountEdit.setText(getResources().getString(R.string.default_split_count));
		findViewById(R.id.split_count_group).requestFocus();

		resetPackTypeButtons();
		resetQtyTypeButtons();

		if (!isReturn) {
			mPickingLabel.setText(res.getString(R.string.picking_label));
			mFullyButton.setText(res.getString(R.string.fully_button_label));
			mPartiallyButton.setText(res.getString(R.string.partially_button_label));
			mTakePhotoButton.setVisibility(View.VISIBLE);
		} else {
			mPickingLabel.setText(res.getString(R.string.picking_return_label));
			mFullyButton.setText(res.getString(R.string.fully_button_return_label));
			mPartiallyButton.setText(res.getString(R.string.partially_button_return_label));
			mTakePhotoButton.setVisibility(View.INVISIBLE);
		}
	}

	private void setPickingData(final String button, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		final Resources res = getResources();

		if (SharedVars.mCurPickingId == null) {
			callback.failed(res.getString(R.string.picking_name_invalid_message));
			return;
		}

		String splitCountString = this.mSplitCountEdit.getText().toString();
		int packageCount = Integer.valueOf(splitCountString);
		if (packageCount < 1) {
			callback.failed(res.getString(R.string.package_count_invalid_message));
			return;
		}

		String packageType = Oerp.PACKAGE_TYPE_NA;
		if (mPackTypeGroup.getCheckedRadioButtonId() == R.id.boxes_button) {
			packageType = Oerp.PACKAGE_TYPE_BOX;
		} else if (mPackTypeGroup.getCheckedRadioButtonId() == R.id.pallets_button) {
			packageType = Oerp.PACKAGE_TYPE_PALLET;
		} else {
			callback.failed(res.getString(R.string.package_type_invalid_message));
			return;
		}

		String qtyType = Oerp.QTY_TYPE_NA;
		if (mQuantityTypeGroup.getCheckedRadioButtonId() == R.id.fully_button) {
			qtyType = Oerp.QTY_TYPE_FULL;
		} else if (mQuantityTypeGroup.getCheckedRadioButtonId() == R.id.partially_button) {
			qtyType = Oerp.QTY_TYPE_PARTIAL;
		} else {
			callback.failed(res.getString(R.string.qty_type_invalid_message));
			return;
		}

		Oerp.getInstance().updatePicking(SharedVars.mCurPickingId, packageCount, packageType, qtyType, button, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				// TODO Auto-generated method stub
				Oerp.getInstance().getPicking(SharedVars.mCurPickingId, new XMLRPCMethod.XMLRPCMethodCallback() {

					@Override
					public void succesed(Object result) {
						// TODO Auto-generated method stub
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
						SharedVars.logCurPicking();

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

	private void getPickingData() {		
		Resources res = getResources();
		ProgressDialog progressDialog = ProgressDialog.show(this, "", res.getString(R.string.getting_picking_data_from_server_message));
		progressDialog.show();

		String pickingName = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_NAME).toString();
		mPickingName.setText(pickingName);

		Integer packageCount = Integer.valueOf(SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_COUNT).toString());
		if (packageCount > 0) {
			mSplitCountEdit.setText(packageCount.toString());
		}

		String packageType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_PACK_TYPE).toString();
		if (packageType.equals(Oerp.PACKAGE_TYPE_BOX)) {
			mPackTypeGroup.check(R.id.boxes_button);
		} else if (packageType.equals(Oerp.PACKAGE_TYPE_PALLET)) {
			mPackTypeGroup.check(R.id.pallets_button);
		}

		String qtyType = SharedVars.mCurPicking.get(Oerp.PICKING_FIELD_QTY_TYPE).toString();
		if (qtyType.equals(Oerp.QTY_TYPE_PARTIAL)) {
			mQuantityTypeGroup.check(R.id.partially_button);
		} else if (qtyType.equals(Oerp.QTY_TYPE_FULL)) {
			mQuantityTypeGroup.check(R.id.fully_button);
		}

		if (progressDialog.isShowing()) {
			progressDialog.cancel();
		}
	}
}
