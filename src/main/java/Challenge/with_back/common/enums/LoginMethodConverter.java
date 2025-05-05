package Challenge.with_back.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// LoginMethod를 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class LoginMethodConverter implements AttributeConverter<LoginMethod, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(LoginMethod loginMethod)
    {
        if(loginMethod == null)
            return null;

        return loginMethod.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public LoginMethod convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return LoginMethod.valueOf(data);
    }
}
