package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

// 공개 챌린지 검색 결과 정렬 기준 enum
@AllArgsConstructor
@Getter
public enum SearchChallengeSortBy
{
    NAME("이름순") {
        @Override
        public Sort getSort() {
            return Sort.by("name.keyword").ascending();
        }
    },
    NEWEST("최신순") {
        @Override
        public Sort getSort() {
            return Sort.by("createdAt").descending();
        }
    },
    OLDEST("오래된순") {
        @Override
        public Sort getSort() {
            return Sort.by("createdAt").ascending();
        }
    };

    private final String description;
    public abstract Sort getSort();
}