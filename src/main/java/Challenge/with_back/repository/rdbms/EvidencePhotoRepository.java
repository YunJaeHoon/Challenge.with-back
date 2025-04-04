package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.EvidencePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidencePhotoRepository extends JpaRepository<EvidencePhoto, Long> {
}
