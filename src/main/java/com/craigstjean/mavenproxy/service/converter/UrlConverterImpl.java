package com.craigstjean.mavenproxy.service.converter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;

public class UrlConverterImpl implements UrlConverter {

	@Inject
	private ProxyConfiguration configuration;

	@Override
	public List<String> getUrlsFrom(String originatingUrl) {
		String repository = getRepository(originatingUrl);
		String relativeUrl = stripRepository(originatingUrl);

		List<String> urls = new ArrayList<>();

		if (configuration.getRepositories().containsKey(repository)) {
			String[] repositoryUrls = configuration.getRepositories().get(repository);
			for (String repositoryUrl : repositoryUrls) {
				urls.add(combineUrls(repositoryUrl, relativeUrl));
			}
		}

		return urls;
	}

	private String getRepository(String url) {
		if (url.startsWith("/")) {
			url = url.substring(1);
		}

		int slashIndex = url.indexOf('/');
		if (slashIndex == -1) {
			// Invalid repository, just return an empty string
			return "";
		}

		return url.substring(0, slashIndex);
	}

	private String stripRepository(String url) {
		if (url.startsWith("/")) {
			url = url.substring(1);
		}

		url = url.substring(getRepository(url).length() + 1);

		return url;
	}

	private String combineUrls(String base, String remainder) {
		if (!base.endsWith("/")) {
			base = base + '/';
		}

		return base + remainder;
	}

}
