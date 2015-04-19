/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.exception;

/**
 * 短信异常
 * 
 * @author xuzunyuan
 * @date 2014年3月11日
 */
public class SmsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 597895421319718022L;

	public static final int DEFAULT_CODE = 0;

	protected int code = DEFAULT_CODE; // 异常码

	public int getCode() {
		return code;
	}

	public SmsException() {
		super();
	}

	public SmsException(String msg) {
		super(msg);
	}

	public SmsException(Throwable e) {
		super(e);
	}

	public SmsException(String msg, Throwable e) {
		super(msg, e);
	}

	public SmsException(int code, String msg, Throwable e) {
		super(msg, e);
		this.code = code;
	}

	public SmsException(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public SmsException(int code) {
		super();
		this.code = code;
	}
}
