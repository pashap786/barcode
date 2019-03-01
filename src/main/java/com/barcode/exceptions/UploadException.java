package com.barcode.exceptions;

@SuppressWarnings("serial")
public class UploadException extends RuntimeException {

	public UploadException(String message) {
		super(message);
	}
	
	public UploadException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
