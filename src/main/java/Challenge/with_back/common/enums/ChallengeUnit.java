package Challenge.with_back.common.enums;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.function.Function;

// 챌린지 단위 enum
@AllArgsConstructor
public enum ChallengeUnit
{
    DAILY("매일", startDate -> startDate),
    WEEKLY("매주", startDate -> startDate.plusWeeks(1)),
    MONTHLY("매월", startDate -> startDate.plusMonths(1));

    private final String description;
    private final Function<LocalDate, LocalDate> calculator;

    // 페이즈 종료 날짜 계산
    public LocalDate calcPhaseEndDate(LocalDate startDate) {
        return calculator.apply(startDate);
    }
}
