package com.barcode.component;

import java.io.InputStream;

public interface FileWrapper {
	String getOriginalName();
	void setOriginalName(String originalName);
	String getContentType();
	String getETag();
	InputStream getInputStream();
	long getContentLength();
	long getLastModified();
	boolean isModifiedAfter(long milliseconds);
}
