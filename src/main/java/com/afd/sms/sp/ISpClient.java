/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.sp;

import java.util.Map;

/**
 * SP提供的客户端短信功能
 * 
 * @author xuzunyuan
 * @date 2014年3月11日
 */
public interface ISpClient extends ISpSms {
	/**
	 * 初始化客户端
	 * 
	 * @param args
	 * @return
	 */
	public boolean initClient(Map<String, String> initParams);

	/**
	 * 销毁客户端
	 */
	public void destroyClient();
}
