package com.martin.php;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月9日 下午1:57:02
 */
public class XWikiThread extends Thread{
	private XWikiHander hander;
	private Map<String, String> result;
	private String homePage;
	private int count = 0;
	private int MAX_COUNT = 5;
	private int sleepTime;
	public XWikiThread(int sleep) {
		hander = new XWikiHander();
		sleepTime = sleep;
	}
	
	@Override
	public void run() {
		try {
			//登录wiki
			result = hander.login();
			//保存登录会话信息
			hander.saveCookieHead(result);
			//访问Location指向的页面（主页）
			homePage = result.get("Location");
			while (count++ < MAX_COUNT) {
				result = hander.accessHomePage(homePage);
				//从返回的页面中获取wiki的page对应的URI
				Map<String, String> titles = hander.getAllTitles(result.get("result"));
				//获取标题对应的URI，并访问URI
				Document document = hander.getTitleDocumnet(titles.get("gxc"));
				//获取编辑前的内容
				String text = document.getElementById("xwikicontent").html();
				//获取编辑页面的URI
				String edituri = hander.getEditURI(document);
				//进入编辑页面
				document = hander.doGetRequest(edituri);
				//存放表单要提交的数据
				Map<String, String> keyValues = new HashMap<String, String>();
				Element formElement = document.getElementById("edit");
				String postURI = formElement.attr("action");
				hander.setFormDatas(document, keyValues);
				keyValues.put("content", text+" <h5>"+this.getName()+"-gxc-"+count+"</h5>");
				hander.postEdit(postURI, edituri, keyValues);
				Thread.sleep(sleepTime);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (hander != null) {
				try {
					hander.Logout();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
