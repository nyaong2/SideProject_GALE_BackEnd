package com.SideProject.GALE.controller.file;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.controller.board.BoardResCode;
import com.SideProject.GALE.exception.file.DenyFileExtensionException;
import com.SideProject.GALE.exception.file.FileUploadException;
import com.SideProject.GALE.exception.file.FileUploadDuplicateFileException;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@RestController	//Controller = ResponseBody 미포함
@RequiredArgsConstructor
@RequestMapping(value = "/file")
public class FileController {
	
	//https://taetaetae.github.io/2019/07/21/spring-file-upload/
	private final ResponseService responseService;
	private final FileService fileService;
	


	@PostMapping(value = "/upload")
	public ResponseEntity upload(MultipartFile file) {
		boolean result = false;
		
		try {
			result = fileService.save(file);
		} catch (DenyFileExtensionException ex) {
			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, FileResCode.FAIL_DENYFILEEXTENSION, ex.getMessage());
		} catch (FileUploadException ex) {
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, FileResCode.FAIL_SERVERERROR, ex.getMessage());	
		} catch (FileUploadDuplicateFileException ex) {
			return responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, FileResCode.FAIL_DUPLICATEFILE, ex.getMessage());	
		} catch (Exception e) {}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "업로드에 성공하였습니다.");
	}

	
    @PostMapping("/multiupload")
    public ResponseEntity uploadMultipleFiles(@RequestParam("file") MultipartFile[] files) {
    	boolean result = false;
    	
    	int count= 0;
    	for(MultipartFile file : files)
       {
    		try {
    			result = fileService.save(file);
    		} catch (DenyFileExtensionException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, FileResCode.FAIL_DENYFILEEXTENSION, ex.getMessage());
    		} catch (FileUploadException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, FileResCode.FAIL_SERVERERROR, ex.getMessage());	
    		} catch (FileUploadDuplicateFileException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, FileResCode.FAIL_DUPLICATEFILE, ex.getMessage());	
    		} catch (Exception e) {}
       }
		return responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "test");
    }
	
    
	@GetMapping(value = "/download")
	public ResponseEntity download(@RequestParam String fileName) {
		System.out.println("실행중");

		byte[] imageByte = null;
		String srcFileName = null;
		HttpHeaders header = new HttpHeaders();
		
		try {
			srcFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
			
			File file = new File("c:\\GALE\\File\\" +srcFileName);
			
			if(!file.exists())
				return responseService.CreateBaseEntity(HttpStatus.NOT_FOUND, null, FileResCode.FAIL_NOTFOUND, "파일을 찾을 수 없습니다.");
			
			header.add("Content-Type", Files.probeContentType(file.toPath()));		

			imageByte = FileCopyUtils.copyToByteArray(file);
		} catch (Exception ex) {}
		
		JSONArray jsary = new JSONArray(imageByte);
		
		return new ResponseEntity<>(imageByte,header,HttpStatus.OK);
		//return responseService.CreateListEntity(HttpStatus.CREATED, null, BoardResCode.SUCCESS, "test", jsary);
	}	
	
}
