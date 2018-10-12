package com.netopstec.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring辅助类
 * @author Administrator
 *
 */
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext context;
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {

		SpringUtil.context = context;
	}
	/**
	 * 获取实体类
	 * @param beanName
	 * @return
	 */
	public static Object getBean(String beanName) {
		return context.getBean(beanName);
	}
}
