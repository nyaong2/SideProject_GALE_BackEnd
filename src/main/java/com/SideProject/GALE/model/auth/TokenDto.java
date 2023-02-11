package com.SideProject.GALE.model.auth;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.util.StringUtils;


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
	private String token;
    @TimeToLive
	private Long expiration;
	//public long IssuedTime = 0;
	//public long ExpirationTime = 0;	
    
	public boolean NullChecking()
	{
		return (!StringUtils.hasText(email) || !StringUtils.hasText(token)) ? true : false;
	}
    
    public TokenDto() {};
    
    public TokenDto create (String email, String token, Long expiration) {
        return TokenDto.builder()
        		.email(email)
        		.token(token)
        		.expiration(expiration/1000)
        		.build();
      }
    
    
}
