package Challenge.with_back.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// InquiryCategory를 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class InquiryCategoryConverter implements AttributeConverter<InquiryCategory, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(InquiryCategory inquiryCategory)
    {
        if(inquiryCategory == null)
            return null;

        return inquiryCategory.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public InquiryCategory convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return InquiryCategory.valueOf(data);
    }
}
