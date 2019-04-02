package org.dwbzen.text.util.exception;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public ConfigurationException() {
		super("ConfigurationException");
	}
	public ConfigurationException(String messageText) {
		super(messageText);
	}


}
