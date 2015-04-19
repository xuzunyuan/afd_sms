/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.sp.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.afd.sms.sp.ISpClient;
import com.afd.sms.tools.SmsClient;


public class EmayClient implements ISpClient {
	private static final Logger logger = LoggerFactory
			.getLogger(EmayClient.class);
	SmsClient client = null; // 
	private static final int DEFAULT_PRIORITY = 1;

	public EmayClient() {
	}

	@Override
	public int sendSmsMultiEx(String[] mobile, String smsContent, String bizCode) {
		return client.sendSMS(mobile, envelopSms(smsContent), DEFAULT_PRIORITY);
	}

	@Override
	public int sendSmsMulti(String[] mobile, String smsContent) {
		return client.sendSMS(mobile, envelopSms(smsContent), DEFAULT_PRIORITY);
	}

	@Override
	public int sendSmsEx(String mobile, String smsContent, String bizCode) {
		return this
				.sendSmsMultiEx(new String[] { mobile }, smsContent, bizCode);
	}

	@Override
	public int sendSms(String mobile, String smsContent) {
		return this.sendSmsMulti(new String[] { mobile }, smsContent);
	}

	@Override
	public int sendScheduledSmsMultiEx(String[] mobile, String smsContent,
			String sendTime, String bizCode) {
		return client
				.sendScheduledSMS(mobile, envelopSms(smsContent), sendTime);
	}

	@Override
	public int sendScheduledSmsMulti(String[] mobile, String smsContent,
			String sendTime) {
		return client
				.sendScheduledSMS(mobile, envelopSms(smsContent), sendTime);
	}

	@Override
	public int sendScheduledSmsEx(String mobile, String smsContent,
			String sendTime, String bizCode) {
		return this.sendScheduledSmsMultiEx(new String[] { mobile },
				smsContent, sendTime, bizCode);
	}

	@Override
	public int sendScheduledSms(String mobile, String smsContent,
			String sendTime) {
		return this.sendScheduledSmsMulti(new String[] { mobile }, smsContent,
				sendTime);
	}

	private String envelopSms(String smsContent) {
		return "【afd】" + smsContent;
	}

	@Override
	public boolean initClient(Map<String, String> args) {
		try {
			client =  new SmsClient();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public void destroyClient() {
		// do nothing
	}

	public static void main(String[] args) {
		/**
		 * if (init) { int regCode = emayClient.register();
		 * System.out.println("the return code of emayClient register is :" +
		 * regCode);
		 * 
		 * } else { System.out.println("emay client creat failed!"); }
		 */

	}
}
