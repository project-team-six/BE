package team6.sobun.domain.user.repository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.commentList c WHERE p.user.id = :userId")
    List<Post> findPostsByUserId(@Param("userId") Long userId);

    @Query("SELECT p.post FROM Pin p WHERE p.user.id = :userId")
    List<Post> findPinnedPostsByUserId(@Param("userId") Long userId);

}

