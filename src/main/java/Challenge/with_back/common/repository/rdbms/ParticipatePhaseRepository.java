package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipatePhaseRepository extends JpaRepository<ParticipatePhase, Long>
{
    // 페이즈 ID로 페이즈 참가 데이터 리스트 조회
    List<ParticipatePhase> findAllByPhaseId(Long phaseId);

    // 페이즈 ID, 사용자 ID로 페이즈 참가 데이터 조회
    Optional<ParticipatePhase> findByPhaseIdAndUserId(Long phaseId, Long userId);
}
