package com.SideProject.GALE.service.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.exception.file.DenyFileExtensionException;
import com.SideProject.GALE.exception.file.FileUploadDuplicateFileException;
import com.SideProject.GALE.exception.file.FileUploadException;
import com.SideProject.GALE.mapper.board.BoardMapper;

import lombok.RequiredArgsConstructor;

@Service
public class FileService {
	//https://gaemi606.tistory.com/entry/Spring-Boot-REST-API-%ED%8C%8C%EC%9D%BC-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C
	//private final Path dirLocation;
	
	private String[] allowImageExtension = {
			"jpg", "jpeg", "png", "bmp", };
	
	@Value("${spring.servlet.multipart.location}")
	private String saveFolderLocation;
	
	
	public boolean save(MultipartFile multiPartFile) throws Exception
	{
		String fileName = StringUtils.cleanPath(multiPartFile.getOriginalFilename());
		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
		
		// Support Extension Checked
		boolean allowExtensionCheck = false;
		for(String ext : allowImageExtension)
		{
			if(fileExtension.toLowerCase().contains(ext))
				allowExtensionCheck=true;
		}
		
		if(!allowExtensionCheck)
			throw new DenyFileExtensionException("지원하지 않는 확장자 입니다. ['" + fileExtension + "']");
		
		
		try {
			multiPartFile.transferTo(Paths.get(saveFolderLocation + "\\" + fileName));
		} catch (Exception e) {
			System.out.println(e);
			throw new FileUploadException( "['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
		}
		
		String hashFileString = this.ExtractFileHashSHA256(saveFolderLocation + "\\"+ fileName);
		
		Path oldPath = Paths.get(saveFolderLocation + "\\" + fileName);
		Path hashPath = Paths.get(saveFolderLocation + "\\" + hashFileString + fileExtension);
		
		File file = new File(hashPath.toUri());
		if(file.exists())
		{
			File tempFile = new File(oldPath.toUri());
			tempFile.delete();
			throw new FileUploadDuplicateFileException( "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");				
		}
		
		Files.move( oldPath, hashPath);
		
		return true;
	}
	
	
	
	public static String ExtractFileHashSHA256(String fullFilePath) throws Exception {
		String SHA = ""; 
		int buff = 16384;
		try {
			RandomAccessFile file = new RandomAccessFile(fullFilePath, "r");

			MessageDigest hashSum = MessageDigest.getInstance("SHA-256");

			byte[] buffer = new byte[buff];
			byte[] partialHash = null;

			long read = 0;

			// calculate the hash of the hole file for the test
			long offset = file.length();
			int unitsize;
			while (read < offset) {
				unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
				file.read(buffer, 0, unitsize);

				hashSum.update(buffer, 0, unitsize);

				read += unitsize;
			}

			file.close();
			partialHash = new byte[hashSum.getDigestLength()];
			partialHash = hashSum.digest();
			
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < partialHash.length ; i++){
				sb.append(Integer.toString((partialHash[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return SHA;
	}
	
}
