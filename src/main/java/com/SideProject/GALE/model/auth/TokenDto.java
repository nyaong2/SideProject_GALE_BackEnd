package com.SideProject.GALE.model.auth;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
//@RedisHash("tokenDto")
public class TokenDto {
	
	@Id
	private String email;
	private String nickname;
	private String token;
	//public long IssuedTime = 0;
	//public long ExpirationTime = 0;	
    
	public boolean NullChecking()
	{
		return (!StringUtils.hasText(email) || !StringUtils.hasText(nickname)) || !StringUtils.hasText(token) ? true : false;
	}
    
	public TokenDto(Claims claims, String token) {
		if(claims != null)
		{
			this.setEmail( (claims.get("email") != null) ? claims.get("email").toString() : null );
			this.setNickname((claims.get("nickname") != null) ?  claims.get("nickname").toString() : null);
			this.setToken( (!token.isEmpty()) ? token.toString() : null );
		}
	}
    
}
