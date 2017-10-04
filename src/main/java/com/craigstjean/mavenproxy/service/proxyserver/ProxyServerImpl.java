package com.craigstjean.mavenproxy.service.proxyserver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;
import com.craigstjean.mavenproxy.model.dto.UrlStream;
import com.craigstjean.mavenproxy.model.dto.UrlStreamHeader;
import com.craigstjean.mavenproxy.service.converter.UrlConverter;
import com.craigstjean.mavenproxy.service.urlstream.UrlStreamer;

import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class ProxyServerImpl implements ProxyServer {

	private static final Logger logger = LoggerFactory.getLogger(ProxyServerImpl.class);

	@Inject
	private UrlConverter converter;

	@Inject
	private UrlStreamer urlStreamer;

	@Inject
	private ProxyConfiguration configuration;

	private Undertow server;

	@Override
	public void start() {
		server = Undertow.builder().addHttpListener(configuration.getPort().intValue(), configuration.getHost())
				.setHandler(new HttpHandler() {
					@Override
					public void handleRequest(final HttpServerExchange exchange) throws Exception {
						logger.debug("Handling request: " + exchange.getRequestPath());

						List<String> possibleUrls = converter.getUrlsFrom(exchange.getRequestPath());

						int rc = 0;
						UrlStream resultStream = null;
						for (String url : possibleUrls) {
							logger.debug("Attempting to receive: " + url + "...");

							UrlStream urlStream = urlStreamer.getStream(url);

							if (!urlStream.isCached()) {
								logger.debug("Received: " + urlStream.getHttpStatusCode());
								rc = urlStream.getHttpStatusCode();
							} else {
								logger.debug("Using cached file: " + urlStream.getFilePath());

								rc = 200;
								resultStream = urlStream;
								break;
							}
						}

						if (rc == 0) {
							rc = 404;
							logger.warn("Could not find repository for: " + exchange.getRequestPath());
						}

						exchange.setStatusCode(rc);
						if (rc == 200) {
							for (UrlStreamHeader header : resultStream.getHeaders()) {
								exchange.getResponseHeaders().put(new HttpString(header.getKey()), header.getValue());
							}

							String cachePath = configuration.getExpandedCachePath() + File.separator + "data"
									+ File.separator + resultStream.getFilePath();

							RandomAccessFile file = new RandomAccessFile(cachePath, "r");
							FileChannel fileChannel = file.getChannel();

							exchange.getResponseSender().transferFrom(fileChannel, new IoCallback() {

								@Override
								public void onException(HttpServerExchange exchange, Sender sender,
										IOException exception) {
									logger.info("Failed to send file to client", exception);

									try {
										fileChannel.close();
									} catch (Exception e) {
										// oh well
									}

									try {
										file.close();
									} catch (Exception e) {
										// oh well
									}

									exchange.endExchange();
								}

								@Override
								public void onComplete(HttpServerExchange exchange, Sender sender) {
									logger.debug("File sent to client.");

									try {
										fileChannel.close();
									} catch (Exception e) {
										// oh well
									}

									try {
										file.close();
									} catch (Exception e) {
										// oh well
									}

									exchange.endExchange();
								}
							});
						} else {
							exchange.getResponseSender().send("");
							exchange.endExchange();
						}
					}
				}).build();
		server.start();
	}

	@Override
	public void stop() {
		try {
			logger.info("Shutting down...");
			server.stop();
		} catch (Exception e) {
			logger.debug("Error shutting down server", e);
		}
	}

}
