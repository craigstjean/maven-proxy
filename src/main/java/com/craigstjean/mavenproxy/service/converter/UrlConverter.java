package com.craigstjean.mavenproxy.service.converter;

import java.util.List;

public interface UrlConverter {

	List<String> getUrlsFrom(String originatingUrl);

}
