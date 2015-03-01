package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import com.lhsoft.pda.R;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HomeListAdapter extends BaseAdapter{

	private static final String TAG = "HomeListAdapter";

	private OnPickingClickListener mPickingClickListener;
	
	public class HomeItem {
		public String pickingName;
		public boolean stage1;
		public boolean stage2;
		public String carrier;
	}
	
	private HashMap<Integer, HomeItem> mHomes;
	
	public HomeListAdapter(OnPickingClickListener pickingClickListener) {
		mHomes = new HashMap<Integer, HomeItem>();
		mHomes.clear();
		
		mPickingClickListener = pickingClickListener;
	}
	
	synchronized public void addHomeItem(Integer order, String pickingName, boolean stage1, boolean stage2, String carrier) {
		HomeItem hi = new HomeItem();
		hi.pickingName = pickingName;
		hi.stage1 = stage1;
		hi.stage2 = stage2;
		hi.carrier = carrier;
		
		mHomes.put(order, hi);
	}
	
	public void clear() {
		mHomes.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mHomes.size();
	}

	@Override
	public HomeItem getItem(int position) {
		return mHomes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
		
		SpannableString content = new SpannableString(hi.pickingName);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		pickingNameLabel.setText(content);
		
		stage1Button.setChecked(hi.stage1);
		stage1Button.setEnabled(false);
		
		stage2Button.setChecked(hi.stage2);
		stage2Button.setEnabled(false);
		
		carrierLabel.setText(hi.carrier);
		
		pickingNameLabel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String pickingName = ((TextView) v).getText().toString();
				mPickingClickListener.onClick(pickingName);
			}
		});
		
		return view;
	}

	public interface OnPickingClickListener {
		public void onClick(String pickingName);
	}
}
