package team6.sobun.domain.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.comment.dto.CommentReportRequestDto;
import team6.sobun.domain.comment.dto.CommentRequestDto;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.comment.entity.Comment;
import team6.sobun.domain.comment.entity.CommentReport;
import team6.sobun.domain.comment.repository.CommentReportRepository;
import team6.sobun.domain.comment.repository.CommentRepository;
import team6.sobun.domain.notification.service.NotificationService;
import team6.sobun.domain.notification.util.AlarmType;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.util.List;
import java.util.stream.Collectors;

import static team6.sobun.global.stringCode.ErrorCodeEnum.USER_NOT_MATCH;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final CommentReportRepository commentReportRepository;
    private final S3Service s3Service;


    public Post findPostWithComments(Long postId) {
        return postRepository.findPostWithComments(postId);
    }

    public List<CommentResponseDto> findCommentsResponseByPostId(Long postId) {
        Post post = postRepository.findPostWithComments(postId);
        List<Comment> comments = post.getCommentList();
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }


    public ApiResponse<?> createComment(Long postId, CommentRequestDto commentRequestDto, User user) {
        log.info("'{}' 사용자가 '{}' 게시물에 댓글을 작성하였습니다.", user.getNickname(), postId);
        Comment comment = new Comment(commentRequestDto, user);
        Post post = findPost(postId);
        post.addComment(comment);
        commentRepository.save(comment);
        User Writer = findPostUser(postId);
        notificationService.send(Writer, AlarmType.eventCreateComment, "새로운 댓글이 달렸어요.", user.getUsername(), user.getNickname(), user.getProfileImageUrl(), "/feed/" + post.getId());
        return ResponseUtils.okWithMessage(SuccessCodeEnum.COMMENT_CREATE_SUCCESS);
    }

    public ApiResponse<?> updateComment(Long commentId, CommentRequestDto commentRequestDto, User user) {
        Comment comment = findComment(commentId);
        checkUsername(comment, user);
        comment.update(commentRequestDto);
        log.info("'{}' 사용자가 '{}' 댓글을 수정하였습니다.", user.getNickname(), commentId);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.COMMENT_UPDATE_SUCCESS);
    }

    public ApiResponse<?> deleteComment(Long commentId, User user) {
        Comment comment = findComment(commentId);
        checkUsername(comment, user);
        Post post = comment.getPost();
        post.getCommentList().remove(comment);
        commentRepository.delete(comment);
        log.info("'{}' 사용자가 '{}' 댓글을 삭제하였습니다.", user.getNickname(), commentId);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.COMMENT_DELETE_SUCCESS);
    }

    @Transactional(readOnly = true)
    public Comment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new InvalidConditionException(ErrorCodeEnum.COMMENT_NOT_EXIST));
    }

    @Transactional(readOnly = true)
    public Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new InvalidConditionException(ErrorCodeEnum.POST_NOT_EXIST));
    }

    private void checkUsername(Comment comment, User user) {
        if (!user.getId().equals(comment.getUser().getId()) && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new InvalidConditionException(USER_NOT_MATCH);
        }
    }
    private User findPostUser(Long postId) {
        Post post = findPost(postId);
        return post.getUser();
    }
    @Transactional
    public ApiResponse<?> reportComment(Long commentId, List<MultipartFile> images, CommentReportRequestDto commentReportRequestDto, User user) {
        List<String> imageUrlList = s3Service.uploads(images);
        Comment comment = commentRepository.findById(commentId).orElseThrow(()
                -> new IllegalArgumentException("존재하지 않는 댓글 입니다."));
        CommentReport commentReport = new CommentReport(comment, imageUrlList,commentReportRequestDto.getReport(),comment.getPost().getUser().getId(), user);
        comment.increaseReportCount();
        commentReportRepository.save(commentReport);
        return ApiResponse.okWithMessage(SuccessCodeEnum.COMMENT_REPORT_SUCCESS);
    }
}
