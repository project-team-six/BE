package team6.sobun.global.security.repository;

import org.springframework.data.repository.CrudRepository;
import team6.sobun.global.jwt.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findById(String email);
}