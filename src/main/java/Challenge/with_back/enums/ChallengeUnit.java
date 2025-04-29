package Challenge.with_back.enums;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

// 챌린지 단위 enum
@AllArgsConstructor
public enum ChallengeUnit
{
    DAILY("매일",
            startDate -> startDate,
            startDate -> Math.toIntExact(Math.abs(ChronoUnit.DAYS.between(LocalDate.now(), startDate) + 1))),
    WEEKLY("매주",
            startDate -> startDate.plusWeeks(1),
            startDate -> Math.toIntExact(Math.abs(ChronoUnit.WEEKS.between(LocalDate.now(), startDate) + 1))),
    MONTHLY("매월",
            startDate -> startDate.plusMonths(1),
            startDate -> Math.toIntExact(Math.abs(ChronoUnit.MONTHS.between(LocalDate.now(), startDate) + 1)));

    private final String description;
    private final Function<LocalDate, LocalDate> phaseEndDateCalculator;
    private final Function<LocalDate, Integer> currentPhaseNumberCalculator;

    // 페이즈 종료 날짜 계산
    public LocalDate calcPhaseEndDate(LocalDate startDate) {
        return phaseEndDateCalculator.apply(startDate);
    }

    // 현재 페이즈 번호 계산
    public int calcCurrentPhaseNumber(LocalDate startDate) {
        return currentPhaseNumberCalculator.apply(startDate);
    }
}
