package team6.sobun.domain.poststatus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.poststatus.entity.PostStatus;
import team6.sobun.domain.poststatus.repository.PostStatusRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.responseDto.ApiResponse;

import static team6.sobun.global.stringCode.ErrorCodeEnum.POST_NOT_EXIST;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostStatusService {

    private final PostStatusRepository postStatusRepository;
    private final PostRepository postRepository;

    public ApiResponse<?> updatePostStatus(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));

        String nickname = user.getNickname();
        String postTitle = post.getTitle();   // 게시물 제목 가져오기

        if (!isComplete(post, user)) {
            createPostStatus(post, user);
            log.info("'{}'님이 '{}' 게시물을 마감합니다.", nickname, postTitle);
            post.markClosed(); // Post 엔티티의 status를 마감으로 변경
        } else {
            removePostStatus(post, user);
            log.info("'{}'님이 '{}' 게시물을 진행합니다.", nickname, postTitle);
            post.markInProgress(); // Post 엔티티의 status를 진행중으로 변경
        }
        String statusMessage = isComplete(post, user) ? team6.sobun.domain.post.entity.PostStatus.COMPLETED.getMessage() : team6.sobun.domain.post.entity.PostStatus.IN_PROGRESS.getMessage();
        return ApiResponse.success(statusMessage);
    }

    private Boolean isComplete(Post post, User user) {
        return postStatusRepository.findByPostAndUser(post, user).isPresent();
    }
    private void createPostStatus(Post post, User user) {
        PostStatus postStatus = new PostStatus(post, user);
        postStatus.markCompleted(); // 생성시에 마감 상태로 변경
        postStatusRepository.save(postStatus);
    }

    private void removePostStatus(Post post, User user) {
        PostStatus postStatus = postStatusRepository.findByPostAndUser(post, user).orElseThrow();
        postStatus.markInProgress(); // 삭제시에 진행 상태로 변경
        postStatusRepository.delete(postStatus);
    }
}
