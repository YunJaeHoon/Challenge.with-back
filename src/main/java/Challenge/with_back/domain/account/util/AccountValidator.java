package Challenge.with_back.domain.account.util;

import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountValidator
{
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
}
