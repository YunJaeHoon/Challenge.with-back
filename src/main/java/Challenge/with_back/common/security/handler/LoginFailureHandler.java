package Challenge.with_back.common.security.handler;

import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.response.ExceptionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler
{
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException
    {
        ExceptionResponseDto responseDto = ExceptionResponseDto.builder().build();

        if (exception instanceof BadCredentialsException || exception instanceof InternalAuthenticationServiceException) {
            responseDto.setCode(CustomExceptionCode.WRONG_PASSWORD.name());
            responseDto.setMessage(CustomExceptionCode.WRONG_PASSWORD.getMessage());
        } else if (exception instanceof DisabledException) {
            responseDto.setCode(CustomExceptionCode.DISABLED_ACCOUNT.name());
            responseDto.setMessage(CustomExceptionCode.DISABLED_ACCOUNT.getMessage());
        } else {
            responseDto.setCode(CustomExceptionCode.UNEXPECTED_ERROR.name());
            responseDto.setMessage(CustomExceptionCode.UNEXPECTED_ERROR.getMessage());
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
