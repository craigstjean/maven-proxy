package com.craigstjean.mavenproxy.service.urlstream;

import com.craigstjean.mavenproxy.model.dto.UrlStream;

public interface CacheStreamer {

	boolean hasCached(String url);

	UrlStream getFromCache(String url);

}
