package com.SideProject.GALE.controller.board;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.controller.auth.AuthResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.board.BoardService;
import com.SideProject.GALE.util.DebugMsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json")
@Slf4j
public class BoardController {
	
	private final BoardService boardService;
	private final ResponseService responseService;
	
	//참조했던 사이트 : https://github.com/leejinseok/spring-vue/blob/master/src/main/java/com/example/vue/config/security/JwtAuthenticationFilter.java
	
	// redis https://bcp0109.tistory.com/328
	
	@GetMapping("/board")
	public ResponseEntity GetList(@AuthenticationPrincipal TokenDto tokenDto, @RequestParam int category) // 특정 카테고리 리스트 모두 불러오기
	{
		boolean loginCheck = tokenDto.NullChecking();
		
		List<BoardDto> allList = new ArrayList<BoardDto>();
		
		//로그인 되어있을 시 private 게시판 가져옴
		if(loginCheck)
			allList = boardService.GetUserPrivateList(category,tokenDto.getEmail());

		// 비로그인 / 로그인 둘 다 통합적으로 public 게시물 가져옴
		for(BoardDto list : boardService.GetAllList(category))
			allList.add(list);
		
		if(allList.size() == 0)
			return responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.FAIL_NOTFOUND, "불러올 데이터가 없습니다.");
		
		JSONArray arrayData = new JSONArray(allList);
		return responseService.CreateListEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "성공", arrayData);
	}
	
	
	
	@PostMapping("/board") // C : 쓰기
	public ResponseEntity Write(@AuthenticationPrincipal TokenDto tokenDto, @RequestBody BoardDto boardDto) 
	{
		boolean loginCheck = tokenDto.NullChecking();
		
		//로그인이 됐을 때만 게시물을 쓸 수 있으므로 체킹후 토큰 정보가 없으면 fail
		if(!loginCheck)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, BoardResCode.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		boolean result= false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write", "[ID : " + boardDto.getWriter() + "]");
		
		try {
			result = boardService.Write(boardDto);			
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write", (result) ? "[Result : true]" : "[Result : false]");

		return (result) ? responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "글쓰기에 성공했습니다.")
				: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, BoardResCode.FAIL, "글쓰기 실패했습니다.");
	}
	
	// R : 읽어오기
	@GetMapping("/board/{idx}") 
	public ResponseEntity Read(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx)
	{
		boolean loginCheck = tokenDto.NullChecking();

		BoardDto readData = null;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Read", "[index : " + idx + "]");
		
		
		//1. 게시물을 읽을 수 있는 권한이 있는지 확인
		// - 클래스 하나 만들어서 거기서 아이디를 통해 친구인지 일반 유저인지 등 판단 후 처리
		
		//2. 권한에 맞아떨어지면 게시물 쿼리에서 읽고 전송
		try {
			readData = boardService.Read(idx);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Read", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Read", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}
		
		// Private이면서 요청한 사람의 이메일과 요청한 사람의 게시물이 서로 같지 않을 경우
		if(readData.getAccesstype() == 0 && !loginCheck)
			return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, BoardResCode.FAIL_FORBIDDEN, "글쓴이만 볼 수 있습니다.");
		else if(readData.getAccesstype() == 0 && loginCheck && !tokenDto.getEmail().equals(readData.getWriter()))
			return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, BoardResCode.FAIL_FORBIDDEN, "글쓴이만 볼 수 있습니다.");
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Read", (readData != null) ? "[Result : true]" : "[Result : false]");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		JSONObject obj = new JSONObject(readData);
		return (readData != null) ? responseService.CreateListEntity(HttpStatus.OK, headers, BoardResCode.SUCCESS, "읽기 성공 했습니다.", obj)
				: responseService.CreateBaseEntity(HttpStatus.NOT_FOUND, null, BoardResCode.FAIL_NOTFOUND, "잘못된 접근이거나 데이터를 찾을 수 없습니다.");
		/*
		 * https://sas-study.tistory.com/326
		 * 익명상태에서 private 접근 : 401
		 * 로그인은 되어있는데 권한이 없을 경우 : 403
		 */
		
	}
	
	
	
	// U : 업데이트
	@PatchMapping("/board/{idx}") 
	public ResponseEntity Update(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx, @RequestBody BoardDto boardDto) // 게시물 수정하기
	{
		boolean loginCheck = tokenDto.NullChecking();

		//로그인이 되어있지 않으면 게시물은 수정 할 수 없음
		if(!loginCheck)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, BoardResCode.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");

		boolean result = false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Update", "[index : " + idx + "]");

		// 업데이트를 위한 게시물 인덱스 설정
		boardDto.setIdx(idx);
		
		try {
			BoardDto ReadBoardDto = boardService.Read(idx);
				
			// Private 글 접근시 요청한 사람이랑 요청한 게시물이랑 작성자가 맞는지 비교
			if (!tokenDto.getEmail().equals(ReadBoardDto.getWriter()))
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, BoardResCode.FAIL_FORBIDDEN,"잘못된 접근입니다.");			

			result = boardService.Update(boardDto);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Update", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Update", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}		
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Update", (result) ? "[Result : true]" : "[Result : false]");
		
		return (result) ?
				responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "업데이트 성공")
				: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, BoardResCode.FAIL_BAD_REQUEST, "업데이트 실패");
	}
	
	
	// D : 지우기
	@DeleteMapping("/board/{idx}") 
	public ResponseEntity Delete(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx) // 게시물 삭제하기
	{
		boolean loginCheck = tokenDto.NullChecking();
		if(!loginCheck)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, BoardResCode.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		boolean result = false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Delete", "[index : " + idx + "]");
		
		try {
			BoardDto ReadBoardDto = boardService.Read(idx);
			
			//요청한 사람이랑 요청한 게시물이랑 작성자 맞는지 비교
			if(!tokenDto.getEmail().equals(ReadBoardDto.getWriter()))
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, BoardResCode.FAIL_FORBIDDEN, "글쓴이만 글을 지울 수 있습니다.");
			
			result = boardService.Delete(idx);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Delete", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Delete", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Delete", (result) ? "[Result : true]" : "[Result : false]");
		
		return (result) ?
				responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "게시물이 삭제되었습니다.")
				: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, BoardResCode.FAIL_SERVICE_UNAVAILABLE, "Server Error - DataBase");
	}
}
