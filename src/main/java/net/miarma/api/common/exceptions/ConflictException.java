package net.miarma.api.common.exceptions;

public class ConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ConflictException(String message) {
		super(message);
	}
	
	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ConflictException(Throwable cause) {
		super(cause);
	}

}
