package com.project.mingle.Config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders; // Base64 디코딩을 위해 추가
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Component
public class JwtTokenProvider {

    // 🔑 Base64로 인코딩된 512비트(64바이트) 이상의 안전한 시크릿 키 문자열.
    // Base64 유효 문자(a-z, A-Z, 0-9, +, /)와 패딩 문자('=')만 사용해야 합니다.
    // 기존의 특수 문자('!', '@', '$')를 모두 제거하고 안전하게 변경했습니다.
    private final String SECRET_KEY = "MingleProjectJwtTokenSecureKeyBase64EncodedForHS512AlgorithmNeedsTobeVeryLongAndSafeByIncludingOnlyBase64CharactersABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    // Base64 문자열을 바이트 배열로 디코딩하고, 이를 HMAC SHA512 알고리즘에 적합한 Key 객체로 변환합니다.
    // 이 방식은 키 길이(512비트 이상)와 유효 문자열 문제를 동시에 해결합니다.
    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1시간 유효 기간 (밀리초)

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512) // Key 객체와 HS512 알고리즘 사용
                .compact();
    }
    /**
     * 주어진 JWT 토큰을 파싱하여 유효성을 검증합니다.
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT Token validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 유효한 JWT 토큰에서 사용자 ID(Subject)를 추출합니다.
     * @param token 사용자 ID를 추출할 JWT 토큰
     * @return 추출된 사용자 ID
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
}
