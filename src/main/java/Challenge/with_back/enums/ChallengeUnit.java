package Challenge.with_back.enums;

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
            (createAt, number) -> createAt.plusDays(number - 1),
            startDate -> startDate,
            createAt -> Math.toIntExact(Math.abs(ChronoUnit.DAYS.between(LocalDate.now(), createAt) + 1))),
    WEEKLY("매주",
            (createAt, number) -> createAt.plusWeeks(number - 1),
            startDate -> startDate.plusWeeks(1),
            createAt -> Math.toIntExact(Math.abs(ChronoUnit.WEEKS.between(LocalDate.now(), createAt) + 1))),
    MONTHLY("매월",
            (createAt, number) -> createAt.plusMonths(number - 1),
            startDate -> startDate.plusMonths(1),
            createAt -> Math.toIntExact(Math.abs(ChronoUnit.MONTHS.between(LocalDate.now(), createAt) + 1)));

    private final String description;
    private final BiFunction<LocalDate, Integer, LocalDate> phaseStartDateCalculator;
    private final Function<LocalDate, LocalDate> phaseEndDateCalculator;
    private final Function<LocalDate, Integer> currentPhaseNumberCalculator;

    // 페이즈 시작 날짜 계산
    public LocalDate calcPhaseStartDate(LocalDate createAt, int number) {
        return phaseStartDateCalculator.apply(createAt, number);
    }

    // 페이즈 종료 날짜 계산
    public LocalDate calcPhaseEndDate(LocalDate startDate) {
        return phaseEndDateCalculator.apply(startDate);
    }

    // 현재 페이즈 번호 계산
    public int calcCurrentPhaseNumber(LocalDate createAt) {
        return currentPhaseNumberCalculator.apply(createAt);
    }
}
