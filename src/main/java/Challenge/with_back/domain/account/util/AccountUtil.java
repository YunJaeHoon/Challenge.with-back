package Challenge.with_back.domain.account.util;

import Challenge.with_back.common.repository.rdbms.ParticipateChallengeRepository;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.entity.redis.CheckVerificationCode;
import Challenge.with_back.common.entity.redis.VerificationCode;
import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.common.repository.redis.CheckVerificationCodeRepository;
import Challenge.with_back.common.repository.redis.VerificationCodeRepository;
import Challenge.with_back.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountUtil
{
	private final UserRepository userRepository;
	private final VerificationCodeRepository verificationCodeRepository;
	private final CheckVerificationCodeRepository checkVerificationCodeRepository;
	private final ParticipateChallengeRepository participateChallengeRepository;

	// Authentication 데이터에서 User 엔티티 추출
	public User getUserFromAuthentication(Authentication authentication)
	{
		if (authentication == null || !authentication.isAuthenticated())
			throw new CustomException(CustomExceptionCode.NOT_LOGIN, null);

		Object principal = authentication.getPrincipal();

		if (!(principal instanceof CustomUserDetails userDetails))
			throw new CustomException(CustomExceptionCode.INVALID_AUTHENTICATION, null);

		// 사용자 객체 반환
		return userDetails.getUser();
	}

	// 비밀번호 형식 체크
	public void checkPasswordFormat(String password)
	{
		// 8 ~ 20자
		// 영문, 숫자, 특수문자를 모두 포함
		String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$";
		
		if(!Pattern.matches(regex, password))
			throw new CustomException(CustomExceptionCode.INVALID_PASSWORD_FORMAT, password);
	}
	
	// 닉네임 형식 체크
	public void checkNicknameFormat(String nickname)
	{
		// 2 ~ 12자
		// 영문, 한글, 숫자만 허용
		String regex = "^[A-Za-z0-9가-힣]{2,12}$";
		
		if(!Pattern.matches(regex, nickname))
			throw new CustomException(CustomExceptionCode.INVALID_NICKNAME_FORMAT, nickname);
	}
	
	// 인증번호 일치 여부 확인
	@Transactional(noRollbackFor = CustomException.class)
	public void checkVerificationCodeCorrectness(String email, String code)
	{
		// 인증번호 존재 여부 확인
		VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
													.orElseThrow(() -> new CustomException(CustomExceptionCode.VERIFICATION_CODE_NOT_FOUND, email));
		
		// 인증번호 일치 여부 확인
		if(!verificationCode.getCode().equals(code))
		{
			if(verificationCode.getCountWrong() >= 5)
			{
				verificationCodeRepository.delete(verificationCode);
				
				throw new CustomException(CustomExceptionCode.TOO_MANY_WRONG_VERIFICATION_CODE, null);
			}
			else
			{
				verificationCode.increaseCountWrong();
				verificationCodeRepository.save(verificationCode);
				
				throw new CustomException(CustomExceptionCode.WRONG_VERIFICATION_CODE, null);
			}
		}
		
		// 새로운 인증번호 확인 정보 등록
		CheckVerificationCode checkVerificationCode = CheckVerificationCode.builder()
															  .email(email)
															  .build();
		
		// 생성한 인증번호 확인 정보 저장
		checkVerificationCodeRepository.save(checkVerificationCode);
	}
	
	// 인증번호 삭제
	@Transactional
	public void deleteVerificationCode(String email)
	{
		Optional<VerificationCode> verificationCode = verificationCodeRepository.findByEmail(email);
		verificationCode.ifPresent(verificationCodeRepository::delete);
	}
	
	// 일반 로그인 계정 중복 확인
	public void shouldNotExistingUser(String email)
	{
		if(userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL).isPresent())
			throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, email);
	}
	
	// 일반 로그인 계정 존재 확인
	public User shouldExistingUser(String email)
	{
		return userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL)
					   .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, email));
	}
	
	// 프리미엄 여부 확인
	public boolean isPremium(User user)
	{
		return user.getPremiumExpirationDate().isAfter(LocalDate.now());
	}

	// 챌린지 개수 상한값 확인
	public int getMaxChallengeCount(User user)
	{
		return isPremium(user) ? 20 : 3;
	}

	// 사용자가 이미 참여 챌린지 개수가 최대인지 확인
	public boolean isParticipatingInMaxChallenges(User user)
	{
		return participateChallengeRepository.countAllOngoing(user) >= getMaxChallengeCount(user);
	}
}
