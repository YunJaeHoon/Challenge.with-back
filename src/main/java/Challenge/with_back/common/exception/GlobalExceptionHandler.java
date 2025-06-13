package Challenge.with_back.common.exception;

import Challenge.with_back.common.response.ExceptionResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> customExceptionHandler(CustomException e)
    {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(e.getErrorCode().name())
                        .message(e.getErrorCode().getMessage())
                        .data(e.getData())
                        .build());
    }
}
