package Challenge.with_back.domain.update_participate_phase.controller;

import Challenge.with_back.common.aop.annotation.PremiumOnly;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.CustomSuccessCode;
import Challenge.with_back.common.response.SuccessResponseDto;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.challenge.dto.EvidencePhotoDto;
import Challenge.with_back.domain.update_participate_phase.dto.UpdateCommentDto;
import Challenge.with_back.domain.update_participate_phase.dto.UpdateCurrentCountDto;
import Challenge.with_back.domain.update_participate_phase.service.UpdateParticipatePhaseService;
import jakarta.validation.Valid;
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
public class UpdateParticipatePhaseController
{
    private final UpdateParticipatePhaseService updateParticipatePhaseService;

    // 증거사진 등록
    @PostMapping("/participate-phase/{participatePhaseId}/evidence-photo")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PremiumOnly
    public ResponseEntity<SuccessResponseDto> uploadEvidencePhotos(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable Long participatePhaseId,
                                                                   @RequestPart(value = "images") List<MultipartFile> images)
    {
        User user = userDetails.getUser();
        List<EvidencePhotoDto> data = updateParticipatePhaseService.uploadEvidencePhotos(user, participatePhaseId, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("증거사진을 성공적으로 등록하였습니다.")
                        .data(data)
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
        updateParticipatePhaseService.deleteEvidencePhoto(user, evidencePhotoId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("증거사진을 성공적으로 삭제하였습니다.")
                        .data(null)
                        .build());
    }

    // 페이즈 참여 정보 한마디 변경
    @PatchMapping("/participate-phase/{participatePhaseId}/comment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> updateParticipatePhaseComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                            @PathVariable Long participatePhaseId,
                                                                            @Valid @RequestBody UpdateCommentDto dto)
    {
        User user = userDetails.getUser();
        updateParticipatePhaseService.sendUpdateParticipatePhaseComment(user, participatePhaseId, dto.getComment());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("한마디 변경을 성공적으로 요청하였습니다.")
                        .data(null)
                        .build());
    }

    // 페이즈 참여 정보 현재 달성 개수 변경
    @PatchMapping("/participate-phase/{participatePhaseId}/current-count")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> updateParticipatePhaseCurrentCount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                 @PathVariable Long participatePhaseId,
                                                                                 @Valid @RequestBody UpdateCurrentCountDto dto)
    {
        User user = userDetails.getUser();
        updateParticipatePhaseService.sendUpdateParticipatePhaseCurrentCount(user, participatePhaseId, dto.getValue());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("현재 달성 개수 변경을 성공적으로 요청하였습니다.")
                        .data(null)
                        .build());
    }

    // 페이즈 참여 정보 면제 여부 토글
    @PatchMapping("/participate-phase/{participatePhaseId}/is-exempt")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> toggleIsExempt(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @PathVariable Long participatePhaseId)
    {
        User user = userDetails.getUser();
        updateParticipatePhaseService.sendToggleIsExempt(user, participatePhaseId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("면제 여부 변경을 성공적으로 요청하였습니다.")
                        .data(null)
                        .build());
    }
}
