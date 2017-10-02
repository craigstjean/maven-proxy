package com.craigstjean.mavenproxy.model.dto;

import java.util.ArrayList;
import java.util.List;

public class UrlStream {

	private String filePath;
	private List<Header> headers = new ArrayList<>();
	private int httpStatusCode;

	public boolean isCached() {
		return filePath != null;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

}
