package com.lhsoft.pda.ui.adapters;

import com.lhsoft.pda.R;
import com.lhsoft.pda.utils.Oerp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class PhotoListAdapter extends BaseAdapter{

	private OnAdapterListener mAdapterListener;
	private int mPackCount;
	private String mPackType;
	
	private boolean[][] mPhotoData;
	
	public PhotoListAdapter() {
		mPackType = "";
		mPackCount = 0;
	}
	
	public void setData(String packType, int packCount) {
		mPackType = packType;
		mPackCount = packCount;
		
		mPhotoData = new boolean[packCount][2];
		
		notifyDataSetChanged();
	}

	public void setCheck(int position, int photo) {
		mPhotoData[position][photo] = true;
	}
	
	public void setAdapterListener(OnAdapterListener adapter) {
		mAdapterListener = adapter;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPackCount;
	}

	@Override
	public boolean[] getItem(int position) {
		// TODO Auto-generated method stub
		return mPhotoData[position];
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
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
		} else {
			view = convertView;
		}
		
		final Resources res = parent.getResources();
		boolean[] photo = getItem(position);
		
		Button photo1Button = (Button) view.findViewById(R.id.take_photo1);
		Button photo2Button = (Button) view.findViewById(R.id.take_photo2);
		
		if (photo[0]) {
			photo1Button.setBackground(res.getDrawable(R.drawable.toggle_on_background));
			photo1Button.setTextColor(Color.WHITE);
		} else {
			photo1Button.setBackground(res.getDrawable(R.drawable.button_background));
		}
		
		if (photo[1]) {
			photo2Button.setBackground(res.getDrawable(R.drawable.toggle_on_background));
			photo2Button.setTextColor(Color.WHITE);
		} else {
			photo2Button.setBackground(res.getDrawable(R.drawable.button_background));
		}
		
		if (mPackType.equals(Oerp.PACKAGE_TYPE_BOX)) {
			photo1Button.setText(String.format(res.getString(R.string.photo_box_button1_label), position + 1));
			photo2Button.setText(String.format(res.getString(R.string.photo_box_button2_label), position + 1));
		} else if (mPackType.equals(Oerp.PACKAGE_TYPE_PALLET)) {
			photo1Button.setText(String.format(res.getString(R.string.photo_pallet_button1_label), position + 1));
			photo2Button.setText(String.format(res.getString(R.string.photo_pallet_button2_label), position + 1));
		}
		
		photo1Button.setTag(position);
		photo2Button.setTag(position);
		
		final int pos = position; 
		photo1Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mAdapterListener.onPhoto(pos, 0);
			}
			
		});

		photo2Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mAdapterListener.onPhoto(pos, 1);
			}
			
		});
		
		return view;
	}

	public static interface OnAdapterListener {
		public void onPhoto(int position, int photo);
	}
}
