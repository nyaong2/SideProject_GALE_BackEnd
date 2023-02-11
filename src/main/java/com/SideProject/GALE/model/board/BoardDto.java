package com.SideProject.GALE.model.board;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
public class BoardDto {
	public int idx;
	public int category;
	public int accesstype;
	public String writer;
	public String regdate;
	public String locationname;
	public String locationaddress;
	public String content;
	public int average;
	public int likes;
	public int price;
	public int congestion;
	public int accessibility;
}
