package Challenge.with_back.domain.basic.controller;

import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BasicController
{
    // 버전 확인
    @GetMapping("/version")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getVersion(@Value("${spring.application.version}") String version)
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("버전 확인을 성공적으로 완료하였습니다.")
                        .data(version)
                        .build());
    }

    // 사용자 권한 계정 테스트
    @GetMapping("/test-user")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> testUser(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("사용자 권한 이상입니다.")
                        .data(user.getId())
                        .build());
    }

    // 관리자 권한 계정 테스트
    @GetMapping("/test-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SuccessResponseDto> testAdmin(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("관리자 권한 이상입니다.")
                        .data(user.getId())
                        .build());
    }
}
