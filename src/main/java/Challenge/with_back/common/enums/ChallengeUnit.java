package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

// 챌린지 단위 enum
@AllArgsConstructor
public enum ChallengeUnit
{
    DAILY("매일",
            (createdAt, number) -> createdAt.plusDays(number - 1),
            startDate -> startDate,
            createdAt -> (int) ChronoUnit.DAYS.between(createdAt, LocalDate.now()) + 1),
    WEEKLY("매주",
            (createdAt, number) -> createdAt.plusWeeks(number - 1),
            startDate -> startDate.plusWeeks(1).minusDays(1),
            createdAt -> (int) ChronoUnit.WEEKS.between(createdAt, LocalDate.now()) + 1),
    MONTHLY("매월",
            (createdAt, number) -> createdAt.plusMonths(number - 1),
            startDate -> startDate.plusMonths(1).minusDays(1),
            createdAt -> (int) ChronoUnit.MONTHS.between(createdAt, LocalDate.now()) + 1);

    private final String description;
    private final BiFunction<LocalDate, Integer, LocalDate> phaseStartDateCalculator;
    private final Function<LocalDate, LocalDate> phaseEndDateCalculator;
    private final Function<LocalDate, Integer> currentPhaseNumberCalculator;

    // 페이즈 시작 날짜 계산
    public LocalDate calcPhaseStartDate(LocalDate createdAt, int number) {
        return phaseStartDateCalculator.apply(createdAt, number);
    }

    // 페이즈 종료 날짜 계산
    public LocalDate calcPhaseEndDate(LocalDate startDate) {
        return phaseEndDateCalculator.apply(startDate);
    }

    // 현재 페이즈 번호 계산
    public int calcCurrentPhaseNumber(LocalDate createdAt) {
        return currentPhaseNumberCalculator.apply(createdAt);
    }
}
