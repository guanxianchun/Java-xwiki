package com.martin.php;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月7日 下午2:28:18
 */
public class XWikiHander {

	private String httpServer = "http://localhost:8080";
	private CookieHeader cookieHeader = new CookieHeader();

	public static void main(String[] args) throws Exception {
		XWikiHander wiki = new XWikiHander();
		Map<String, String> result = wiki.login();
		wiki.saveCookieHead(result);
		result = wiki.requestUrl(result);
		Map<String, String> titles = wiki.getAllTitles(result.get("result"));//从html中
		wiki.printMap(titles);
		Document document = wiki.getTitleDocumnet(titles.get("gxc"));//获取标题对应的URI
		String text = document.getElementById("xwikicontent").html();//要提交的内容
		String edituri = getEditURI(document);
		System.out.println(text.toCharArray().length+":"+text);
		document = wiki.doGetRequest(edituri);
		Element formElement = document.getElementById("edit");
		String postURI = formElement.attr("action");
		String form_token = formElement.select("input[name=form_token]").val();
		System.out.println("form_token="+form_token);
		String dirverURI = document.getElementById("tmEditWysiwyg").attr("href");//使用my swiyg方式
		System.out.println("dirverURI="+dirverURI);
		String content = document.getElementById("content").html();
		System.out.println("content="+content);
		String mainParent = document.getElementById("xwikidocparentinput").val();
		System.out.println("mainParent="+mainParent);
		String editTitle = document.getElementById("xwikidoctitleinput").val();
		System.out.println("editTitle="+editTitle);
		Element selection = document.getElementById("xwikidocsyntaxinput2");
		String docSyntax=selection.select("option[selected=selected]").val();
		System.out.println("docSyntax="+docSyntax);
		// wiki.printWiki();
//		System.out.println(document.html());
		Map<String, String> keyValues = new HashMap<String, String>();
		keyValues.put("form_token", form_token);
		keyValues.put("parent", mainParent);
		keyValues.put("title", editTitle);
		keyValues.put("xcontinue", dirverURI);
		keyValues.put("RequiresHTMLConversion", "content");
		keyValues.put("content_syntax", docSyntax);
		keyValues.put("content", text+" gxc");
		keyValues.put("xeditaction", "edit");        
		keyValues.put("syntaxId", docSyntax);        
		keyValues.put("ajax", "false");
		keyValues.put("action_save", "Save & View");
		wiki.postEdit(postURI,edituri,keyValues);
		
	}

	public Document postEdit(String postURI, String edituri, Map<String, String> keyValues) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", cookieHeader.toString());
		headers.put("Referer", edituri);
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, String> result = HttpRequestClient.doPost(httpServer + postURI, headers,keyValues);
		if (result == null || result.get("result") == null || "".equals(result.get("result"))) {
			return null;
		}
		Document document = Jsoup.parse(result.get("result"));
		return document;
	}

	public static String getEditURI(Document document) {
		Element editDiv = document.getElementById("tmEdit");
		Element href = editDiv.getElementById("tmEditDefault");
		return href.attr("href");
	}

	public Document getTitleDocumnet(String uri) throws Exception {
		return doGetRequest(uri);
	}

	public Map<String, String> getAllTitles(String html) {
		Map<String, String> titleMap = new HashMap<String, String>();
		if (html == null || "".equals(html)) {
			return titleMap;
		}
		Document document = parseHtml(html);
		Element xwikicontent = document.getElementById("xwikicontent");
		Elements titles = xwikicontent.getElementsByClass("spSpaceName");
		Iterator<Element> iterator = titles.iterator();
		while (iterator.hasNext()) {
			Element element = (Element) iterator.next();
			if (element.childNodeSize() > 0) {
				Element href = element.child(0);
				titleMap.put(href.html(), href.attr("href"));
			}
		}
		return titleMap;
	}

	public Map<String, String> login() throws Exception {
		String url = httpServer+"/xwiki/bin/login/XWiki/XWikiLogin";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", "");
		Map<String, String> result = null;
		try {
			// result = HttpRequestClient.doGet(url, headers);
			result = auth(url, result);
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("401")) {// 没有登录或认证失败
				result = auth(url, result);
			}
		}
		return result;
	}

	public Map<String, String> accessHomePage(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		headers.put("Cookie", cookieHeader.toString());
		headers.put("Referer", httpServer+"/xwiki/bin/login/XWiki/XWikiLogin");
		return HttpRequestClient.doGet(url, headers);
	}
	
	public Map<String, String> requestUrl(Map<String, String> result) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		String url = httpServer+"/xwiki/bin/login/XWiki/XWikiLogin";
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		headers.put("Cookie", cookieHeader.toString());
		headers.put("Referer", url);
		result = HttpRequestClient.doGet(result.get("Location"), headers);
		return result;
	}

	public void setFormDatas(Document editDocument,Map<String, String> keyValues){
		Element formElement = editDocument.getElementById("edit");
		String postURI = formElement.attr("action");
		String form_token = formElement.select("input[name=form_token]").val();
		System.out.println("form_token="+form_token);
		String dirverURI = editDocument.getElementById("tmEditWysiwyg").attr("href");//使用my swiyg方式
		System.out.println("dirverURI="+dirverURI);
		String content = editDocument.getElementById("content").html();
		System.out.println("content="+content);
		String mainParent = editDocument.getElementById("xwikidocparentinput").val();
		System.out.println("mainParent="+mainParent);
		String editTitle = editDocument.getElementById("xwikidoctitleinput").val();
		System.out.println("editTitle="+editTitle);
		Element selection = editDocument.getElementById("xwikidocsyntaxinput2");
		String docSyntax=selection.select("option[selected=selected]").val();
		System.out.println("docSyntax="+docSyntax);
		// wiki.printWiki();
//		System.out.println(document.html());
		keyValues.put("form_token", form_token);
		keyValues.put("parent", mainParent);
		keyValues.put("title", editTitle);
		keyValues.put("xcontinue", dirverURI);
		keyValues.put("RequiresHTMLConversion", "content");
		keyValues.put("content_syntax", docSyntax);
		
		keyValues.put("xeditaction", "edit");        
		keyValues.put("syntaxId", docSyntax);        
		keyValues.put("ajax", "false");
		keyValues.put("action_save", "Save & View");
	}
	
	private Map<String, String> auth(String url, Map<String, String> result) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		url = httpServer+"/xwiki/bin/loginsubmit/XWiki/XWikiLogin";
		Map<String, String> keyValues = new HashMap<String, String>();
		keyValues.put("xredirect", "");
		keyValues.put("j_username", "Admin");
		keyValues.put("j_password", "admin");
		result = HttpRequestClient.doPost(url, headers, keyValues);
		System.out.println("-------------------------------------------------------------------");
		return result;
	}

	public void saveCookieHead(Map<String, String> result) {
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

	private Map<String, String> getLoginHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", cookieHeader.toString());
		return headers;
	}

	private Document parseHtml(String html) {
		Document document = Jsoup.parse(html);
		return document;
	}

	private void printMap(Map<String, String> map) {
		if (map == null || map.size() == 0) {
			return;
		}
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			System.out.println(name + ":" + map.get(name));
		}
	}

	public Document doGetRequest(String uri) throws Exception {
		System.out.println("request uri = "+uri);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", cookieHeader.toString());
		Map<String, String> result = HttpRequestClient.doGet(httpServer + uri, headers);
		if (result == null || result.get("result") == null || "".equals(result.get("result"))) {
			return null;
		}
		Document document = Jsoup.parse(result.get("result"));
		return document;
	}
	
	public void Logout() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", "JSESSIONID="+cookieHeader.getValue("JSESSIONID"));
		HttpRequestClient.doGet(httpServer+"/xwiki/bin/logout/XWiki/XWikiLogout?xredirect=/xwiki/bin/view/Main/", headers);
	}
}
