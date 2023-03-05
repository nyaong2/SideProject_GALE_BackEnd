package com.SideProject.GALE.exception.file;

public class FileUploadDuplicateFileException extends RuntimeException {
    public FileUploadDuplicateFileException() {}
	
    public FileUploadDuplicateFileException(String message) {
        super(message);
    }
    
    public FileUploadDuplicateFileException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
