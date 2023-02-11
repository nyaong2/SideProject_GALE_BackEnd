package com.SideProject.GALE.mapper.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.board.BoardDto;

@Mapper
public interface BoardMapper {
	List<BoardDto> GetUserPrivateList(Map<String, Object> map);
	List<BoardDto> GetAllList(int category);
	int Write(BoardDto boardDto);
	BoardDto Read(int idx);
	int Update(BoardDto boardDto);
	int Delete(int idx);
}
