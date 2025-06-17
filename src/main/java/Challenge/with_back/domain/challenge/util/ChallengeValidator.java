package Challenge.with_back.domain.challenge.util;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeValidator
{
	// 챌린지 색상 테마 이름으로 색상 코드 찾기
	public ChallengeColorTheme getColor(String colorThemeName)
	{
		try {
			return ChallengeColorTheme.valueOf(colorThemeName);
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
}
