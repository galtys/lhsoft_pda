package com.lhsoft.pda.ui.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import com.lhsoft.pda.R;

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
	
	private HashMap<Integer, DimensionItem> mDimensions;
	
	public DimensionsListAdapter() {
		mDimensions = new HashMap<Integer, DimensionItem>();
		mDimensions.clear();
	}
	
	synchronized public void addDimensionItem(boolean trash, Integer number, Integer width, Integer depth, Integer height) {
		DimensionItem di = new DimensionItem();
		di.trash = trash;
		di.number = number;
		di.width = width;
		di.depth = depth;
		di.height = height;
		
		mDimensions.put(number, di);
	}
	
	synchronized private void setTrash(int position, boolean newTrash) {
		DimensionItem di = mDimensions.get(position + 1);
		di.trash = newTrash;
	}
	
	synchronized private void setWidth(int position, Integer newWidth) {
		DimensionItem di = mDimensions.get(position + 1);
		di.width = newWidth;
	}
	
	synchronized private void setDepth(int position, Integer newDepth) {
		DimensionItem di = mDimensions.get(position + 1);
		di.depth = newDepth;
	}
	
	synchronized private void setHeight(int position, Integer newHeight) {
		DimensionItem di = mDimensions.get(position + 1);
		di.height = newHeight;
	}
	
	public DimensionItem getDimensionItem(int index) {
		return mDimensions.get(index + 1);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDimensions.size();
	}

	@Override
	public DimensionItem getItem(int position) {
		// TODO Auto-generated method stub
		return mDimensions.get(position + 1);
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

		Log.e(TAG, position + " " + di.width + " " + di.height + " " + di.depth);
		trashButton.setChecked(di.trash);
		palletNoLabel.setText(di.number.toString());
		widthEdit.setText(di.width.toString());
		depthEdit.setText(di.depth.toString());
		heightEdit.setText(di.height.toString());
		
		trashButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int pos = ((Integer) buttonView.getTag()).intValue();
				setTrash(pos, isChecked);
			}
		});
		
		widthEdit.addTextChangedListener(new CustomTextWatcher(widthEdit, position, CustomTextWatcher.WATCHER_WIDTH));
		depthEdit.addTextChangedListener(new CustomTextWatcher(depthEdit, position, CustomTextWatcher.WATCHER_DEPTH));
		heightEdit.addTextChangedListener(new CustomTextWatcher(heightEdit, position, CustomTextWatcher.WATCHER_HEIGHT));
		
		View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if (hasFocus) {
					Handler handler = new Handler();
					handler.post(new Runnable() {
						public void run() {
							Log.d(TAG, "focus on");
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							EditText numEdit = (EditText) v;
							String text = numEdit.getText().toString();
							numEdit.setText(text);
							numEdit.selectAll();
						}
					});
				}
			}
		};
		
		widthEdit.setOnFocusChangeListener(focusChangeListener);
		depthEdit.setOnFocusChangeListener(focusChangeListener);
		heightEdit.setOnFocusChangeListener(focusChangeListener);
		
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
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
		
	}
}
