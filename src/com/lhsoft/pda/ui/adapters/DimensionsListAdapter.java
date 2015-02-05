package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;

import com.lhsoft.pda.R;

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

public class DimensionsListAdapter extends BaseAdapter{

	public class DimensionItem {
		public Integer number;
		public Integer width;
		public Integer depth;
		public Integer height;
	}
	
	private ArrayList<DimensionItem> mDimensions;
	
	public DimensionsListAdapter() {
		mDimensions = new ArrayList<DimensionItem>();
		mDimensions.clear();
	}
	
	synchronized public void addDimensionItem(Integer number, Integer width, Integer depth, Integer height) {
		DimensionItem di = new DimensionItem();
		di.number = number;
		di.width = width;
		di.depth = depth;
		di.height = height;
		
		mDimensions.add(di);
	}
	
	public DimensionItem getDimensionItem(int index) {
		return mDimensions.get(index);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDimensions.size();
	}

	@Override
	public DimensionItem getItem(int position) {
		// TODO Auto-generated method stub
		return mDimensions.get(position);
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
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dimensions_item, parent, false);
		} else {
			view = convertView;
		}
		
		//final Resources res = parent.getResources();
		
		TextView palletNoLabel = (TextView) view.findViewById(R.id.dimensions_item_pallet_no);
		EditText widthEdit = (EditText) view.findViewById(R.id.dimensions_item_width);
		EditText depthEdit = (EditText) view.findViewById(R.id.dimensions_item_depth);
		EditText heightEdit = (EditText) view.findViewById(R.id.dimensions_item_height);
		
		widthEdit.setTag(position);
		depthEdit.setTag(position);
		heightEdit.setTag(position);
		
		final DimensionItem di = getItem(position);
		palletNoLabel.setText(di.number.toString());
		widthEdit.setText(di.width.toString());
		depthEdit.setText(di.depth.toString());
		heightEdit.setText(di.height.toString());	
		
		widthEdit.addTextChangedListener(new TextWatcher() {
			
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
				
				di.width = newValue;
			}
		});
		
		depthEdit.addTextChangedListener(new TextWatcher() {
			
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
				
				di.depth = newValue;
			}
		});
		
		heightEdit.addTextChangedListener(new TextWatcher() {
			
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
				
				di.height = newValue;
			}
		});
		
		return view;
	}
}
