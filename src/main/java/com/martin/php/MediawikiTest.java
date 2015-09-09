package com.martin.php;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月8日 下午12:45:18
 */
public class MediawikiTest {
	private String url ="http://172.19.106.248/mediawiki/index.php?title=%E7%89%B9%E6%AE%8A:%E7%94%A8%E6%88%B7%E7%99%BB%E5%BD%95&returnto=%E9%A6%96%E9%A1%B5";
	private static CookieHeader cookieHeader = new CookieHeader();
	public static void main(String[] args) throws Exception {
		MediawikiTest mediawiki = new MediawikiTest();
		mediawiki.login();
		
	}
	public void login() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, String> result=null;
		headers.put("Cookie", "");
		result = HttpRequestClient.doGet(url, headers);
//		saveCookieHead(result);
		String loginToken = getLoginToken(result);
		System.out.println("loginToken="+loginToken);
		
		Map<String, String> keyValues = new HashMap<String, String>();
//		keyValues.put("wpLoginToken", loginToken);
//		keyValues.put("wpPassword", "passw0rd");
		keyValues.put("wpName", "Admin");
		keyValues.put("wpLoginattempt", "%E7%99%BB%E5%BD%95");
		
		headers = new HashMap<String, String>();
		cookieHeader.addCookie("wikidbUserName", keyValues.get("wpName"));
		cookieHeader.addCookie("Hm_lvt_f5127c6793d40d199f68042b8a63e725",System.currentTimeMillis()+"");
		cookieHeader.addCookie("Hm_lpvt_f5127c6793d40d199f68042b8a63e725",(System.currentTimeMillis()+1000)+"");
		System.err.println(cookieHeader.toString());
		headers.put("Cookie", cookieHeader.toString());
		
//		headers.put("Referer", url);
		url ="http://172.19.106.248/mediawiki/index.php?title=%E7%89%B9%E6%AE%8A:%E7%94%A8%E6%88%B7%E7%99%BB%E5%BD%95&action=submitlogin&type=login&returnto=%E9%A6%96%E9%A1%B5";
		System.out.println("post------------------------------");
		result = HttpRequestClient.doPost(url, headers, keyValues);
//		System.out.println(result.get("result"));
	}
	
	private String getLoginToken(Map<String, String> result) {
		String token = null;
		String html = result.get("result");
		if (html == null) {
			return token;
		}
		int index = html.indexOf("wpLoginToken");
		if (index >=0) {
			index = html.indexOf("value",index);
			int endIndex = html.indexOf("/>", index);
			if (endIndex>index) {
				token = html.substring(index, endIndex).split("=")[1];
				if (token !=null) {
					token.trim();
					token = token.replaceAll("\"", "");
				}
			}
		}
		return token;
	}
	private static void saveCookieHead(Map<String, String> result) {
		int index = 0;
		String[] keyValue;
		while (true) {
			if (result.containsKey("cookie-" + index)) {
				keyValue = result.get("cookie-" + index).split("=");
				if (keyValue.length == 2) {
					cookieHeader.addCookie(keyValue[0], keyValue[1]);
				}
				index++;
			} else {
				break;
			}
		}
		System.out.println(cookieHeader.toString());
	}
}
