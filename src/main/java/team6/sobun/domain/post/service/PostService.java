package team6.sobun.domain.post.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import static team6.sobun.global.stringCode.ErrorCodeEnum.POST_NOT_EXIST;
import static team6.sobun.global.stringCode.ErrorCodeEnum.USER_NOT_MATCH;
import static team6.sobun.global.stringCode.SuccessCodeEnum.*;
import static team6.sobun.global.utils.ResponseUtils.ok;
import static team6.sobun.global.utils.ResponseUtils.okWithMessage;
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;
    @Autowired
    private final JwtProvider jwtProvider;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    // 최신순 전체조회
    public ApiResponse<?> searchPost(PostSearchCondition condition, Pageable pageable) {
        return ok(postRepository.serachPostByPage(condition, pageable));
    }

    @Transactional
    public ApiResponse<?> createPost(PostRequestDto postRequestDto, MultipartFile image, User user) {
        String imageUrl = s3Service.upload(image);
        postRepository.save(new Post(postRequestDto, imageUrl, user));
        log.info("'{}'님이 새로운 게시물을 생성했습니다.", user.getNickname());
        return ResponseUtils.okWithMessage(POST_CREATE_SUCCESS);
    }

    @Transactional
    public ApiResponse<?> getSinglePost(Long postId , HttpServletRequest req) {
        String token = jwtProvider.getTokenFromHeader(req);
        String subStringToken;
        Boolean isPin=false;
        if(token!=null) {
            subStringToken = jwtProvider.substringHeaderToken(token);
            Claims userInfo = jwtProvider.getUserInfoFromToken(subStringToken);
            Post post = postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("?"));
            User user = userRepository.findByEmail(userInfo.getSubject()).orElseThrow(()->new IllegalArgumentException("?"));

            if(pinRepository.findByPostAndUser(post, user).isPresent()) {
                isPin = true;
            }
        }
        Post post = postRepository.findDetailPost(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));
        log.info("게시물 ID '{}' 조회 성공", postId);
        post.increaseViews();
        return ok(new PostResponseDto(post, isPin));
    }

    @Transactional
    public ApiResponse<?> updatePost(Long postId, PostRequestDto postRequestDto, MultipartFile image, User user) {
        Post post = confirmPost(postId, user);
        updatePostDetail(postRequestDto, image, post);
        log.info("'{}'님이 게시물 ID '{}'의 정보를 업데이트했습니다.", user.getNickname(), postId);
        return okWithMessage(POST_UPDATE_SUCCESS);
    }

    @Transactional
    public ApiResponse<?> deletePost(Long postId, User user) {
        Post post = confirmPost(postId, user);
        deleteImage(post);
        postRepository.delete(post);
        log.info("'{}'님이 게시물 ID '{}'를 삭제했습니다.", user.getNickname(), postId);
        return okWithMessage(POST_DELETE_SUCCESS);
    }
    public ApiResponse<?> markPostInProgress(Long postId) {
        Post post = findPost(postId);
        post.markInProgress();
        return ApiResponse.okWithMessage(SuccessCodeEnum.valueOf("해당 게시물은 진행중 입니다."));
    }

    public ApiResponse<?> markPostClosed(Long postId) {
        Post post = findPost(postId);
        post.markClosed();
        return ApiResponse.okWithMessage(SuccessCodeEnum.valueOf("해당 게시물은 완료 입니다."));
    }



    private void updatePostDetail(PostRequestDto postRequestDto, MultipartFile image, Post post) {
        if (image != null && !image.isEmpty()) {
            String existingImageUrl = post.getImage();
            String imageUrl = s3Service.upload(image);
            post.updateAll(postRequestDto, imageUrl);

            // 새로운 이미지 업로드 후에 기존 이미지 삭제
            if (StringUtils.hasText(existingImageUrl)) {
                s3Service.delete(existingImageUrl);
            }
        }
        post.update(postRequestDto);
    }

    private void deleteImage(Post post) {
        String imageUrl = post.getImage();
        if (StringUtils.hasText(imageUrl)) {
            s3Service.delete(imageUrl);
        }
    }


    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));
    }

    private Post confirmPost(Long postId, User user) {
        Post post = findPost(postId);
        if (!user.getId().equals(post.getUser().getId())) {
            throw new InvalidConditionException(USER_NOT_MATCH);
        }
        return post;
    }
}