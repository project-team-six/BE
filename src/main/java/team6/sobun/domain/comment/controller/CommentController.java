package team6.sobun.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.comment.dto.CommentRequestDto;
import team6.sobun.domain.comment.dto.CommentResponseDto;
import team6.sobun.domain.comment.entity.Comment;
import team6.sobun.domain.comment.service.CommentService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "댓글 관련 API", description = "댓글 등록 및 조회, 수정, 삭제")
@RestController
@RequestMapping("/post/{postId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "해당 게시글 댓글 전체 조회")
    @GetMapping
    public ApiResponse<?> searchComment(@PathVariable Long postId) {
        List<CommentResponseDto> commentResponseDtos = commentService.findCommentsResponseByPostId(postId);
        return ApiResponse.success(commentResponseDtos);
    }

    @Operation(summary = "댓글 작성")
    @PostMapping
    public ApiResponse<?> createComment(@PathVariable Long postId,
                                        @RequestBody CommentRequestDto commentRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return commentService.createComment(postId, commentRequestDto, userDetailsImpl.getUser());
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    public ApiResponse<?> updateComment(@PathVariable Long commentId,
                                        @RequestBody CommentRequestDto commentRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return commentService.updateComment(commentId, commentRequestDto, userDetailsImpl.getUser());
    }
    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ApiResponse<?> removeComment(@PathVariable Long commentId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return commentService.deleteComment(commentId, userDetailsImpl.getUser());
    }
}