package com.casrusil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class SiiErpAiApplication {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SiiErpAiApplication.class);

	public static void main(String[] args) {
		org.springframework.context.ConfigurableApplicationContext context = SpringApplication
				.run(SiiErpAiApplication.class, args);

		log.info("🔍 FULL BEAN SEARCH:");
		String[] allBeans = context.getBeanDefinitionNames();
		log.info("Total beans found: {}", allBeans.length);
		for (String bean : allBeans) {
			log.info("BEAN: {}", bean);
		}

		log.info("🔍 SPECIFIC CHECKS:");
		printBean(context, "securityConfig");
		printBean(context, "authController");
		printBean(context, "testController");
		printBean(context, "companyJpaAdapter");
	}

	private static void printBean(org.springframework.context.ApplicationContext ctx, String name) {
		if (ctx.containsBean(name)) {
			log.info("✅ FOUND: {}", name);
		} else {
			log.info("❌ MISSING: {}", name);
		}
	}

}
