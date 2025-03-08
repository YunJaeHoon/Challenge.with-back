package Challenge.with_back.controller;

import Challenge.with_back.dto.response.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "기본")
public class BasicController
{
    @GetMapping("/check-connection")
    @Operation(summary = "연결 테스트")
    public ResponseEntity<SuccessResponseDto> checkConnection()
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("백엔드 서버와 연결을 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }
}
