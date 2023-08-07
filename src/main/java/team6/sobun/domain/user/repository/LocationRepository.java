package team6.sobun.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.user.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
