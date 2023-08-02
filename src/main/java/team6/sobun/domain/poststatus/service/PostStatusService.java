package team6.sobun.domain.poststatus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.poststatus.entity.PostStatus;
import team6.sobun.domain.poststatus.repository.PostStatusRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.exception.InvalidConditionException;

import static team6.sobun.global.stringCode.ErrorCodeEnum.POST_NOT_EXIST;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostStatusService {

    private final PostStatusRepository postStatusRepository;
    private final PinRepository pinRepository;
    private final PostRepository postRepository;

    public PostResponseDto updatePostStatus(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));

        String nickname = user.getNickname();
        String postTitle = post.getTitle();   // 게시물 제목 가져오기

        if (!isComplete(post, user)) {
            createPostStatus(post, user);
            log.info("'{}'님이 '{}'에 관심을 추가했습니다.", nickname, postTitle);
        } else {
            removePostStatus(post, user);
            log.info("'{}'님이 '{}'의 관심을 취소했습니다.", nickname, postTitle);
        }

        return new PostResponseDto(post, isComplete(post, user), isPinedPost(post, user));
    }

    private Boolean isComplete(Post post, User user) {
        return postStatusRepository.findByPostAndUser(post, user).isPresent();
    }
    private boolean isPinedPost(Post post, User user) {
        return pinRepository.findByPostAndUser(post, user).isPresent();
    }

    private void createPostStatus(Post post, User user) {
        PostStatus postStatus = new PostStatus(post, user);
        postStatusRepository.save(postStatus);
    }

    private void removePostStatus(Post post, User user) {
        PostStatus postStatus = postStatusRepository.findByPostAndUser(post, user).orElseThrow();
        postStatusRepository.delete(postStatus);
    }
}
