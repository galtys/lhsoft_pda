package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.lhsoft.pda.R;
import com.lhsoft.pda.ui.adapters.PhotoListAdapter.PhotoItem;
import com.lhsoft.pda.utils.SharedVars;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DimensionsListAdapter extends BaseAdapter{

	private static final String TAG = "DimensionsListAdapter";
	
	public class DimensionItem {
		public boolean trash;
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
	
	synchronized public void addDimensionItem(boolean trash, Integer number, Integer width, Integer depth, Integer height) {
		DimensionItem di = new DimensionItem();
		di.trash = trash;
		di.number = number;
		di.width = width;
		di.depth = depth;
		di.height = height;
		
		mDimensions.add(di);
	}
	
	public void sort() {
		Collections.sort(mDimensions, new Comparator<DimensionItem>() {
			@Override
			public int compare(DimensionItem lhs, DimensionItem rhs) {
				return lhs.number.compareTo(rhs.number);
			}
		});
	}
	
	synchronized private void setTrash(int position, boolean newTrash) {
		DimensionItem di = mDimensions.get(position);
		di.trash = newTrash;
	}
	
	synchronized private void setWidth(int position, Integer newWidth) {
		DimensionItem di = mDimensions.get(position);
		di.width = newWidth;
	}
	
	synchronized private void setDepth(int position, Integer newDepth) {
		DimensionItem di = mDimensions.get(position);
		di.depth = newDepth;
	}
	
	synchronized private void setHeight(int position, Integer newHeight) {
		DimensionItem di = mDimensions.get(position);
		di.height = newHeight;
	}
	
	public DimensionItem getDimensionItem(int index) {
		return mDimensions.get(index);
	}
	
	@Override
	public int getCount() {
		return mDimensions.size();
	}

	@Override
	public DimensionItem getItem(int position) {
		return mDimensions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dimensions_item, parent, false);
		} else {
			view = convertView;
		}
		
		ToggleButton trashButton = (ToggleButton) view.findViewById(R.id.dimensions_item_trash);
		TextView palletNoLabel = (TextView) view.findViewById(R.id.dimensions_item_pallet_no);
		EditText widthEdit = (EditText) view.findViewById(R.id.dimensions_item_width);
		EditText depthEdit = (EditText) view.findViewById(R.id.dimensions_item_depth);
		EditText heightEdit = (EditText) view.findViewById(R.id.dimensions_item_height);

		trashButton.setTag(position);
		widthEdit.setTag(position);
		depthEdit.setTag(position);
		heightEdit.setTag(position);
		
		DimensionItem di = getItem(position);

		Log.e(TAG, position + " " + di.width + " " + di.height + " " + di.depth + " " + di.trash);
		trashButton.setChecked(!di.trash);
		palletNoLabel.setText(di.number.toString());
		widthEdit.setText(di.width.toString());
		depthEdit.setText(di.depth.toString());
		heightEdit.setText(di.height.toString());
		
		trashButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int pos = ((Integer) buttonView.getTag()).intValue();
				setTrash(pos, !isChecked);
			}
		});
		
		widthEdit.addTextChangedListener(new CustomTextWatcher(widthEdit, position, CustomTextWatcher.WATCHER_WIDTH));
		depthEdit.addTextChangedListener(new CustomTextWatcher(depthEdit, position, CustomTextWatcher.WATCHER_DEPTH));
		heightEdit.addTextChangedListener(new CustomTextWatcher(heightEdit, position, CustomTextWatcher.WATCHER_HEIGHT));
		
		widthEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
		depthEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
		heightEdit.setOnFocusChangeListener(SharedVars.mFocusChangeListener);
		
		return view;
	}
	
	private class CustomTextWatcher implements TextWatcher {
		private EditText mEditText;
		private int mPosition;
		private int mWatcher;
		public static final int WATCHER_WIDTH = 0;
		public static final int WATCHER_DEPTH = 1;
		public static final int WATCHER_HEIGHT = 2;
		
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
				if (mWatcher == WATCHER_WIDTH) {
					setWidth(mPosition, newValue);
				} else if (mWatcher == WATCHER_DEPTH) {
					setDepth(mPosition, newValue);
				} else if (mWatcher == WATCHER_HEIGHT) {
					setHeight(mPosition, newValue);
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
