package team6.sobun.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserPopularity;
import team6.sobun.domain.user.repository.UserPopularityRepository;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.responseDto.ApiResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPopularityService {

    private final UserRepository userRepository;
    private final UserPopularityRepository userPopularityRepository;

    public ApiResponse<?> popularity(Long receiverUserId, User user) {
        User receiverUser = userRepository.findById(receiverUserId).orElseThrow(()
                -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));
        if (userPopularityRepository.findByReceiverAndGiver(receiverUser, user) == null) {
            UserPopularity userPopularity = new UserPopularity(receiverUser, user);
            userPopularityRepository.save(userPopularity);
            receiverUser.increasePopularity();
            userRepository.save(receiverUser);
            return ApiResponse.success(receiverUser.getNickname() + "님이 " + user.getNickname() + "님의 인기도를 올렸습니다.");
        } else {
            userPopularityRepository.delete(userPopularityRepository.findByReceiverAndGiver(receiverUser, user));
            receiverUser.DecreasePopularity();
            userRepository.save(receiverUser);
            return ApiResponse.success(receiverUser.getNickname() + "님이 " + user.getNickname() + "님의 인기도를 뺏앗았습니다.");
        }
    }
}
