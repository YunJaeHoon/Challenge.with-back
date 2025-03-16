package Challenge.with_back.controller;

import Challenge.with_back.dto.response.SuccessResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BasicController
{
    // 버전 확인 테스트
    @GetMapping("/version")
    public ResponseEntity<SuccessResponseDto> getVersion(@Value("${spring.application.version}") String version)
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("버전 확인을 성공적으로 완료하였습니다.")
                        .data(version)
                        .build());
    }
}
