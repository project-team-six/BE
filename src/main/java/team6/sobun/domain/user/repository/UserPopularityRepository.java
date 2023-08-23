package team6.sobun.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserPopularity;

public interface UserPopularityRepository extends JpaRepository<UserPopularity, Long> {
    UserPopularity findByReceiverAndGiver(User Receiver, User Giver);
}
