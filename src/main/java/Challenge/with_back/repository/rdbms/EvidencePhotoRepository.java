package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.EvidencePhoto;
import Challenge.with_back.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.entity.rdbms.ParticipatePhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidencePhotoRepository extends JpaRepository<EvidencePhoto, Long>
{
    // 페이즈 참여 정보로 모든 증거사진 조회
    List<EvidencePhoto> findAllByParticipatePhase(ParticipatePhase participatePhase);
}
