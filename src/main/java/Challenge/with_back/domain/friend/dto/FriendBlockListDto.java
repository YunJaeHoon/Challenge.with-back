package Challenge.with_back.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FriendBlockListDto
{
    private List<FriendBlockDto> friendBlockList;   // 친구 차단 리스트

    private int pageSize;                   // 페이지 사이즈
    private int currentPageNumber;          // 현재 페이지 번호
    private int totalPageCount;             // 전체 페이지 개수
    private Boolean isLastPage;             // 마지막 페이지인가?
}
