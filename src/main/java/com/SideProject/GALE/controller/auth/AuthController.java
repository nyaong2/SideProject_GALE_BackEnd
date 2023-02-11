package com.SideProject.GALE.controller.auth;

import java.security.Principal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.controller.board.BoardResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.model.auth.LoginDto;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.auth.UserDto;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.auth.AuthService;
import com.SideProject.GALE.util.DebugMsg;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins="http://localhost:3000")
@RequestMapping(value = "/auth", produces = "application/json") // 맨앞부분은 auth로 매핑
public class AuthController {
 
	//12-01 https://onejunu.tistory.com/138
	private final AuthService authService;
	private final ResponseService responseService;
	
	@PostMapping("/test")
	public ResponseEntity Test() {
		
		//System.out.println("값 : " + date);
		return responseService.CreateBaseEntity(HttpStatus.OK, null, BoardResCode.SUCCESS, "성공");
	}
	
	
	
	@PostMapping("/login")
	public ResponseEntity Login(@RequestBody LoginDto loginDto) {
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Login", "[ID : " + loginDto.getEmail() + "]");
		
		Map<String,Object> token = null;
		
		try {
			token = authService.Login(loginDto);
		} catch (CustomRuntimeException ex){
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Login", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Login", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}

		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Login", (token == null) ? "[Result : false]" : "[Result : true]");

		
		
		JSONObject tokenData = new JSONObject(token);
		return (token != null) ? responseService.CreateListEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "로그인 성공", tokenData) 
							  : responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
	}
	
	
	//로그아웃
	@PostMapping("/logout")
	public ResponseEntity Logout(@RequestBody TokenDto tokenDto) {

		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Logout", "[ID : " + tokenDto.getEmail() + "]");
			
		boolean result = false;
		
		try {
			result = authService.Logout(tokenDto);
		}catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Logout", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		}catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Logout", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}
		
		//HttpHeaders httpHeaders = new HttpHeaders();
		//httpHeaders.add("authorization", "gale " + tokens.get("accessToken"));
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Logout", "[Result : " + result + "]");

		
		return (result == true) ? responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "로그아웃 되었습니다.")
								: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, AuthResCode.FAIL, "로그아웃에 실패했습니다.");
	}	
	

	@PostMapping("/signup")
	public ResponseEntity Signup(@RequestBody UserDto userDto, Principal principal)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[ID : " + userDto.getEmail() + ", PW : " + userDto.getPassword());		

		boolean result = false;

		try {
			result = authService.Signup(userDto);
		} 
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Signup", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Signup", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[Result : " + result + "]");
		
		return result ? responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "회원가입 성공")
					: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
	}
	
	
	//이메일 중복체크
	@GetMapping("/signup/exist-email")
	public ResponseEntity ExistEmail(@RequestParam String email)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistEmail", "[ReqValue : " + email + "]");
		
		boolean duplication = false;
		try{
			duplication = authService.ExistEmail(email);
		}
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistEmail", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistEmail", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[Duplication : " + duplication + "]");
		

		return duplication ? responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, AuthResCode.FAIL_DUPLICATION, "이미 가입된 이메일입니다.")
					: responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "사용 가능한 이메일입니다.");
	}
	
	
	//닉네임 중복체크
	@GetMapping("/signup/exist-nickname")
	public ResponseEntity ExistNickname(@RequestParam String nickname)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistNickname", "[ReqValue : " + nickname + "]");
			
		boolean duplication = false;
		try{
			duplication = authService.ExistNickname(nickname);
		}
		catch (CustomRuntimeException ex)
		{
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistNickname", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistNickname", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistNickname", "[Duplication : " + duplication + "]");

		return duplication ? responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, AuthResCode.FAIL_DUPLICATION, "이미 존재하는 닉네임입니다.")
					: responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "사용 가능한 닉네임입니다.");
	}
	
	
	//리프레시토큰을 이용한 액세스 토큰재발급
	@RequestMapping(value= "/token")
	public ResponseEntity ExpireCheckingRefreshTokenAndGenerateAccessToken(@RequestBody TokenDto tokenDto)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Token", "[ReqValue : " + tokenDto.getEmail() + "]");

		// Request = 리프레시토큰 받고 검증 후 액세스토큰 다시 발금
		String tokens = null;
		
		try {
			tokens = authService.ExpireCheckingRefreshTokenAndGenerateAccessToken(tokenDto);
		}
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Token", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch(Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Token", "Exception", null, null, ex.getMessage(), null);

			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
		}
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - token", "[Result : " + (StringUtils.hasText(tokens) ? true : false) + "]");
		
		
		return (StringUtils.hasText(tokens)) ? responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, tokens)
							: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  AuthResCode.FAIL_UNAVAILABLE_SERVER, "Server Error");
	}
	
//	@PostMapping("test2")
//	public ResponseEntity tests(@RequestBody TokenDto tokenDto)
//	{
//		
//		System.out.println("save");
//		RefreshTokenDto data = new RefreshTokenDto(tokenDto.getEmail(), tokenDto.getToken(), LocalDateTime.now());
//		
//		//repo.save(data);
//		return responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "asd");
//	}
//	
//	@PostMapping("test3")
//	public ResponseEntity tests2(@RequestBody TokenDto tokenDto)
//	{
//		
//		System.out.println("save");
//		
//		//System.out.println(dto.toString());
//		return responseService.CreateBaseEntity(HttpStatus.OK, null, AuthResCode.SUCCESS, "asd");
//	}
}


