package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.EvidencePhoto;
import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidencePhotoRepository extends JpaRepository<EvidencePhoto, Long>
{
    // 페이즈 참여 정보로 모든 증거사진 조회
    List<EvidencePhoto> findAllByParticipatePhase(ParticipatePhase participatePhase);

    // 페이즈 참여 정보로 모든 증거사진 개수 세기
    int countAllByParticipatePhase(ParticipatePhase participatePhase);
}
