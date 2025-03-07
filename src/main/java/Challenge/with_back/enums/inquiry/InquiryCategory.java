package Challenge.with_back.enums.inquiry;

import lombok.AllArgsConstructor;

// 문의 카테고리 enum
@AllArgsConstructor
public enum InquiryCategory
{
    ACCOUNT("계정 관련 문의"),
    CHALLENGE("챌린지 관련 문의"),
    ETC("기타 문의");

    private final String description;
}
