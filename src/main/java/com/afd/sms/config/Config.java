/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.collect.Maps;

/**
 * 配置实体类
 * 
 * @author xuzunyuan
 * @date 2014年3月12日
 */
public class Config {
	private static final Logger logger = LoggerFactory
			.getLogger(Config.class);
	Map<String, SpEntity> spConfig = Maps.newHashMap();

	public Config() {

	}

	public Map<String, SpEntity> getSpConfig() {
		return spConfig;
	}

	/**
	 * 解析sms配置文件
	 * 
	 * @return
	 */
	public boolean parseConfig() {
		String activeProfile = System.getProperty("spring.profiles.active");
		if (activeProfile == null)
			activeProfile = System.getProperty("spring.profiles.default");

		Resource resource = new ClassPathResource("/sms.xml");
		InputStream is = null;

		SAXReader saxReader = new SAXReader();
		try {
			is = resource.getInputStream();
			Document document = saxReader.read(is);

			Element root = document.getRootElement();

			for (@SuppressWarnings("rawtypes")
			Iterator it = root.elementIterator(); it.hasNext();) {
				Element element = (Element) it.next();
				String profile = element.attributeValue("profile");

				// 忽略非活动的sp
				if (profile != null && !profile.equals(activeProfile))
					continue;

				SpEntity spEntity = new SpEntity();
				spEntity.setName(element.attributeValue("name"));
				assert (spEntity.getName() != null);

				String isDefault = element.attributeValue("default");
				if ("true".equalsIgnoreCase(isDefault)) {
					spEntity.setDefaultSp(true);
				}

				spEntity.setClientClassName(element
						.elementTextTrim("clientClassName"));
				assert (spEntity.getClientClassName() != null);

				String initParams = element.elementText("initParams");
				setSpInitParams(spEntity, initParams);

				String threads = element.elementText("threads");
				if (threads != null) {
					int t = (int) ConvertUtils.convert(threads, int.class);
					if (t > 0)
						spEntity.setThreads(t);
				}

				String queueSize = element.elementText("queueSize");
				if (queueSize != null) {
					int t = (int) ConvertUtils.convert(queueSize, int.class);
					if (t > 0)
						spEntity.setQueueSize(t);
				}

				String bizCodes = element.elementText("bizCodes");
				setSpBizCodes(spEntity, bizCodes);

				spConfig.put(spEntity.getName(), spEntity);
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return false;

		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}

		return true;

	}

	/**
	 * 处理SP初始化参数
	 * 
	 * @param spEntity
	 * @param initParams
	 */
	private void setSpInitParams(SpEntity spEntity, String initParams) {
		if (initParams == null)
			return;

		String[] properties = initParams.split("\\,");

		for (String property : properties) {
			String[] pair = property.split("\\=");
			if (pair.length != 2)
				continue;

			spEntity.getInitParams().put(pair[0].trim(), pair[1].trim());
		}
	}

	private void setSpBizCodes(SpEntity spEntity, String bizCodes) {
		if (bizCodes == null)
			return;

		String[] bizCodeArr = bizCodes.split("\\,");
		for (String bizCode : bizCodeArr) {
			bizCode = bizCode.trim();

			if (!"".equals(bizCode))
				spEntity.getBizCodes().add(bizCode);
		}
	}

	public static void main(String[] args) {
		Config configEntity = new Config();
		boolean parse = configEntity.parseConfig();

		System.out.println("parse result is :" + parse);
	}
}
