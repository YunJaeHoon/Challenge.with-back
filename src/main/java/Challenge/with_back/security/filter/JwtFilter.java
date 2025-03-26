package Challenge.with_back.security.filter;

import Challenge.with_back.entity.User;
import Challenge.with_back.repository.UserRepository;
import Challenge.with_back.security.CustomUserDetailsService;
import Challenge.with_back.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter
{
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        String authorizationHeader = request.getHeader("Authorization");

        // 헤더에 토큰이 존재하는지 체크
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
        {
            String accessToken = authorizationHeader.substring(7);

            // 토큰이 유효한지 체크
            if(jwtUtil.checkToken(accessToken))
            {
                Long id = jwtUtil.getId(accessToken);
                User user = userRepository.findById(id).orElse(null);

                // 존재하는 계정인지 체크
                if(user != null)
                {
                    UserDetails userDetails = customUserDetailService.loadUserByUsernameAndLoginMethod(user.getEmail(), user.getLoginMethod());

                    if(userDetails != null)
                    {
                        // 접근 권한 인증 토큰 생성
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        // 현재 요청의 security context에 접근 권한 부여
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        }

        // 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
}
