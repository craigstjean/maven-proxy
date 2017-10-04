package com.craigstjean.mavenproxy.service.urlstream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;
import com.craigstjean.mavenproxy.model.dto.UrlStream;
import com.craigstjean.mavenproxy.model.dto.UrlStreamHeader;
import com.craigstjean.mavenproxy.service.cache.Cache;

public class UrlStreamerImpl implements UrlStreamer {

	private static final Logger logger = LoggerFactory.getLogger(UrlStreamerImpl.class);

	@Inject
	private Cache cache;

	@Inject
	private CacheStreamer cacheStreamer;

	@Inject
	private ProxyConfiguration configuration;

	private Set<String> invalidHeaders = new HashSet<>();

	@PostConstruct
	public void init() {
		invalidHeaders.add("connection");
		invalidHeaders.add("via");
		invalidHeaders.add("date");
		invalidHeaders.add("x-cache");
		invalidHeaders.add("x-cache-hits");
	}

	@Override
	public UrlStream getStream(String url) {
		UrlStream urlStream;
		if (cacheStreamer.hasCached(url)) {
			urlStream = cacheStreamer.getFromCache(url);
		} else {
			try {
				urlStream = getStreamOverUrl(url);
			} catch (IOException e) {
				logger.error("Failed to retrieve file from url: " + url, e);
				urlStream = new UrlStream();
				urlStream.setHttpStatusCode(500);
			}
		}

		return urlStream;
	}

	private UrlStream getStreamOverUrl(String url) throws IOException {
		UrlStream urlStream = new UrlStream();

		RequestConfig config = RequestConfig.custom().setConnectTimeout(configuration.getConnectionTimeout())
				.setConnectionRequestTimeout(configuration.getConnectionTimeout())
				.setSocketTimeout(configuration.getConnectionTimeout()).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpClient.execute(httpGet);
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null && response.getStatusLine().getStatusCode() == 200) {
				String cacheFilename = UUID.randomUUID().toString().replaceAll("-", "");

				String cachePath = configuration.getExpandedCachePath() + File.separator + "data" + File.separator;
				new File(cachePath).mkdirs();

				urlStream.setFilePath(cacheFilename);
				for (Header header : response.getAllHeaders()) {
					if (isValidHeader(header.getName())) {
						urlStream.getHeaders().add(new UrlStreamHeader(header.getName(), header.getValue()));
					}
				}

				urlStream.getHeaders().add(new UrlStreamHeader("Connection", "close"));

				InputStream is = entity.getContent();
				Files.copy(is, Paths.get(cachePath, cacheFilename));
				is.close();

				cache.cacheUrlStream(url, urlStream);
			} else {
				urlStream.setHttpStatusCode(response.getStatusLine().getStatusCode());
			}
		} finally {
			response.close();
			httpClient.close();
		}

		return urlStream;
	}

	private boolean isValidHeader(String name) {
		return !invalidHeaders.contains(name.toLowerCase());
	}

}
