package Challenge.with_back.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// ChallengeColorTheme를 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class ChallengeColorThemeConverter implements AttributeConverter<ChallengeColorTheme, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(ChallengeColorTheme challengeColorTheme)
    {
        if(challengeColorTheme == null)
            return null;

        return challengeColorTheme.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public ChallengeColorTheme convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return ChallengeColorTheme.valueOf(data);
    }
}
