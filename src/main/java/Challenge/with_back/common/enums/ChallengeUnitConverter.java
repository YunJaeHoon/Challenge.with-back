package Challenge.with_back.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// ChallengeUnit을 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class ChallengeUnitConverter implements AttributeConverter<ChallengeUnit, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(ChallengeUnit challengeUnit)
    {
        if(challengeUnit == null)
            return null;

        return challengeUnit.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public ChallengeUnit convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return ChallengeUnit.valueOf(data);
    }
}
