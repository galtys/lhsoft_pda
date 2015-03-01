package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.lhsoft.pda.R;
import com.lhsoft.pda.utils.Oerp;

public class PhotoListAdapter extends BaseAdapter{

	private static final String TAG = "PhotoListAdapter";

	private OnAdapterListener mAdapterListener;

	public class PhotoItem {
		public Integer number;
		public boolean[] photo = new boolean[2];
	}

	private ArrayList<PhotoItem> mPhotoList;
	private String mPackType;

	public PhotoListAdapter() {
		mPackType = "";
		mPhotoList = new ArrayList<PhotoItem>();
	}

	public void setPackType(String packType) {
		mPackType = packType;

		notifyDataSetChanged();
	}

	public void addPhotoItem(Integer number, boolean photo1, boolean photo2) {
		PhotoItem pi = new PhotoItem();

		pi.number = number;
		pi.photo[0] = photo1;
		pi.photo[1] = photo2;

		mPhotoList.add(pi);
	}

	public void sort() {
		Collections.sort(mPhotoList, new Comparator<PhotoItem>() {
			@Override
			public int compare(PhotoItem lhs, PhotoItem rhs) {
				return lhs.number.compareTo(rhs.number);
			}
		});
	}
	
	public void setCheck(int position, int photo) {
		PhotoItem pi = mPhotoList.get(position);
		pi.photo[photo] = true;
	}

	public void setAdapterListener(OnAdapterListener adapter) {
		mAdapterListener = adapter;
	}

	@Override
	public int getCount() {
		return mPhotoList.size();
	}

	@Override
	public PhotoItem getItem(int position) {
		return mPhotoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
		} else {
			view = convertView;
		}

		final Resources res = parent.getResources();
		PhotoItem pi = getItem(position);

		Button photo1Button = (Button) view.findViewById(R.id.take_photo1);
		Button photo2Button = (Button) view.findViewById(R.id.take_photo2);

		if (pi.photo[0]) {
			photo1Button.setBackground(res.getDrawable(R.drawable.toggle_on_background));
			photo1Button.setTextColor(Color.WHITE);
		} else {
			photo1Button.setBackground(res.getDrawable(R.drawable.button_background));
		}

		if (pi.photo[1]) {
			photo2Button.setBackground(res.getDrawable(R.drawable.toggle_on_background));
			photo2Button.setTextColor(Color.WHITE);
		} else {
			photo2Button.setBackground(res.getDrawable(R.drawable.button_background));
		}

		if (mPackType.equals(Oerp.PACKAGE_TYPE_BOX)) {
			photo1Button.setText(String.format(res.getString(R.string.photo_box_button1_label), pi.number));
			photo2Button.setText(String.format(res.getString(R.string.photo_box_button2_label), pi.number));
		} else if (mPackType.equals(Oerp.PACKAGE_TYPE_PALLET)) {
			photo1Button.setText(String.format(res.getString(R.string.photo_pallet_button1_label), pi.number));
			photo2Button.setText(String.format(res.getString(R.string.photo_pallet_button2_label), pi.number));
		}

		photo1Button.setTag(pi.number);
		photo2Button.setTag(pi.number);

		final int pos = position;
		final int number = pi.number;
		photo1Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mAdapterListener.onPhoto(pos, number, 0);
			}

		});

		photo2Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mAdapterListener.onPhoto(pos, number, 1);
			}

		});

		return view;
	}

	public static interface OnAdapterListener {
		public void onPhoto(int position, int number, int photo);
	}
}
