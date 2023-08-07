package team6.sobun.domain.pin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.exception.InvalidConditionException;

import static team6.sobun.global.stringCode.ErrorCodeEnum.POST_NOT_EXIST;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PinService {

    private final PinRepository pinRepository;
    private final PostRepository postRepository;

    public PostResponseDto updatePin(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));

        String nickname = user.getNickname();
        String postTitle = post.getTitle();   // 게시물 제목 가져오기

        if (!isPinedPost(post, user)) {
            createPin(post, user);
            post.increasePin();
            log.info("'{}'님이 '{}'에 관심을 추가했습니다.", nickname, postTitle);
        } else {
            removePin(post, user);
            post.decreasePin();
            log.info("'{}'님이 '{}'의 관심을 취소했습니다.", nickname, postTitle);
        }

        return new PostResponseDto();
    }



    private boolean isPinedPost(Post post, User user) {
        return pinRepository.findByPostAndUser(post, user).isPresent();
    }

    private void createPin(Post post, User user) {
        Pin pin = new Pin(post, user);
        pinRepository.save(pin);
    }

    private void removePin(Post post, User user) {
        Pin like = pinRepository.findByPostAndUser(post, user).orElseThrow();
        pinRepository.delete(like);
    }


}
