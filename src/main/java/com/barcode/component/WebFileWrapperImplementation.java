package com.barcode.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class WebFileWrapperImplementation implements FileWrapper {

	private final MultipartFile multipartFile;
	private final String id;
	private final long lastModified;
	private String originalName;

	
	public WebFileWrapperImplementation(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
		this.id = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
		this.lastModified = new Date().getTime();
		this.originalName = multipartFile.getOriginalFilename();
	}

	
	@Override
	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}


	@Override
	public InputStream getInputStream() {
		try {
			return multipartFile.getInputStream();
		} catch (IOException exception) {
			throw new IllegalArgumentException(exception);
		}
	}

	@Override
	public long getContentLength() {
		return multipartFile.getSize();
	}

	@Override
	public String getContentType() {
		return multipartFile.getContentType();
	}

	@Override
	public String getETag() {
		return "\"" + id + "\"";
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public boolean isModifiedAfter(long milliseconds) {
		return !(new Date(milliseconds).toInstant().isBefore(new Date(getLastModified()).toInstant()));
	}
}
