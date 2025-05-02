package Challenge.with_back.domain.challenge.util;

import Challenge.with_back.enums.ChallengeColorTheme;
import Challenge.with_back.enums.ChallengeIcon;
import Challenge.with_back.enums.ChallengeRole;
import Challenge.with_back.enums.ChallengeUnit;
import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.entity.rdbms.*;
import Challenge.with_back.repository.rdbms.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeUtil
{
	private final UserRepository userRepository;
	private final ChallengeRepository challengeRepository;
	private final PhaseRepository phaseRepository;
	private final ParticipateChallengeRepository participateChallengeRepository;
	private final ParticipatePhaseRepository participatePhaseRepository;

	private final AccountUtil accountUtil;

	// 아이콘 이름으로 아이콘 URL 찾기
	public String getIconUrl(String iconName)
	{
		try {
			return ChallengeIcon.valueOf(iconName).getUrl();
		} catch (IllegalArgumentException e) {
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_ICON, iconName);
		}
	}
	
	// 챌린지 색상 테마 이름으로 색상 코드 찾기
	public String getColor(String colorThemeName)
	{
		try {
			return ChallengeColorTheme.valueOf(colorThemeName).getColor();
		} catch (IllegalArgumentException e) {
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_COLOR_THEME, colorThemeName);
		}
	}
	
	// 챌린지 단위 이름으로 단위 찾기
	public ChallengeUnit getUnit(String unitName)
	{
		try {
			return ChallengeUnit.valueOf(unitName);
		} catch (IllegalArgumentException e) {
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_UNIT, unitName);
		}
	}
	
	// 챌린지 이름 길이 체크
	public void checkNameLength(String name)
	{
		if(name.trim().isEmpty() || name.trim().length() > 255)
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_NAME_FORMAT, name);
	}
	
	// 챌린지 설명 길이 체크
	public void checkDescriptionLength(String description)
	{
		if(description.trim().isEmpty() || description.trim().length() > 255)
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_DESCRIPTION_FORMAT, description);
	}

	// 페이즈 참여 정보 한마디 길이 체크
	public void checkParticipatePhaseCommentLength(String comment)
	{
		if(comment.trim().length() > 1000)
			throw new CustomException(CustomExceptionCode.INVALID_PARTICIPATE_PHASE_COMMENT, comment);
	}

	// 해당 값이 달성 개수 범위에 속하는지 체크
	public void checkCurrentCount(int value, Challenge challenge)
	{
		if(value < 0 || value > challenge.getGoalCount())
			throw new CustomException(CustomExceptionCode.INVALID_PARTICIPATE_PHASE_CURRENT_COUNT, value);
	}
	
	// 챌린지 목표 개수 크기 체크
	public void checkGoalCount(int goalCount)
	{
		if(goalCount <= 0 || goalCount > 100)
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_GOAL_COUNT, goalCount);
	}

	// 현재 페이즈 조회
	public Phase getCurrentPhase(Challenge challenge)
	{
		return phaseRepository.findByChallengeAndNumber(challenge, challenge.calcCurrentPhaseNumber())
				.orElseThrow(() -> new CustomException(CustomExceptionCode.PHASE_NOT_FOUND, null));
	}

	// 챌린지 가입
	@Transactional
	public void joinChallenge(Challenge challenge, User user, ChallengeRole role)
	{
		// 이미 챌린지에 참여자가 가득 찼는지 확인
		if(challenge.getMaxParticipantCount() == challenge.getCountCurrentParticipant())
			throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, null);

		// 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
		if(accountUtil.isParticipatingInMaxChallenges(user))
			throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);

		// 이미 사용자가 해당 챌린지에 가입했는지 확인
		if(participateChallengeRepository.findByUserAndChallenge(user, challenge).isPresent())
			throw new CustomException(CustomExceptionCode.ALREADY_PARTICIPATING_CHALLENGE, null);

		// 챌린지 참여 정보 생성
		ParticipateChallenge participateChallenge = ParticipateChallenge.builder()
				.user(user)
				.challenge(challenge)
				.determination("")
				.challengeRole(role)
				.countSuccess(0)
				.countExemption(0)
				.isPublic(true)
				.lastActiveDate(LocalDate.now())
				.build();

		// 챌린지 참여 정보 저장
		participateChallengeRepository.save(participateChallenge);

		// 현재 페이즈 조회
		Phase phase = getCurrentPhase(challenge);

		// 페이즈 참여 정보 생성
		ParticipatePhase participatePhase = ParticipatePhase.builder()
				.user(user)
				.phase(phase)
				.currentCount(0)
				.isExempt(false)
				.comment("")
				.countEvidencePhoto(0)
				.build();

		// 페이즈 참여 정보 저장
		participatePhaseRepository.save(participatePhase);

		// 챌린지 참여자 인원수 1명 증가
		challenge.increaseCountCurrentParticipant();
		challengeRepository.save(challenge);

		// 사용자 참여 챌린지 개수 1개 증가
		user.increaseCountParticipateChallenge();
		userRepository.save(user);
	}

	// 다음 페이즈 생성
	@Transactional
	public void createPhases(Challenge challenge, int count)
	{
		// 챌린지 참여 정보 리스트
		List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByChallengeOrderByCreatedAtDesc(challenge);

		// 페이즈 리스트
		List<Phase> phaseList = new ArrayList<>();

		// 페이즈 참여 정보 리스트
		List<ParticipatePhase> participatePhaseList = new ArrayList<>();

		for(int i = 0; i < count; i++)
		{
			// 챌린지의 페이즈 개수 증가
			challenge.increaseCountPhase();

			// 페이즈 시작 날짜 및 종료 날짜 계산
			LocalDate startDate = challenge.calcPhaseStartDate(challenge.getCountPhase());
			LocalDate endDate = challenge.getUnit().calcPhaseEndDate(startDate);

			// 페이즈 생성
			Phase phase = Phase.builder()
					.challenge(challenge)
					.name(challenge.getCountPhase() + "번째 페이즈")
					.description("")
					.number(challenge.getCountPhase())
					.startDate(startDate)
					.endDate(endDate)
					.build();

			phaseList.add(phase);

			// 페이즈 참여 정보 생성
			participateChallengeList.forEach(participateChallenge -> {
				ParticipatePhase participatePhase = ParticipatePhase.builder()
						.user(participateChallenge.getUser())
						.phase(phase)
						.currentCount(0)
						.isExempt(false)
						.comment("")
						.countEvidencePhoto(0)
						.build();

				participatePhaseList.add(participatePhase);
			});

			challengeRepository.save(challenge);
		}

		phaseRepository.saveAll(phaseList);
		participatePhaseRepository.saveAll(participatePhaseList);
	}

	// 페이즈 참여 정보 소유자 확인
	public void checkParticipatePhaseOwnership(User user, ParticipatePhase participatePhase)
	{
		if(!participatePhase.getUser().getId().equals(user.getId()))
			throw new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_OWNED, null);
	}

	// 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
	@Transactional
	public void renewLastActiveDate(ParticipatePhase participatePhase)
	{
		// 사용자
		User user = participatePhase.getUser();

		// 페이즈
		Phase phase = participatePhase.getPhase();

		// 챌린지
		Challenge challenge = phase.getChallenge();

		// 챌린지 마지막 활동 날짜 갱신
		challenge.renewLastActiveDate();
		challengeRepository.save(challenge);

		// 챌린지 참여 정보
		ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserAndChallenge(user, challenge)
				.orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, challenge.getId()));

		// 챌린지 참여 정보 마지막 활동 날짜 갱신
		participateChallenge.renewLastActiveDate();
		participateChallengeRepository.save(participateChallenge);
	}
}
