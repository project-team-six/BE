package team6.sobun.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByUser(User user);
}
