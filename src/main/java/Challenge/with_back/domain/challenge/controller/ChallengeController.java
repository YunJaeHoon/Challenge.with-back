package Challenge.with_back.domain.challenge.controller;

import Challenge.with_back.aop.annotation.PremiumOnly;
import Challenge.with_back.domain.challenge.dto.UpdateCommentDto;
import Challenge.with_back.response.success.CustomSuccessCode;
import Challenge.with_back.response.success.SuccessResponseDto;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.dto.GetMyChallengeDto;
import Challenge.with_back.domain.challenge.service.ChallengeService;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChallengeController
{
    private final ChallengeService challengeService;

    // 챌린지 생성
    @PostMapping("/challenge")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createChallenge(@RequestBody CreateChallengeDto dto,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        challengeService.createChallenge(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("챌린지 생성을 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 내 챌린지 조회
    @GetMapping("/challenge/me")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getMyChallenges(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        GetMyChallengeDto data = challengeService.getMyChallenges(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("내 챌린지 조회를 성공적으로 완료하였습니다.")
                        .data(data)
                        .build());
    }

    // 증거사진 등록
    @PostMapping("/participate-phase/{participatePhaseId}/evidence-photo")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PremiumOnly
    public ResponseEntity<SuccessResponseDto> uploadEvidencePhotos(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable Long participatePhaseId,
                                                                   @RequestPart(value = "images") List<MultipartFile> images)
    {
        User user = userDetails.getUser();
        challengeService.uploadEvidencePhotos(user, participatePhaseId, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("증거사진을 성공적으로 등록하였습니다.")
                        .data(null)
                        .build());
    }

    // 증거사진 삭제
    @DeleteMapping("/evidence-photo/{evidencePhotoId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PremiumOnly
    public ResponseEntity<SuccessResponseDto> deleteEvidencePhoto(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable Long evidencePhotoId)
    {
        User user = userDetails.getUser();
        challengeService.deleteEvidencePhoto(user, evidencePhotoId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("증거사진을 성공적으로 삭제하였습니다.")
                        .data(null)
                        .build());
    }

    // 페이즈 참여 정보 한마디 수정
    @PatchMapping("/participate-phase/{participatePhaseId}/comment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> updateParticipatePhaseComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                            @PathVariable Long participatePhaseId,
                                                                            @RequestBody UpdateCommentDto dto)
    {
        User user = userDetails.getUser();
        challengeService.updateParticipatePhaseComment(user, participatePhaseId, dto.getComment());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("한마디를 성공적으로 수정하였습니다.")
                        .data(null)
                        .build());
    }
}
