package com.martin.php;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月8日 上午11:50:02
 */
public class CookieHeader {
	
	private Map<String,String> cookies = new HashMap<String, String>();
	
	public void addCookie(String key,String value) {
		cookies.put(key, value);
	}
	
	public void remove(String key) {
		cookies.remove(key);
	}
	
	public String getValue(String key) {
		return cookies.get(key);
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iterator = cookies.keySet().iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			buffer.append(name+"="+cookies.get(name)).append(";");
		}
		if (buffer.length()>0) {
			return buffer.substring(0, buffer.length()-1);
		}else {
			return "";
		}
	};
}
