package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;

import com.lhsoft.pda.R;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;
import com.lhsoft.pda.utils.SharedVars;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PickingListAdapter extends BaseAdapter{

	private static final String TAG = "PickingListAdapter";
	
	private boolean isReturn;

	public class PickingItem {
		public Integer productQty;
		public String name;
		public Integer toFollow;
		public Integer returned;
		public Integer repair;
	}
	
	private ArrayList<PickingItem> mPickings;
	
	public PickingListAdapter(boolean isReturn) {
		
		this.isReturn = isReturn;
		
		mPickings = new ArrayList<PickingItem>();
	}
	
	synchronized public void addPickingItem(Integer productQty, String name, Integer toFollow, Integer returned, Integer repair) {
		PickingItem pi = new PickingItem();
		
		pi.productQty = productQty;
		pi.name = name;
		pi.toFollow = toFollow;
		pi.returned = returned;
		pi.repair = repair;
		
		mPickings.add(pi);
	}
	
	synchronized private void setToFollow(int position, int newToFollow) {
		PickingItem pi = mPickings.get(position);
		pi.toFollow = newToFollow;
	}
	
	synchronized private void setReturned(int position, int newReturned) {
		PickingItem pi = mPickings.get(position);
		pi.returned = newReturned;
	}
	
	synchronized private void setRepair(int position, int newRepair) {
		PickingItem pi = mPickings.get(position);
		pi.repair = newRepair;
	}
	
	public PickingItem getPickingItem(int index) {
		return mPickings.get(index);
	}
	
	@Override
	public int getCount() {
		return mPickings.size();
	}

	@Override
	public PickingItem getItem(int position) {
		return mPickings.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			if (!isReturn) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picking_item, parent, false);
			} else{
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picking_return_item, parent, false);
			}
		} else {
			view = convertView;
		}
		
		PickingItem pi = getItem(position);
		
		Log.e(TAG, "name: " + pi.name);
		
		if (!isReturn) {
			TextView qtyLabel = (TextView) view.findViewById(R.id.picking_item_qty);
			TextView nameLabel = (TextView) view.findViewById(R.id.picking_item_name);
			EditText toFollowEdit = (EditText) view.findViewById(R.id.picking_item_to_follow);
			
			qtyLabel.setText(pi.productQty.toString());
			nameLabel.setText(pi.name);
			toFollowEdit.setText(pi.toFollow.toString());
			
			toFollowEdit.setTag(position);
			toFollowEdit.addTextChangedListener(new CustomTextWatcher(toFollowEdit, position, CustomTextWatcher.WATCHER_TOFOLLOW));
			
			toFollowEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
		} else {
			TextView returnQtyLabel = (TextView) view.findViewById(R.id.picking_return_item_qty);
			TextView returnNameLabel = (TextView) view.findViewById(R.id.picking_return_item_name);
			EditText returnReturnedEdit = (EditText) view.findViewById(R.id.picking_return_item_returned);
			EditText returnRepairEdit = (EditText) view.findViewById(R.id.picking_return_item_repair);
			
			returnQtyLabel.setText(pi.productQty.toString());
			returnNameLabel.setText(pi.name);
			returnReturnedEdit.setText(pi.returned.toString());
			returnRepairEdit.setText(pi.repair.toString());
			
			returnReturnedEdit.setTag(position);
			returnRepairEdit.setTag(position);
			
			returnReturnedEdit.addTextChangedListener(new CustomTextWatcher(returnReturnedEdit, position, CustomTextWatcher.WATCHER_RETURNED));
			returnRepairEdit.addTextChangedListener(new CustomTextWatcher(returnRepairEdit, position, CustomTextWatcher.WATCHER_REPAIR));
			
			returnReturnedEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
			returnRepairEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
		}
		
		return view;
	}
	
	private class CustomTextWatcher implements TextWatcher {
		private EditText mEditText;
		private int mPosition;
		private int mWatcher;
		public static final int WATCHER_TOFOLLOW = 0;
		public static final int WATCHER_RETURNED = 1;
		public static final int WATCHER_REPAIR = 2;
		
		public CustomTextWatcher(EditText editText, int position, int watcher) {
			mEditText = editText;
			mPosition = position;
			mWatcher = watcher;
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			Integer newValue = null;
			try {
				newValue = Integer.valueOf(s.toString());
			} catch(NumberFormatException e) {
				newValue = 0; 
			}
			if ((Integer) mEditText.getTag() == mPosition) {
				if (mWatcher == WATCHER_TOFOLLOW) {
					setToFollow(mPosition, newValue);
				} else if (mWatcher == WATCHER_RETURNED) {
					setReturned(mPosition, newValue);
				} else if (mWatcher == WATCHER_REPAIR) {
					setRepair(mPosition, newValue);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
	}
}