package Challenge.with_back.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// AccountRole을 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class AccountRoleConverter implements AttributeConverter<AccountRole, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(AccountRole accountRole)
    {
        if(accountRole == null)
            return null;

        return accountRole.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public AccountRole convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return AccountRole.valueOf(data);
    }
}
