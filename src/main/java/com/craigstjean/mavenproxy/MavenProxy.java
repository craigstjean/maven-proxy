package com.craigstjean.mavenproxy;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

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
		initializer.addExtensions(new Extension() {

			@SuppressWarnings("unused")
			void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
				event.addBean().types(ProxyConfiguration.class).scope(ApplicationScoped.class)
						.createWith(ctx -> configuration).name("ProxyConfiguration");
			}

		});

		SeContainer container = initializer.initialize();

		ProxyServer server = container.select(ProxyServer.class).get();
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				server.stop();
				container.close();
			}
		});
	}

}
