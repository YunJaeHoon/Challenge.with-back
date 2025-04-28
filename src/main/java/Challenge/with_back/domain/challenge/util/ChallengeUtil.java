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
	
	// 챌린지 목표 개수 크기 체크
	public void checkGoalCount(int goalCount)
	{
		if(goalCount <= 0 || goalCount > 100)
			throw new CustomException(CustomExceptionCode.INVALID_CHALLENGE_GOAL_COUNT, goalCount);
	}

	// 새로운 페이즈 생성
	@Transactional
	public void createPhase(Challenge challenge)
	{
		// 챌린지의 페이즈 개수 1개 증가
		challenge.increaseCountPhase();
		challengeRepository.save(challenge);

		// 페이즈 생성
		Phase phase = Phase.builder()
				.challenge(challenge)
				.name(challenge.getCountPhase() + "번째 페이즈")
				.description("")
				.number(challenge.getCountPhase())
				.startDate(LocalDate.now())
				.endDate(challenge.getUnit().calcPhaseEndDate(LocalDate.now()))
				.build();

		// 페이즈 저장
		phaseRepository.save(phase);
	}

	// 현재 페이즈 조회
	public Phase getLastPhase(Challenge challenge)
	{
		return phaseRepository.findByChallengeAndNumber(challenge, challenge.getCountPhase())
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
		Phase phase = getLastPhase(challenge);

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

	// 챌린지 참여 정보가 해당 사용자 것인지 확인
	public void checkParticipateChallengeOwnership(User user, Long participateChallengeId)
	{
		ParticipateChallenge participateChallenge = participateChallengeRepository.findById(participateChallengeId)
				.orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, null));

		if(!participateChallenge.getUser().equals(user))
			throw new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_OWNED, null);
	}

	public void checkParticipateChallengeOwnership(User user, ParticipateChallenge participateChallenge)
	{
		if(!participateChallenge.getUser().getId().equals(user.getId()))
			throw new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_OWNED, null);
	}

	// 페이즈 참여 정보가 해당 사용자 것인지 확인
	public void checkParticipatePhaseOwnership(User user, Long participatePhaseId)
	{
		ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
				.orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

		if(!participatePhase.getUser().equals(user))
			throw new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_OWNED, null);
	}

	public void checkParticipatePhaseOwnership(User user, ParticipatePhase participatePhase)
	{
		if(!participatePhase.getUser().getId().equals(user.getId()))
			throw new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_OWNED, null);
	}
}
