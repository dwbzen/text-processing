package org.dwbzen.text.util.exception;

public class InvalidDataSourceException extends RuntimeException {

	private static final long serialVersionUID = -3959446740818051944L;
	
	public InvalidDataSourceException() {
		super("InvalidDataSource");
	}
	public InvalidDataSourceException(String messageText) {
		super(messageText);
	}

}
