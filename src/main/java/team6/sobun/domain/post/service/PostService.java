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
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.post.dto.PostReportRequestDto;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.entity.PostReport;
import team6.sobun.domain.post.repository.PostReportRepository;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.poststatus.repository.PostStatusRepository;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.utils.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

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
    private final ChatService chatService;
    @Autowired
    private final JwtProvider jwtProvider;
    private final PinRepository pinRepository;
    private final PostStatusRepository postStatusRepository;
    private final UserRepository userRepository;
    private final PostReportRepository postReportRepository;

    // 최신순 전체조회
    public ApiResponse<?> searchPost(PostSearchCondition condition, Pageable pageable) {
        return ok(postRepository.searchPostByPage(condition, pageable));
    }

    @Transactional
    public ApiResponse<?> createPost(PostRequestDto postRequestDto, List<MultipartFile> images, User user) {
        List<String> imageUrlList = s3Service.uploads(images);
        postRequestDto.setImageUrlList(imageUrlList);
        ChatRoomEntity chatRoom = chatService.createRoomByPost(postRequestDto,user);
        String roomId = chatRoom.getRoomId();
        postRepository.save(new Post(postRequestDto, imageUrlList, user, roomId));
        log.info("'{}'님이 새로운 게시물을 생성했습니다.", user.getNickname());
        return ResponseUtils.okWithMessage(POST_CREATE_SUCCESS);
    }

    @Transactional
    public ApiResponse<?> getSinglePost(Long postId, HttpServletRequest req) {
        String token = jwtProvider.getTokenFromHeader(req);
        String subStringToken;
        Boolean isPined = false;
        Boolean isComplete = false;
        if (token != null) {
            subStringToken = jwtProvider.substringHeaderToken(token);
            Claims userInfo = jwtProvider.getUserInfoFromToken(subStringToken);
            Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("?"));
            User user = userRepository.findByEmail(userInfo.getSubject()).orElseThrow(() -> new IllegalArgumentException("?"));

            if (pinRepository.findByPostAndUser(post, user).isPresent()) {
                isPined = true;
            }
            if (postStatusRepository.findByPostAndUser(post, user).isPresent()) {
                isComplete = true;
            }
        }
        Post post = postRepository.findDetailPost(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));
        log.info("게시물 ID '{}' 조회 성공", postId);
        post.increaseViews();
        return ok(new PostResponseDto(post, isPined, isComplete));
    }


    @Transactional
    public ApiResponse<?> updatePost(Long postId, PostRequestDto postRequestDto, List<MultipartFile> images, List<String> deletedImageUrls, User user) {
        Post post = confirmPost(postId, user);
        post.update(postRequestDto);

        List<String> newImageUrlList = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            newImageUrlList = s3Service.uploads(images);
        }

        // 기존 이미지 URL 리스트 가져오기
        List<String> existingImageUrlList = post.getImageUrlList();

        // 기존 이미지 URL 리스트에서 삭제할 이미지 URL 제거
        if (deletedImageUrls != null && !deletedImageUrls.isEmpty()) {
            existingImageUrlList.removeAll(deletedImageUrls);
            s3Service.delete(deletedImageUrls); // 삭제할 이미지들 S3에서 삭제
        }

        // 새로운 이미지 URL 리스트와 기존 이미지 URL 리스트를 병합
        existingImageUrlList.addAll(newImageUrlList);

        // 병합된 이미지 URL 리스트를 사용하여 게시물 업데이트
        post.setImage(existingImageUrlList);

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

    private void deleteImage(Post post) {
        List<String> imageUrlList = post.getImageUrlList();
        if (StringUtils.hasText(String.valueOf(imageUrlList))) {
            s3Service.delete(imageUrlList);
        }
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(POST_NOT_EXIST));
    }

    private Post confirmPost(Long postId, User user) {
        Post post = findPost(postId);
        log.info("Confirming post access: postId={}, user={}, postUser={}, userRole={}",
                postId, user.getId(), post.getUser().getId(), user.getRole());
        if (!user.getId().equals(post.getUser().getId()) && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new InvalidConditionException(USER_NOT_MATCH);
        }

        log.info("Post access confirmed: postId={}, user={}", postId, user.getId());
        return post;
    }


    @Transactional
    public ApiResponse<?> reportPost(Long postId, PostReportRequestDto postReportRequestDto, User user) {
        Post post = findPost(postId);
        PostReport postReport = new PostReport(user, post, postReportRequestDto.getReport());
        postReportRepository.save(postReport);
        return ApiResponse.okWithMessage(POST_REPORT_SUCCESS);
    }
}


