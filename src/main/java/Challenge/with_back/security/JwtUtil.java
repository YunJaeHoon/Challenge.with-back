package Challenge.with_back.security;

import Challenge.with_back.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JwtUtil
{
    private final Key key;
    private final int accessTokenValidTime;
    private final int refreshTokenValidTime;

    // 생성자
    public JwtUtil(@Value("${JWT_SECRET}") String secret,
                   @Value("${ACCESS_TOKEN_VALID_TIME}") String accessTokenValidTime,
                   @Value("${REFRESH_TOKEN_VALID_TIME}") String refreshTokenValidTime)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidTime = Integer.parseInt(accessTokenValidTime);
        this.refreshTokenValidTime = Integer.parseInt(refreshTokenValidTime);
    }

    // 토큰 반환
    public String getToken(Long id, boolean isAccessToken)
    {
        Claims claims = Jwts.claims();
        claims.put("id", id);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiresAt = now.plusSeconds(isAccessToken ? accessTokenValidTime : refreshTokenValidTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰을 쿠키로 변환
    public Cookie parseTokenToCookie(String token, boolean isAccessToken)
    {
        Cookie cookie = new Cookie(isAccessToken ? "accessToken" : "refreshToken", token);

        cookie.setHttpOnly(!isAccessToken);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(isAccessToken ? accessTokenValidTime : refreshTokenValidTime);
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    // 토큰 검증
    public boolean checkToken(String token)
    {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 User 엔티티 추출
    public Long getId(String token)
    {
        if(checkToken(token))
        {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.get("id", Long.class);
        }

        return null;
    }
}
