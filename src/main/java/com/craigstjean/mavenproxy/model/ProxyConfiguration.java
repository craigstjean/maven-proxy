package com.craigstjean.mavenproxy.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;

@Alternative
public class ProxyConfiguration {

	private static final int DEFAULT_TIMEOUT = 30 * 1000;

	private Long port;
	private String cache;
	private Map<String, String[]> repositories = new HashMap<>();
	private int connectionTimeout = DEFAULT_TIMEOUT;

	public void setDefaults() {
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

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
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

		sb.append("ProxyConfiguration [").append("port=").append(port).append(", cache=").append(cache)
				.append(", repositories={");

		for (Map.Entry<String, String[]> entry : repositories.entrySet()) {
			sb.append(entry.getKey()).append('=').append(Arrays.toString(entry.getValue())).append(", ");
		}

		sb.delete(sb.length() - 2, sb.length());
		sb.append("}, connectionTimeout=").append(connectionTimeout).append(']');

		return sb.toString();
	}

}
