package Challenge.with_back.common.entity.rdbms;

import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.enums.ChallengeColorThemeConverter;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.enums.ChallengeUnitConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Challenge extends BasicEntity
{
	@Id
	@Column(nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// 최고 관리자 계정
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "super_admin")
	private User superAdmin;
	
	// 아이콘
	@NotNull
	@Column(length = 255)
	private String icon;
	
	// 색 테마
	@NotNull
	@Convert(converter = ChallengeColorThemeConverter.class)
	private ChallengeColorTheme colorTheme;
	
	// 이름
	@NotNull
	@Column(length = 255)
	private String name;
	
	// 설명
	@Column(columnDefinition = "TEXT")
	private String description;
	
	// 목표 개수
	@Column(columnDefinition = "SMALLINT")
	private int goalCount;
	
	// 단위
	@NotNull
	@Convert(converter = ChallengeUnitConverter.class)
	private ChallengeUnit unit;
	
	// 공개 챌린지 인가?
	@NotNull
	private boolean isPublic;
	
	// 최대 참여자 인원수
	@NotNull
	private int maxParticipantCount;

	// 만들어 놓은 페이즈 총 개수
	@NotNull
	private int countPhase;
	
	// 마지막 활동 날짜
	@NotNull
	private LocalDate lastActiveDate;

	// 종료했는가?
	@NotNull
	private boolean isFinished;

	// 페이즈 시작 날짜 계산
	public LocalDate calcPhaseStartDate(int number) {
		return this.unit.calcPhaseStartDate(this.getCreatedAt().toLocalDate(), number);
	}

	// 현재 페이즈 번호 계산
	public int calcCurrentPhaseNumber() {
		return this.unit.calcCurrentPhaseNumber(this.getCreatedAt().toLocalDate());
	}

	// 마지막 활동 날짜 갱신
	public void renewLastActiveDate() {
		this.lastActiveDate = LocalDate.now();
	}

	// 페이즈 개수 1개 증가
	public void increaseCountPhase() {
		this.countPhase++;
	}
}
