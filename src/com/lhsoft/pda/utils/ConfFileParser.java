package com.lhsoft.pda.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfFileParser {
	private static final String TAG = "ConfFileParser";
	
	public static final int PARSE_OK = 0;
	public static final int PARSE_ERROR_NOFILE = 1;
	public static final int PARSE_ERROR_NOEXIST = 2;
	public static final int PARSE_ERROR_OTHER = 100;
	
	private String mFileName;
	
	public ConfFileParser() {
		mFileName = "";
	}
	
	public ConfFileParser(String fileName) {
		mFileName = fileName;
	}
	
	public void setFileName(String fileName) {
		mFileName = fileName;
	}
	
	public int parse(HashMap<String, String> result) {
		if (mFileName.isEmpty()) {
			return PARSE_ERROR_NOFILE;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(mFileName));
			String line;
			while ((line = br.readLine()) != null ) {
				String[] ary = line.split("=", 2);
				result.put(ary[0], ary[1]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return PARSE_ERROR_NOEXIST;
		} catch (IOException e) {
		    e.printStackTrace();
		    return PARSE_ERROR_OTHER;
		}
		
		return PARSE_OK;
	}
}
