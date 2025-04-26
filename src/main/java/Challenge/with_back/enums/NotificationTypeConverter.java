package Challenge.with_back.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Notification을 데이터베이스 컬럼으로 저장하기 위한 converter
@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String>
{
    // 데이터베이스에 저장할 때, 작동하는 함수
    @Override
    public String convertToDatabaseColumn(NotificationType notificationType)
    {
        if(notificationType == null)
            return null;

        return notificationType.name();
    }

    // 데이터베이스에서 가져올 때, 작동하는 함수
    @Override
    public NotificationType convertToEntityAttribute(String data)
    {
        if(data == null)
            return null;

        return NotificationType.valueOf(data);
    }
}
