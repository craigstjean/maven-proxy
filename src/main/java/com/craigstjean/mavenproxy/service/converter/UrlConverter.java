package com.craigstjean.mavenproxy.service.converter;

import java.util.List;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;

public interface UrlConverter {

	List<String> getUrlsFrom(String originatingUrl, ProxyConfiguration configuration);

}
