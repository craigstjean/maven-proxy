package com.craigstjean.mavenproxy.service.cache;

import java.util.List;

import com.craigstjean.mavenproxy.model.dto.UrlStream;
import com.craigstjean.mavenproxy.model.dto.UrlStreamHeader;

public interface Cache {

	String getFilePath(String url);

	List<UrlStreamHeader> getHeaders(String url);

	void cacheUrlStream(String url, UrlStream urlStream);

}
