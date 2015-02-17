package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;

import com.lhsoft.pda.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HomeListAdapter extends BaseAdapter{

	private static final String TAG = "HomeListAdapter";

	public class HomeItem {
		public String pickingName;
		public boolean stage1;
		public boolean stage2;
		public String carrier;
	}
	
	private ArrayList<HomeItem> mHomes;
	
	public HomeListAdapter() {
		mHomes = new ArrayList<HomeItem>();
		mHomes.clear();
	}
	
	synchronized public void addHomeItem(String pickingName, boolean stage1, boolean stage2, String carrier) {
		HomeItem mi = new HomeItem();
		mi.pickingName = pickingName;
		mi.stage1 = stage1;
		mi.stage2 = stage2;
		mi.carrier = carrier;
		
		mHomes.add(mi);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mHomes.size();
	}

	@Override
	public HomeItem getItem(int position) {
		// TODO Auto-generated method stub
		return mHomes.get(position);
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
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
		} else {
			view = convertView;
		}
		
		TextView pickingNameLabel = (TextView) view.findViewById(R.id.home_item_picking_name);
		ToggleButton stage1Button = (ToggleButton) view.findViewById(R.id.home_item_stage1);
		ToggleButton stage2Button = (ToggleButton) view.findViewById(R.id.home_item_stage2);
		TextView carrierLabel = (TextView) view.findViewById(R.id.home_item_carrier);
			
		HomeItem hi = getItem(position);
		pickingNameLabel.setText(hi.pickingName);
		
		stage1Button.setChecked(hi.stage1);
		stage1Button.setEnabled(false);
		
		stage2Button.setChecked(hi.stage2);
		stage2Button.setEnabled(false);
		
		carrierLabel.setText(hi.carrier);
		
		return view;
	}

}
