package com.craigstjean.mavenproxy;

import java.io.File;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;
import com.craigstjean.mavenproxy.service.proxyserver.ProxyServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MavenProxy {

	private static final Logger logger = LoggerFactory.getLogger(MavenProxy.class);

	public static void main(String[] args) throws Exception {

		ProxyConfiguration configuration;
		if (args.length == 1) {
			ObjectMapper objectMapper = new ObjectMapper();
			configuration = objectMapper.readValue(new File(args[0]), ProxyConfiguration.class);
		} else {
			configuration = new ProxyConfiguration();
			configuration.setDefaults();

			logger.info("Setting configuration to defaults:\n\t{}", configuration);
		}

		SeContainerInitializer initializer = SeContainerInitializer.newInstance();
		SeContainer container = initializer.initialize();

		ProxyServer server = container.select(ProxyServer.class).get();
		server.start(configuration);

		container.close();
	}

}
