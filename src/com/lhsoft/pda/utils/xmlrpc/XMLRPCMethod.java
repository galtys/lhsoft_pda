package com.lhsoft.pda.utils.xmlrpc;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.conn.HttpHostConnectException;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;

import android.os.Handler;
import android.util.Log;

public class XMLRPCMethod extends Thread {
	private static final String TAG = "XMLRPCMethod";

	private static AtomicBoolean mRunning = new AtomicBoolean(false);
	
	private XMLRPCClient mClient;

	private String mMethod;
	private Object[] mParams;
	private Handler mHandler;
	private XMLRPCMethodCallback mCallBack;

	public XMLRPCMethod(XMLRPCClient client, String method, XMLRPCMethodCallback callBack) {
		this.mClient = client;
		this.mMethod = method;
		this.mCallBack = callBack;
		mHandler = new Handler();
	}
	public void call() {
		call(null);
	}
	public void call(Object[] params) {

		Log.d(TAG, "Calling host");
		this.mParams = params;
		start();
	}
	@Override
	public void run() {
		try {
			Log.d(TAG, "Waiting other calling");
			while (mRunning.get()) {
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mRunning.set(true);
			
			Log.d(TAG, "Start calling");
			
			final long t0 = System.currentTimeMillis();
			final Object result = mClient.callEx(mMethod, mParams);
			final long t1 = System.currentTimeMillis();
			
			Log.d(TAG, "End calling");
			
			mRunning.set(false);
			
			mHandler.post(new Runnable() {
				public void run() {
					Log.d(TAG, "XML-RPC call took " + (t1-t0) + "ms");
					mCallBack.succesed(result);
				}
			});
		} catch (final XMLRPCFault e) {
			mHandler.post(new Runnable() {
				public void run() {
					Log.d(TAG, "Fault message: " + e.getFaultString() + "\nFault code: " + e.getFaultCode());
					mCallBack.failed(e.getFaultString());
				}
			});
		} catch (final XMLRPCException e) {
			mHandler.post(new Runnable() {
				public void run() {

					Throwable couse = e.getCause();
					if (couse instanceof HttpHostConnectException) {
						Log.d(TAG, "Cannot connect to server");
						mCallBack.failed("Cannot connect to server");
					} else {
						Log.d(TAG, "Error " + e.getMessage());
						mCallBack.failed(e.getMessage());
					}
					
				}
			});
		}
	}

	public interface XMLRPCMethodCallback {
		void succesed(Object result);
		void failed(String message);
	}

}