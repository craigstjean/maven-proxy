package com.craigstjean.mavenproxy.service.proxyserver;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;

public interface ProxyServer {

	void start(ProxyConfiguration configuration);

}
