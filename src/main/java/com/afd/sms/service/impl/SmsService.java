
package com.afd.sms.service.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.afd.service.sms.ISmsService;
import com.afd.sms.config.Config;
import com.afd.sms.config.SpEntity;
import com.afd.sms.sp.ISpClient;
import com.google.common.collect.Lists;

/**
 * 消息服务类
 * 
 * @author xuzunyuan
 * @date 2014年3月12日
 */
@Service("smsService")
public class SmsService implements ISmsService {
	private static final Logger logger = LoggerFactory
			.getLogger(SmsService.class);

	private List<ThreadData> threadDatas = Lists.newArrayList();

	@PostConstruct
	public void startService() {
		// 解析配置文件
		Config config = new Config();
		boolean b = config.parseConfig();
		if (!b) {
			logger.error("解析配置文件出现异常，请检查sms.xml！");
			return;
		}

		// 为sp创建线程数据
		for (String key : config.getSpConfig().keySet()) {
			SpEntity spEntity = config.getSpConfig().get(key);
			List<SmsExecutor> executors = Lists.newArrayList();

			for (int i = 0; i < spEntity.getThreads(); i++) {
				ISpClient spClient = null;

				try {
					spClient = (ISpClient) Class.forName(
							spEntity.getClientClassName()).newInstance();

				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					break;
				}

				if (!spClient.initClient(spEntity.getInitParams())) {
					logger.error("SP[" + spEntity.getName() + "]客户端初始化失败！");
					break;
				}

				SmsExecutor smsExecutor = new SmsExecutor(spClient, "["
						+ spEntity.getName() + "-" + i + "]");

				executors.add(smsExecutor);
			}

			if (executors.size() > 0) {
				ThreadData threadData = new ThreadData();

				threadData.setSpEntity(spEntity);
				threadData.setExecutors(executors);

				threadDatas.add(threadData);
			}
		}

		// 启动线程
		int threadCount = countThread();

		if (threadCount == 0) {
			logger.error("没有可以提供服务的SP!");
			return;
		}
		ExecutorService service = Executors.newFixedThreadPool(threadCount);

		for (ThreadData threadData : threadDatas) {
			for (SmsExecutor executor : threadData.getExecutors()) {
				service.execute(executor);
			}
		}

		service.shutdown();
		logger.info("已启动:[" + threadCount + "]个短信线程！");
	}

	/**
	 * 计算所需的线程数
	 * 
	 * @return
	 */
	private int countThread() {
		int count = 0;

		for (ThreadData threadData : threadDatas) {
			count += threadData.getExecutors().size();
		}

		return count;
	}

	@PreDestroy
	public void destroy() {
		for (ThreadData threadData : threadDatas) {
			for (SmsExecutor executor : threadData.getExecutors()) {
				executor.stop();
			}
		}

		logger.info("所有线程已关闭！");
	}

	/**
	 * 根据业务类型选择合适的执行器
	 * 
	 * @param bizCode
	 * @return
	 */
	private SmsExecutor selectExecutor(String bizCode) {
		ThreadData threadData = selectThread(bizCode);
		if (threadData == null)
			return null;

		int target = (int) (threadData.getBalance() % threadData.getExecutors()
				.size());
		SmsExecutor executor = threadData.getExecutors().get(target);

		return executor;
	};

	/**
	 * 根据业务类型寻找合适的线程
	 * 
	 * @param bizCode
	 * @return
	 */
	private ThreadData selectThread(String bizCode) {
		ThreadData defaultThreadData = null;

		for (ThreadData threadData : threadDatas) {
			SpEntity spEntity = threadData.getSpEntity();

			if (spEntity.isDefaultSp()) {
				if (bizCode == null) {
					return threadData;
				} else if (defaultThreadData == null) {
					defaultThreadData = threadData;
				}
			}

			for (String threadBizCode : threadData.getSpEntity().getBizCodes()) {
				if (bizCode.equals(threadBizCode))
					return threadData;
			}
		}

		return defaultThreadData != null ? defaultThreadData : (threadDatas
				.size() > 0 ? threadDatas.get(0) : null);
	}

	/**
	 * 
	 * 线程数据助手
	 * 
	 * @author xuzunyuan
	 * @date 2014年3月12日
	 */
	private class ThreadData {
		private SpEntity spEntity;
		private List<SmsExecutor> executors;
		private long balance = 0L; // 线程均衡计数器

		public long getBalance() {
			return balance++;
		}

		public SpEntity getSpEntity() {
			return spEntity;
		}

		public void setSpEntity(SpEntity spEntity) {
			this.spEntity = spEntity;
		}

		public List<SmsExecutor> getExecutors() {
			return executors;
		}

		public void setExecutors(List<SmsExecutor> executors) {
			this.executors = executors;
		}
	}

	
	@Override
	public int sendSmsMultiEx(String[] mobile, String smsContent, String bizCode) {
		SmsExecutor executor = this.selectExecutor(bizCode);
		if (executor == null)
			return -1;
		return executor.sendSms(mobile, smsContent, null, bizCode);
	}


	@Override
	public int sendSmsMulti(String[] mobile, String smsContent) {
		SmsExecutor executor = this.selectExecutor(null);
		if (executor == null)
			return -1;
		return executor.sendSms(mobile, smsContent, null, null);
	}


	@Override
	public int sendSmsEx(String mobile, String smsContent, String bizCode) {
		SmsExecutor executor = this.selectExecutor(bizCode);
		if (executor == null)
			return -1;
		return executor.sendSms(new String[] { mobile }, smsContent, null,
				bizCode);
	}


	@Override
	public int sendSms(String mobile, String smsContent) {
		SmsExecutor executor = this.selectExecutor(null);
		if (executor == null)
			return -1;
		return executor
				.sendSms(new String[] { mobile }, smsContent, null, null);
	}


	@Override
	public int sendScheduledSmsMultiEx(String[] mobile, String smsContent,
			String sendTime, String bizCode) {
		SmsExecutor executor = this.selectExecutor(bizCode);
		if (executor == null)
			return -1;
		return executor.sendSms(mobile, smsContent, sendTime, bizCode);
	}

	@Override
	public int sendScheduledSmsMulti(String[] mobile, String smsContent,
			String sendTime) {
		SmsExecutor executor = this.selectExecutor(null);
		if (executor == null)
			return -1;
		return executor.sendSms(mobile, smsContent, sendTime, null);
	}


	@Override
	public int sendScheduledSmsEx(String mobile, String smsContent,
			String sendTime, String bizCode) {
		SmsExecutor executor = this.selectExecutor(bizCode);
		if (executor == null)
			return -1;
		return executor.sendSms(new String[] { mobile }, smsContent, sendTime,
				bizCode);
	}

	
	@Override
	public int sendScheduledSms(String mobile, String smsContent,
			String sendTime) {
		SmsExecutor executor = this.selectExecutor(null);
		if (executor == null)
			return -1;
		return executor.sendSms(new String[] { mobile }, smsContent, sendTime,
				null);
	}

}
