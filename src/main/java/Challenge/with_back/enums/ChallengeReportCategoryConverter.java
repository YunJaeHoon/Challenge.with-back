package Challenge.with_back.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// ChallengeReportCategory를 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class ChallengeReportCategoryConverter implements AttributeConverter<ChallengeReportCategory, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(ChallengeReportCategory challengeReportCategory)
    {
        if(challengeReportCategory == null)
            return null;

        return challengeReportCategory.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public ChallengeReportCategory convertToEntityAttribute(String data)
    {
        if (data == null)
            return null;

        return ChallengeReportCategory.valueOf(data);
    }
}
