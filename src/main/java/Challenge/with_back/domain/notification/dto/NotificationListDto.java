package Challenge.with_back.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListDto
{
	private List<NotificationDto> content;		// 알림 리스트
	private Boolean isLast;									// 마지막 페이지인가?
}
