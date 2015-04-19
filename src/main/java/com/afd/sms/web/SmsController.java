/**
 * Copyright (c)2013-2014 by www.afd.com. All rights reserved.
 * 
 */
package com.afd.sms.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.afd.sms.service.ISmsService;

/**
 * 短消息http服务
 * 
 * @author xuzunyuan
 * @date 2014年3月26日
 */
@Controller
public class SmsController {
	@Autowired
	ISmsService smsService;

	@RequestMapping("/test")
	public String test() {
		return "test";
	}

	@RequestMapping("/sms")
	@ResponseBody
	public Boolean sms(@RequestParam(value = "mobile") String[] mobile,
			@RequestParam("smsContent") String smsContent,
			@RequestParam(value = "bizCode", required = false) String bizCode,
			@RequestParam(value = "sendTime", required = false) String sendTime) {

		if (mobile == null || mobile.length == 0 || smsContent == null)
			return false;

		int ret;

		if (sendTime == null) {
			ret = smsService.sendSmsMultiEx(mobile, smsContent, bizCode);
		} else {
			ret = smsService.sendScheduledSmsMultiEx(mobile, smsContent,
					sendTime, bizCode);
		}

		return (ret == 0);
	}
}
