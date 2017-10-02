package com.craigstjean.mavenproxy.service.urlstream;

import java.util.List;

import javax.inject.Inject;

import com.craigstjean.mavenproxy.model.dto.UrlStreamHeader;
import com.craigstjean.mavenproxy.model.dto.UrlStream;
import com.craigstjean.mavenproxy.service.cache.Cache;

public class CacheStreamerImpl implements CacheStreamer {

	@Inject
	private Cache cache;

	@Override
	public boolean hasCached(String url) {
		String filePath = cache.getFilePath(url);
		return filePath != null;
	}

	@Override
	public UrlStream getFromCache(String url) {
		UrlStream urlStream = new UrlStream();
		urlStream.setFilePath(cache.getFilePath(url));

		List<UrlStreamHeader> headers = cache.getHeaders(url);
		urlStream.getHeaders().addAll(headers);

		return urlStream;
	}

}
