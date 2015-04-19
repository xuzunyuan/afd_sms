/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.service.impl;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afd.sms.sp.ISpClient;

/**
 * 短信发送执行线程
 * 
 * @author xuzunyuan
 * @date 2014年3月12日
 */
public class SmsExecutor implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(SmsExecutor.class);

	private volatile boolean running = false; // 线程运行状态
	private Object lock = new Object(); // 线程锁
	private volatile boolean wating = false; // 等待状态

	private ISpClient spClient = null; // 短信客户端
	private String threadName = null; // 线程名称
	private ConcurrentLinkedQueue<SmsEntity> queue = new ConcurrentLinkedQueue<SmsEntity>(); // 短信队列
	private static final int MAX_CHAR = 400;
	private static final int MAX_MOBILE = 200;

	@SuppressWarnings("unused")
	private SmsExecutor() {
	}

	public SmsExecutor(ISpClient spClient, String threadName) {
		assert (spClient != null);
		this.spClient = spClient;
		this.threadName = threadName;
	}

	public String getThreadName() {
		return threadName;
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		running = false;

		synchronized (lock) {
			if (wating) {
				wating = false;
				lock.notifyAll();
			}
		}
	}

	/**
	 * 返回运行状态
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 发送消息服务，实际上是将短信放入队列等待发送
	 * 
	 * @param mobile
	 * @param smsContent
	 * @param sendTime
	 * @param bizCode
	 * @return
	 */
	public int sendSms(String[] mobile, String smsContent, String sendTime,
			String bizCode) {

		// 服务结束后不再接收短信请求
		if (!running)
			return -1;

		SmsEntity sms = new SmsEntity();

		sms.setMobile(mobile);
		sms.setSmsContent(smsContent);
		sms.setSendTime(sendTime);
		sms.setBizCode(bizCode);

		queue.add(sms);

		synchronized (lock) {
			if (wating) {
				wating = false;
				lock.notifyAll();
			}
		}

		return 0;
	}

	/**
	 * 调用sp客户端发送短信
	 * 
	 * @param sms
	 * @return
	 */
	private void sendSms(SmsEntity sms) {
		String[] smsContent = splitBigSmsContent(sms.getSmsContent());
		if (smsContent == null)
			return;

		String[][] mobile = splitBigMobile(sms.getMobile());
		if (mobile == null)
			return;

		for (String[] oneMobileArr : mobile) {
			for (String oneSmsContent : smsContent) {
				if (sms.getSendTime() == null) {
					if (sms.getBizCode() == null) {
						int result = spClient.sendSmsMulti(oneMobileArr,
								oneSmsContent);

						logSms(oneMobileArr, oneSmsContent, null, null, result);

					} else {
						int result = spClient.sendSmsMultiEx(oneMobileArr,
								oneSmsContent, sms.getBizCode());

						logSms(oneMobileArr, oneSmsContent, null,
								sms.getBizCode(), result);
					}

				} else {
					if (sms.getBizCode() == null) {
						int result = spClient.sendScheduledSmsMulti(
								oneMobileArr, oneSmsContent, sms.getSendTime());

						logSms(oneMobileArr, oneSmsContent, sms.getSendTime(),
								null, result);
					} else {
						int result = spClient.sendScheduledSmsMultiEx(
								oneMobileArr, oneSmsContent, sms.getSendTime(),
								sms.getBizCode());

						logSms(oneMobileArr, oneSmsContent, sms.getSendTime(),
								sms.getBizCode(), result);
					}
				}
			}
		}
	};

	/**
	 * 记录短信发送日志
	 * 
	 * @param mobile
	 * @param smsContent
	 * @param sendTime
	 * @param bizCode
	 * @param result
	 */
	private void logSms(String[] mobile, String smsContent, String sendTime,
			String bizCode, int result) {
		logger.info("线程" + threadName + "发送短信[return code=" + result
				+ "]:[mobile=" + Arrays.toString(mobile) + ", smsContent="
				+ smsContent + ", bizCode=" + bizCode + ", sendTime="
				+ sendTime + "]");
	}

	/**
	 * 将大短信拆分成若干条小短信
	 * 
	 * @param smsContent
	 * @return
	 */
	private String[] splitBigSmsContent(String smsContent) {
		int length;

		if (smsContent == null || (length = smsContent.length()) == 0)
			return null;

		// 虽然看起来笨拙，但是适用于绝大多数情况，效率最高
		if (length <= MAX_CHAR)
			return new String[] { smsContent };

		int count = length / MAX_CHAR;

		if (count * MAX_CHAR != length)
			count++;

		String[] arr = new String[count];

		for (int i = 0; i < count; i++) {
			arr[i] = smsContent.substring(i * MAX_CHAR,
					(i == count - 1) ? length : (i + 1) * MAX_CHAR);
		}

		return arr;
	}

	/**
	 * 将大批手机号拆成若干组
	 * 
	 * @param mobile
	 * @return
	 */
	private String[][] splitBigMobile(String[] mobile) {
		int length;

		if (mobile == null || (length = mobile.length) == 0)
			return null;

		if (length <= MAX_MOBILE)
			return new String[][] { mobile };

		int count = length / MAX_MOBILE;

		if (count * MAX_MOBILE != length)
			count++;

		String[][] arr = new String[count][];

		for (int i = 0; i < count; i++) {
			int subLength = ((i == count - 1) ? (length - i * MAX_MOBILE)
					: MAX_MOBILE);
			arr[i] = new String[subLength];

			for (int j = 0; j < subLength; j++) {
				arr[i][j] = mobile[i * MAX_MOBILE + j];
			}
		}

		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;

		while (running) {
			SmsEntity sms = queue.poll();

			if (sms == null) {
				synchronized (lock) {
					// 再取一次数据，防止在进入等待之前队列被插入数据并通知
					sms = queue.poll();

					if (sms == null) {
						wating = true;
						try {
							lock.wait();
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
							return;
						}

						continue;
					}
				}
			}

			sendSms(sms);
		}

		// 叫停后处理队列中剩余的短信，因为此时不再接收新的短信，因此此处不做同步处理
		while (!queue.isEmpty()) {
			SmsEntity sms = queue.poll();
			sendSms(sms);
		}

		// 销毁客户端
		spClient.destroyClient();
	}

	/**
	 * 短消息实体类
	 * 
	 * @author xuzunyuan
	 * @date 2014年3月12日
	 */
	private class SmsEntity {
		private String[] mobile;
		private String smsContent;
		private String bizCode;
		private String sendTime;

		public String[] getMobile() {
			return mobile;
		}

		public void setMobile(String[] mobile) {
			this.mobile = mobile;
		}

		public String getSmsContent() {
			return smsContent;
		}

		public void setSmsContent(String smsContent) {
			this.smsContent = smsContent;
		}

		public String getBizCode() {
			return bizCode;
		}

		public void setBizCode(String bizCode) {
			this.bizCode = bizCode;
		}

		public String getSendTime() {
			return sendTime;
		}

		public void setSendTime(String sendTime) {
			this.sendTime = sendTime;
		}

		@Override
		public String toString() {
			return "SmsEntity [mobile=" + Arrays.toString(mobile)
					+ ", smsContent=" + smsContent + ", bizCode=" + bizCode
					+ ", sendTime=" + sendTime + "]";
		}

	}
}
