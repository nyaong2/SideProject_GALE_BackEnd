																																																																																																																																																																																																																																																																																																																																																																						package com.SideProject.GALE.service.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.SideProject.GALE.controller.auth.AuthResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.auth.AuthMapper;
import com.SideProject.GALE.mapper.board.BoardMapper;
import com.SideProject.GALE.model.board.BoardDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
	
	private final BoardMapper boardMapper;
	
	public List<BoardDto> GetUserPrivateList(int category, String email)
	{
		List<BoardDto> getAllList = null;
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("category", category);
		map.put("writer", email);
		
		try {
			getAllList = boardMapper.GetUserPrivateList(map);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error - Database");
		}
		
		return getAllList;
	}
	
	public List<BoardDto> GetAllList(int category)
	{
		List<BoardDto> getAllList = null;
		try {
			getAllList = boardMapper.GetAllList(category);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error - Database");
		}
		
		return getAllList;
	}
	
	public boolean Write(BoardDto boardDto) 
	{
		int result = 0;
		try {
			result = boardMapper.Write(boardDto);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error - Database");
		}
		
		return (result == 1) ? true : false;	
	}
	
	public BoardDto Read(int idx)
	{
		BoardDto boardDto = new BoardDto();
		try {
			boardDto = boardMapper.Read(idx);
			//boardDto = boardDto.stream()
		} catch(Exception ex) {
			boardDto = null;
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error - Database");
		};		
		return boardDto;
	}
	
	public boolean Update(BoardDto boardDto)
	{
		int result = 0;
		try {
			result = boardMapper.Update(boardDto);
		} catch(Exception ex) {
			System.out.println("에러내용 : " + ex);
		};
		
		return (result == 1) ? true : false;
	}
	
	public boolean Delete(int idx)
	{
		int result = 0;
		try {
			result = boardMapper.Delete(idx);
		} catch(Exception ex) {};
		
		return (result == 1) ? true : false;
	}
}
