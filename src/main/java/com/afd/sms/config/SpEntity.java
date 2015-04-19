/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * sp配置实体类
 * 
 * @author xuzunyuan
 * @date 2014年3月12日
 */
public class SpEntity {
	private String name;
	private String clientClassName;
	private Map<String, String> initParams = Maps.newHashMap();
	private int threads = 1; // 默认1个线程
	private int queueSize = 500; // 默认队列大小500
	private List<String> bizCodes = Lists.newArrayList();
	private boolean defaultSp = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDefaultSp() {
		return defaultSp;
	}

	public void setDefaultSp(boolean defaultSp) {
		this.defaultSp = defaultSp;
	}

	public String getClientClassName() {
		return clientClassName;
	}

	public void setClientClassName(String clientClassName) {
		this.clientClassName = clientClassName;
	}

	public Map<String, String> getInitParams() {
		return initParams;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public List<String> getBizCodes() {
		return bizCodes;
	}
}
