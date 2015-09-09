package com.martin.php;
/**
 * @auther guanxianchun
 * @description
 * @version 1.0
 * @date 2015年9月9日 下午2:26:13
 */
public class XWikiThreadDemo {
	public static void main(String[] args) {
		XWikiThread thread = new XWikiThread(3000);
		XWikiThread thread2 = new XWikiThread(5000);
		thread.start();
		thread2.start();
	}
}
