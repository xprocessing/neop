package com.gongziyu.neop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token生成和解析工具类（文档28.1节）
 */
@Data
@Component
@ConfigurationProperties(prefix = "neop.jwt")
public class JwtUtil {

    private String secret;
    private Long mobileExpire;
    private Long adminExpire;

    /**
     * 生成移动端Token
     */
    public String generateMobileToken(Long userId, String nickname, String inviteCode) {
        return Jwts.builder()
                .setClaims(Map.of(
                        "userId", userId,
                        "type", "mobile",
                        "nickname", nickname,
                        "inviteCode", inviteCode
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + mobileExpire))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成后台端Token
     */
    public String generateAdminToken(Long adminId, String username, String nickname) {
        return Jwts.builder()
                .setClaims(Map.of(
                        "adminId", adminId,
                        "type", "admin",
                        "username", username,
                        "nickname", nickname
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + adminExpire))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new com.gongziyu.neop.exception.BusinessException(401, "Token已过期");
        } catch (JwtException e) {
            throw new com.gongziyu.neop.exception.BusinessException(401, "Token非法");
        }
    }

    /**
     * 获取Token剩余有效期（毫秒）
     */
    public long getTokenRemainingTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
