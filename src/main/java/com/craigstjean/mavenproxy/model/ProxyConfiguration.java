package com.craigstjean.mavenproxy.model;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;

@Alternative
public class ProxyConfiguration {

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_TIMEOUT = 30 * 1000;

	private String host = DEFAULT_HOST;
	private Long port;
	private String cache;
	private Map<String, String[]> repositories = new HashMap<>();
	private int connectionTimeout = DEFAULT_TIMEOUT;

	public void setDefaults() {
		host = DEFAULT_HOST;
		port = 8002L;
		cache = "~/.maven-proxy";
		connectionTimeout = DEFAULT_TIMEOUT;

		repositories.put("maven-central", new String[] { "https://repo1.maven.org/maven2" });
		repositories.put("jcenter", new String[] { "https://jcenter.bintray.com" });
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	public String getExpandedCachePath() {
		String cachePath = cache;
		if (cachePath.startsWith("~")) {
			String userHome = System.getProperty("user.home");

			if (userHome.endsWith(File.separator)) {
				cachePath = userHome + cachePath.substring(2);
			} else {
				cachePath = userHome + cachePath.substring(1);
			}
		}

		return cachePath;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	public Map<String, String[]> getRepositories() {
		return repositories;
	}

	public void setRepositories(Map<String, String[]> repositories) {
		this.repositories = repositories;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("ProxyConfiguration [\n\thost=").append(host).append("\n\tport=").append(port).append("\n\tcache=")
				.append(cache).append(" (").append(getExpandedCachePath()).append(")\n\trepositories={\n\t\t");

		for (Map.Entry<String, String[]> entry : repositories.entrySet()) {
			sb.append(entry.getKey()).append('=').append(Arrays.toString(entry.getValue())).append("\n\t\t");
		}

		sb.delete(sb.length() - 3, sb.length());
		sb.append("},\n\tconnectionTimeout=").append(connectionTimeout).append(']');

		return sb.toString();
	}

}
