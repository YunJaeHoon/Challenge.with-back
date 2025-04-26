package Challenge.with_back.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// ChallengeRole을 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class ChallengeRoleConverter implements AttributeConverter<ChallengeRole, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(ChallengeRole challengeRole)
    {
        if(challengeRole == null)
            return null;

        return challengeRole.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public ChallengeRole convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return ChallengeRole.valueOf(data);
    }
}
