/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.service.sms;

/**
 * 短信发送服务接口
 * 
 * @author xuzunyuan
 * @date 2014年3月11日
 */
public interface ISmsService {
	/**
	 * 发送短信
	 * 
	 * @param mobile
	 * @param smsContent
	 * @param bizCode
	 * @return 非0表示失败
	 */
	public int sendSmsMultiEx(String[] mobile, String smsContent, String bizCode);

	public int sendSmsMulti(String[] mobile, String smsContent);

	public int sendSmsEx(String mobile, String smsContent, String bizCode);

	public int sendSms(String mobile, String smsContent);

	/**
	 * 发送定时短信
	 * 
	 * @param mobile
	 * @param smsContent
	 * @param bizcode
	 * @param sendTime
	 *            年年年年月月日日时时分分秒秒
	 * @return 0表示失败
	 */
	public int sendScheduledSmsMultiEx(String[] mobile, String smsContent,
			String sendTime, String bizCode);

	public int sendScheduledSmsMulti(String[] mobile, String smsContent,
			String sendTime);

	public int sendScheduledSmsEx(String mobile, String smsContent,
			String sendTime, String bizCode);

	public int sendScheduledSms(String mobile, String smsContent,
			String sendTime);
}
