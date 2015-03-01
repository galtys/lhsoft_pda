package com.lhsoft.pda.utils;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.xmlrpc.android.XMLRPCClient;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.lhsoft.pda.R;
import com.lhsoft.pda.utils.xmlrpc.XMLRPCMethod;

public class Oerp {
	private static final String TAG = "Oerp";

	public static final String PICKING_TYPE_IN = "in";
	public static final String PICKING_TYPE_OUT = "out";
	public static final String PICKING_STATE_ASSIGNED = "assigned";
	public static final String PICKING_STATE_DONE = "done";
	
	public static final String SCREEN_HOME = "home";
	public static final String SCREEN_MAIN = "main";
	public static final String SCREEN_MAIN_RETURN = "main_return";
	public static final String SCREEN_DIMENSIONS = "dimensions";
	public static final String SCREEN_PICKING_PARTIAL = "partial";
	public static final String SCREEN_RETURN_PARTIAL = "return_partial";
	public static final String SCREEN_PHOTO = "photo";

	public static final String PACKAGE_TYPE_BOX = "box";
	public static final String PACKAGE_TYPE_PALLET = "pallet";
	public static final String PACKAGE_TYPE_NA = "na";

	public static final String QTY_TYPE_PARTIAL = "partial";
	public static final String QTY_TYPE_FULL = "full";
	public static final String QTY_TYPE_NA = "na";

	public static final String BUTTON_NONE = "none";
	public static final String BUTTON_CONTINUE = "continue";
	public static final String BUTTON_PHOTO = "photo";
	public static final String BUTTON_HOME = "home";
	public static final String BUTTON_SCAN = "scan";
	public static final String BUTTON_FETCH_DIMENSIONS = "fetch_dimensions";
	public static final String BUTTON_ACTIVATED = "activated";

	public static final String PICKING_FIELD_NAME = "name";
	public static final String PICKING_FIELD_TYPE = "type";
	public static final String PICKING_FIELD_NEXT_SCREEN = "next_screen";
	public static final String PICKING_FIELD_QTY_TYPE = "is_partial";
	public static final String PICKING_FIELD_PACK_TYPE = "package_type";
	public static final String PICKING_FIELD_PACK_IDS = "package_ids";
	public static final String PICKING_FIELD_PACK_COUNT = "no_packages";
	public static final String PICKING_FIELD_TODAY = "today";
	public static final String PICKING_FIELD_MOVE_LINES = "move_lines";
	public static final String PICKING_FIELD_STATE = "state";
	public static final String PICKING_FIELD_STAGE1 = "stage1";
	public static final String PICKING_FIELD_STAGE2 = "stage2";
	public static final String PICKING_FIELD_CARRIER = "pjb_carrier_id";
	public static final String PICKING_FIELD_DELIVERY_PARTNER = "delivery_partner_id";
	public static final String PICKING_FIELD_ALLOW = "allow_pda";
	
	
	public static final String TRACKING_FIELD_PICKING = "picking_id";
	public static final String TRACKING_FIELD_TRASH = "trash";
	public static final String TRACKING_FIELD_NUMBER = "number";
	public static final String TRACKING_FIELD_WIDTH = "w";
	public static final String TRACKING_FIELD_DEPTH = "d";
	public static final String TRACKING_FIELD_HEIGHT = "h";
	public static final String TRACKING_FIELD_PHOTO1 = "has_photo1";
	public static final String TRACKING_FIELD_PHOTO2 = "has_photo2";
	
	public static final String STOCK_MOVE_FIELD_NAME = "name";
	public static final String STOCK_MOVE_FIELD_DATE = "date";
	public static final String STOCK_MOVE_FIELD_PRODUCT_ID = "product_id";
	public static final String STOCK_MOVE_FIELD_PRODUCT_QTY = "product_qty";
	public static final String STOCK_MOVE_FIELD_TO_FOLLOW = "to_follow";
	public static final String STOCK_MOVE_FIELD_RETURNED = "returned";
	public static final String STOCK_MOVE_FIELD_REPAIR = "repair";
	
	private static final String API_URL = "server";
	private static final String USER_NAME = "username";
	private static final String PASSWORD = "password";
	private static final String DATABASE = "database";
	
	private Context mContext = null;
	private static Oerp mInstance = null;
	private XMLRPCClient mClient;
	private Integer mUid;
	private boolean mCredentialLoad;
	
	private String mApiUrl;
	private String mUserName;
	private String mPassword;
	private String mDatabase;

	static public Oerp getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Oerp(context);
		}
		return mInstance;
	}

	public Oerp(Context context) {
		mContext = context;
	}
	
	public void loadDefaultCredentials() {
		mApiUrl = "http://galtys.com:8069/";
		mUserName = "admin";
		mPassword = "ENFIELD8ABA100";
		mDatabase = "pjb-2015-02-16_1533";
	}
	
	public int loadCredentials() {
		String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String confFileName = internalStoragePath + "/cfg/pda.conf";
		
		return loadCredentials(confFileName);
	}
	
	public int loadCredentials(String filePath) {
		mCredentialLoad = false;
		
		ConfFileParser cfp = new ConfFileParser(filePath);
		
		HashMap<String, String> result = new HashMap<String, String>();
		//int err = cfp.parse(result);
		
		Properties prop = new Properties();
		InputStream is;
		
		try {
			is = new FileInputStream(filePath);
			try {
				prop.load(is);

				mApiUrl = prop.getProperty(API_URL);
				if (mApiUrl == null) {
					return ConfFileParser.PARSE_ERROR_OTHER;
				}
				
				mUserName = prop.getProperty(USER_NAME);
				if (mUserName == null) {
					return ConfFileParser.PARSE_ERROR_OTHER;
				}
				
				mPassword = prop.getProperty(PASSWORD);
				if (mPassword == null) {
					return ConfFileParser.PARSE_ERROR_OTHER;
				}
				
				mDatabase = prop.getProperty(DATABASE);
				if (mDatabase == null) {
					return ConfFileParser.PARSE_ERROR_OTHER;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return ConfFileParser.PARSE_ERROR_OTHER;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ConfFileParser.PARSE_ERROR_NOEXIST;
		}
				
		Log.d(TAG, "Server: " + mApiUrl);
		Log.d(TAG, "Username: " + mUserName);
		Log.d(TAG, "Password: " + mPassword);
		Log.d(TAG, "Database: " + mDatabase);
		
		mCredentialLoad = true;
		return ConfFileParser.PARSE_OK;
	}
	
	public boolean isConnected() {
		return (mClient != null);
	}

	public void connect(final XMLRPCMethod.XMLRPCMethodCallback callback) {

		if (!mCredentialLoad) {
			callback.failed(mContext.getResources().getString(R.string.credentials_not_load_message));
			return;
		}
		
		XMLRPCClient common = new XMLRPCClient(mApiUrl + "xmlrpc/common");

		mClient = null;
		mUid = null;

		XMLRPCMethod method = new XMLRPCMethod(common, "login", new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				Log.d(TAG, "Uid = " + result.toString());

				mUid = Integer.valueOf(result.toString());
				
				mClient = new XMLRPCClient(mApiUrl + "xmlrpc/object");

				callback.succesed(null);
			}

			@Override
			public void failed(String message) {
				callback.failed(message);
			}
		});
		Object[] params = {
				mDatabase, mUserName, mPassword
		};
		method.call(params);
	}

	private synchronized void read(String model, Integer[] ids, String[] fnames, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, model, "read", ids, fnames
		};
		method.call(params);
	}

	private synchronized void write(String model, Integer id, HashMap<String, Object> value, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, model, "write", id, value
		};
		method.call(params);
	}

	private synchronized void search(String model, Object[][] domain, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, model, "search", domain
		};
		
		method.call(params);
	}

	public void getPhotos(Integer pId, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "test_photos", pId
		};
		method.call(params);
	}
	
	public void testHasPhotos(Integer pId, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "test_has_photos", pId
		};
		method.call(params);
	}
	
	public void uploadPhoto(Integer pId, Integer number, Integer photoNumber, byte[] binaryData, XMLRPCMethod.XMLRPCMethodCallback callback) {
		String photoString = "photo" + photoNumber; 
		Log.d(TAG, "Number = " + number + " Photo = " + photoString);
		
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "upload_photo", pId, number, photoString, binaryData
		};
		method.call(params);
	}

	public void getDefault(XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "defaults"
		};
		method.call(params);
	}

	public void getCurrentPickings(XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "get_current_pickings"
		};
		method.call(params);
	}
	
	public void getPickingId(String pickingName, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		String[][] condition = {{PICKING_FIELD_NAME, "=", pickingName}};
		this.search("stock.picking", condition, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (result == null) {
					callback.succesed(null);
					return;
				}

				Object[] arr = (Object[])result;
				if (arr.length > 0)
					callback.succesed(arr[0]);
				else
					callback.succesed(null);
			}

			@Override
			public void failed(String message) {
				callback.failed(message);
			}
		});
	}

	public void setDefault(Integer pId, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "set_to_defaults", pId
		};
		method.call(params);
	}

	public void getPicking(Integer pId, XMLRPCMethod.XMLRPCMethodCallback callback) {
		Integer[] pIds = { pId };
		getPicking(pIds, callback);
	}
	
	public void getPicking(Integer[] pIds, XMLRPCMethod.XMLRPCMethodCallback callback) {
		String[] fnames = {
				"id",
				PICKING_FIELD_NAME,
				PICKING_FIELD_TYPE,
				PICKING_FIELD_NEXT_SCREEN,
				PICKING_FIELD_QTY_TYPE,
				PICKING_FIELD_PACK_TYPE,
				PICKING_FIELD_PACK_IDS,
				PICKING_FIELD_PACK_COUNT,
				PICKING_FIELD_TODAY,
				PICKING_FIELD_MOVE_LINES,
				PICKING_FIELD_STATE,
				PICKING_FIELD_STAGE1,
				PICKING_FIELD_STAGE2,
				PICKING_FIELD_CARRIER,
				PICKING_FIELD_DELIVERY_PARTNER,
				PICKING_FIELD_ALLOW
		};
		this.read("stock.picking", pIds, fnames, callback);
	}

	public void updatePicking(Integer pId, Integer packCount, String packType, String isPartial, String button, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "update_state", pId, packCount, packType, isPartial, button
		};
		method.call(params);
	}

	public void updateDimensions(Integer pId, Object[] dimensions, XMLRPCMethod.XMLRPCMethodCallback callback) {
		XMLRPCMethod method = new XMLRPCMethod(mClient, "execute", callback);
		Object[] params = {
				mDatabase, mUid, mPassword, "stock.picking", "upload_dimensions", pId, dimensions
		};
		method.call(params);
	}
	
	public void getTracking(Integer pId, Integer number, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		getDimension(pId, number, callback);
	}
	
	public void getDimension(Integer pId, Integer number, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		Object[][] condition = {
				{ TRACKING_FIELD_PICKING, "=", pId },
				{ TRACKING_FIELD_NUMBER, "=", number }
		};

		this.search("stock.tracking", condition, new XMLRPCMethod.XMLRPCMethodCallback() {

			@Override
			public void succesed(Object result) {
				if (result == null) {
					callback.succesed(null);
					return;
				}

				Object[] ary = (Object[]) result;
				Integer[] dId = { Integer.valueOf(ary[0].toString()) };
				String[] fnames = {
						TRACKING_FIELD_TRASH,
						TRACKING_FIELD_NUMBER, 
						TRACKING_FIELD_WIDTH, 
						TRACKING_FIELD_DEPTH, 
						TRACKING_FIELD_HEIGHT,
						TRACKING_FIELD_PHOTO1,
						TRACKING_FIELD_PHOTO2
				};

				read("stock.tracking", dId, fnames, new XMLRPCMethod.XMLRPCMethodCallback() {

					@Override
					public void succesed(Object result) {
						callback.succesed(result);
					}

					@Override
					public void failed(String message) {
						callback.failed(message);
					}
				});
			}

			@Override
			public void failed(String message) {
				callback.failed(message);
			}
		});
	}

	public void fetchDimensions() {
		
	}
	
	public void getStockMoves(Integer[] moveIds, final XMLRPCMethod.XMLRPCMethodCallback callback) {
		String[] fnames = {
				STOCK_MOVE_FIELD_NAME, 
				STOCK_MOVE_FIELD_DATE, 
				STOCK_MOVE_FIELD_PRODUCT_ID, 
				STOCK_MOVE_FIELD_PRODUCT_QTY, 
				STOCK_MOVE_FIELD_TO_FOLLOW, 
				STOCK_MOVE_FIELD_RETURNED, 
				STOCK_MOVE_FIELD_REPAIR 
		};
		this.read("stock.move", moveIds, fnames, callback);
	}

	public void updateToFollow(Integer moveId, Integer toFollow, XMLRPCMethod.XMLRPCMethodCallback callback) {
		HashMap<String, Object> value = new HashMap<String, Object>();
		value.put(STOCK_MOVE_FIELD_TO_FOLLOW, toFollow);
		this.write("stock.move", moveId, value, callback);
	}

	public void updateReturnRepair(Integer moveId, Integer returned, Integer repair, XMLRPCMethod.XMLRPCMethodCallback callback) {
		HashMap<String, Object> value = new HashMap<String, Object>();
		value.put(STOCK_MOVE_FIELD_RETURNED, returned);
		value.put(STOCK_MOVE_FIELD_REPAIR, repair);
		this.write("stock.move", moveId, value, callback);
	}
}
