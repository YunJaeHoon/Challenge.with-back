package Challenge.with_back.entity.rdbms;

import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.enums.ChallengeUnitConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
	
	// 아이콘 URL
	@NotNull
	@Column(length = 255)
	private String iconUrl;
	
	// 색 테마
	@NotNull
	@Column(columnDefinition = "char(6)")
	private String colorTheme;
	
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
	
	// 현재 참여자 인원수
	@NotNull
	private int countCurrentParticipant;
	
	// 페이즈 총 개수
	@NotNull
	private int countPhase;
	
	// 마지막 활동 날짜
	@NotNull
	private LocalDate lastActiveDate;
	
	@Builder
	public Challenge(User superAdmin, String iconUrl, String colorTheme, String name, String description, int goalCount, ChallengeUnit unit, boolean isPublic, int maxParticipantCount, int countCurrentParticipant, int countPhase, LocalDate lastActiveDate) {
		this.superAdmin = superAdmin;
		this.iconUrl = iconUrl;
		this.colorTheme = colorTheme;
		this.name = name;
		this.description = description;
		this.goalCount = goalCount;
		this.unit = unit;
		this.isPublic = isPublic;
		this.maxParticipantCount = maxParticipantCount;
		this.countCurrentParticipant = countCurrentParticipant;
		this.countPhase = countPhase;
		this.lastActiveDate = lastActiveDate;
	}

	// 페이즈 개수 1개 증가
	public void increaseCountPhase() {
		this.countPhase++;
	}

	// 참여자 인원수 1명 증가
	public void increaseCountCurrentParticipant() {
		this.countCurrentParticipant++;
	}

	// 참여자 인원수 1명 감소
	public void decreaseCountCurrentParticipant() {
		this.countCurrentParticipant--;
	}
}
