package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;

import com.lhsoft.pda.R;
import com.lhsoft.pda.ui.adapters.DimensionsListAdapter.DimensionItem;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PickingListAdapter extends BaseAdapter{

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
	
	synchronized public void addDimensionItem(Integer productQty, String name, Integer toFollow, Integer returned, Integer repair) {
		PickingItem pi = new PickingItem();
		
		pi.productQty = productQty;
		pi.name = name;
		pi.toFollow = toFollow;
		pi.returned = returned;
		pi.repair = repair;
		
		mPickings.add(pi);
	}
	
	public PickingItem getPickingItem(int index) {
		return mPickings.get(index);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPickings.size();
	}

	@Override
	public PickingItem getItem(int position) {
		// TODO Auto-generated method stub
		return mPickings.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
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
		
		final PickingItem pi = getItem(position);
		
		if (!isReturn) {
			TextView qtyLabel = (TextView) view.findViewById(R.id.picking_item_qty);
			TextView nameLabel = (TextView) view.findViewById(R.id.picking_item_name);
			EditText toFollowEdit = (EditText) view.findViewById(R.id.picking_item_to_follow);
			
			qtyLabel.setText(pi.productQty.toString());
			nameLabel.setText(pi.name);
			toFollowEdit.setText(pi.toFollow.toString());
			
			toFollowEdit.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					Integer newValue = null;
					try {
						newValue = Integer.valueOf(s.toString());
					} catch(NumberFormatException e) {
						newValue = 0; 
					}
					
					pi.toFollow = newValue;
				}
			});
		} else {
			TextView returnQtyLabel = (TextView) view.findViewById(R.id.picking_return_item_qty);
			TextView returnNameLabel = (TextView) view.findViewById(R.id.picking_return_item_name);
			EditText returnReturnedEdit = (EditText) view.findViewById(R.id.picking_return_item_returned);
			EditText returnRepairEdit = (EditText) view.findViewById(R.id.picking_return_item_repair);
			
			returnQtyLabel.setText(pi.productQty.toString());
			returnNameLabel.setText(pi.name);
			returnReturnedEdit.setText(pi.returned.toString());
			returnRepairEdit.setText(pi.repair.toString());
			
			returnReturnedEdit.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					Integer newValue = null;
					try {
						newValue = Integer.valueOf(s.toString());
					} catch(NumberFormatException e) {
						newValue = 0; 
					}
					
					pi.returned = newValue;
				}
			});
			
			returnRepairEdit.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					Integer newValue = null;
					try {
						newValue = Integer.valueOf(s.toString());
					} catch(NumberFormatException e) {
						newValue = 0; 
					}
					
					pi.repair = newValue;
				}
			});
		}
		
		return view;
	}
}