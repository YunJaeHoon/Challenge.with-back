package Challenge.with_back.common.security.jwt;

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

    // 생성자
    public JwtUtil(@Value("${JWT_SECRET}") String secret)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String getToken(Long id, Token tokenType)
    {
        Claims claims = Jwts.claims();
        claims.put("id", id);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiresAt = now.plusSeconds(tokenType.getValidTime());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰을 쿠키로 변환
    public Cookie parseTokenToCookie(String token, Token tokenType)
    {
        Cookie cookie = new Cookie(tokenType.getName(), token);

        cookie.setHttpOnly(tokenType.isHttpOnly());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(tokenType.getValidTime());
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

    // 토큰에서 User id 추출
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
