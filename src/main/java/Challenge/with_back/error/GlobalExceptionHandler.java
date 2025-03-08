package Challenge.with_back.error;

import Challenge.with_back.dto.response.ErrorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> customExceptionHandler(CustomException e)
    {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponseDto.builder()
                        .code(e.getErrorCode().name())
                        .message(e.getErrorCode().getMessage())
                        .data(e.getData())
                        .build());
    }
}
