package Challenge.with_back.common.elastic_search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ChallengeDocumentRepository extends ElasticsearchRepository<ChallengeDocument, Long> {
}
