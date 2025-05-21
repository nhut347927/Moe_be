package com.moe.socialnetwork.exception;

public class AppException extends RuntimeException {
	private final String message;
	private final int statusCode;

	public AppException(String message, int statusCode) {
		super(message);
		this.message = message;
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
