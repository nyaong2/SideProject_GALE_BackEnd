package com.SideProject.GALE.exception.file;

public class DenyFileExtensionException extends RuntimeException {
  
	public DenyFileExtensionException() {}
	
	public DenyFileExtensionException(String message) {
        super(message);
    }
    
    public DenyFileExtensionException(String message, Throwable cause) {
        super(message, cause);
    }
}
