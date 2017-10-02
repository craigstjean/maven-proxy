package com.craigstjean.mavenproxy.service.urlstream;

import com.craigstjean.mavenproxy.model.dto.UrlStream;

public interface UrlStreamer {

	UrlStream getStream(String url);

}
