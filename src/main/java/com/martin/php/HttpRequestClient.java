package com.martin.php;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月7日 上午9:46:33
 */
public class HttpRequestClient {

	private static Header[] defaultHeaders = new BasicHeader[] {
			new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"),
			new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
			 new BasicHeader("Accept-Encoding", "gzip, deflate"),
			new BasicHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31") };

	public static Map<String, String> submitRequest(String url, HttpMethod method, Map<String, String> headers,
			Map<String, String> body) throws Exception {
		Map<String, String> responses = new HashMap<String, String>();
		switch (method) {
		case Get:
			return doGet(url, headers);
		case Post:
			return doPost(url, headers, body);
		default:
			break;
		}
		return responses;

	}

	protected static Map<String, String> doGet(String url, Map<String, String> headers) throws Exception {
		System.out.println("http get url="+url);
		Map<String, String> responses = new HashMap<String, String>();
		// step1： 构造HttpClient的实例,类似于打开浏览器
		HttpClient httpClient = new DefaultHttpClient();
		// step2： 创建GET方法的实例，类似于在浏览器地址栏输入url
		HttpGet httpGet = new HttpGet(url);
		// 设置请求头
		Header[] requestHeaders = getHeaders(headers);
		httpGet.setHeaders(requestHeaders);
		try {
			// step3: 执行请求，让浏览器发出请求
			HttpResponse httpResponse = httpClient.execute(httpGet);
			proccessResult(responses, httpResponse);
			return responses;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			if (e.getLocalizedMessage().contains("401")) {
				return responses;
			}else {
				throw e;
			}
		}
	}

	protected static Map<String, String> doPost(String url, Map<String, String> headers, Map<String, String> keyvalues) throws IOException {
		System.out.println("post url="+url);
		Map<String, String> responses = new HashMap<String, String>();
		// step1： 构造HttpClient的实例,类似于打开浏览器
		HttpClient httpClient = new DefaultHttpClient();
		// step2： 创建POST方法的实例，类似于在浏览器地址栏输入url
		HttpPost httpPost = new HttpPost(url);
		// 设置请求头
		Header[] requestHeaders = getHeaders(headers);
		httpPost.setHeaders(requestHeaders);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (keyvalues != null && keyvalues.size() > 0) {
			Iterator<String> iterator = keyvalues.keySet().iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				params.add(new BasicNameValuePair(name, keyvalues.get(name)));
			}
		}
		try {
			// step3: 执行请求，让浏览器发出请求
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			printHttpHeader(httpPost);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			proccessResult(responses, httpResponse);
			return responses;
		} catch (IOException e) {
			// 发生网络异常
			e.printStackTrace();
			throw e;
		} 

	}

	private static void printHttpHeader(HttpRequestBase http) {
		
		System.out.println("********************************HttpRequest header*******************************************");
		Header[] headers = http.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
			System.out.println(headers[i].getName()+":"+headers[i].getValue());
		}
		System.out.println("********************************HttpRequest header*******************************************");
	}

	private static Header[] getHeaders(Map<String, String> headers) {
		Header[] requestHeaders = new BasicHeader[(headers != null ? headers.size() : 0) + defaultHeaders.length];
		int index = 0;
		if (headers != null) {
			Iterator<String> iterator = headers.keySet().iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				requestHeaders[index++] = new BasicHeader(name, headers.get(name));
			}
		}

		for (Header header : defaultHeaders) {
			requestHeaders[index++] = header;
		}
		return requestHeaders;
	}

	private static void proccessResult(Map<String, String> responses, HttpResponse httpResponse) throws IOException {
//		printResponseHeader(httpResponse);
		//获取返回头的信息
		if (httpResponse.getHeaders("Set-Cookie") != null) {
			Header[] responseHeaders = httpResponse.getHeaders("Set-Cookie");
			for (int i = 0; i < responseHeaders.length; i++) {
				responses.put("cookie-" + i, responseHeaders[i].getValue().split(";")[0]);
			}
		} else {
			if (httpResponse.getFirstHeader("Cookie") != null) {
				String set_cookie = httpResponse.getFirstHeader("Cookie").getValue();
				responses.put("cookie", set_cookie);
			}
		}

		if (httpResponse.getLastHeader("Location") != null) {
			String location = httpResponse.getLastHeader("Location").getValue();
			String[] locations = location.split(";");
			for (int i = 0; i < locations.length; i++) {
				if (locations[i].contains("jsessionid")) {
					responses.put("jsessionid", locations[i]);
				} else {
					responses.put("Location", locations[i]);
				}
			}
		}
		int code = httpResponse.getStatusLine().getStatusCode();
		System.out.println("httpResponse.getProtocolVersion:" + httpResponse.getProtocolVersion());
		System.out.println("HttpStatus = "+code);
		
		if (code != HttpStatus.SC_MOVED_TEMPORARILY && code != HttpStatus.SC_OK && code!=HttpStatus.SC_NO_CONTENT) {
			throw new RuntimeException("Method failed: " + httpResponse.getStatusLine());
		}
		// step4: 读取内容,浏览器返回结果
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			String charset = getContentCharSet(entity);
			String result = EntityUtils.toString(entity, charset);
			result.replaceAll("\r", "");// 去掉返回结果中的"\r"字符，否则会在结果字符串后面显示一个小方格
			responses.put("result", result);
		}
		
	}

	private static void printResponseHeader(HttpResponse httpResponse) {
		System.out.println("********************************httpResponse header*******************************************");
		Header[] headers = httpResponse.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
			System.out.println(headers[i].getName()+":"+headers[i].getValue());
		}
		System.out.println("********************************httpResponse header*******************************************");
	}

	public static String getContentCharSet(final HttpEntity entity) {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}

		if (charset == null || "".equals(charset.trim())) {
			charset = "UTF-8";
		}
		return charset;
	}

	public static Document getDocument(String xmlstr) throws Exception {
		Document document = DocumentHelper.parseText(xmlstr);
		return document;
	}
}
