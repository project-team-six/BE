package team6.sobun.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.post.dto.PostReportRequestDto;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.service.PostService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@Tag(name = "게시글 관련 API", description = "게시글 작성 및 조회, 수정, 삭제")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 최신순으로 전체 조회")
    @GetMapping
    public ApiResponse<?> searchPost(PostSearchCondition condition, Pageable pageable) {
        return postService.searchPost(condition, pageable);
    }

    @Operation(summary = "게시글 상세조회")
    @GetMapping("/{postId}")
    public ApiResponse<?> readOnePost(@PathVariable Long postId, HttpServletRequest req) {
        return postService.getSinglePost(postId, req);
    }

    @Operation(summary = "게시글 작성")
    @PostMapping
    public ApiResponse<?> createPost(@RequestPart(value = "data") PostRequestDto postRequestDto,
                                     @RequestPart(value = "file", required = false) List<MultipartFile> images,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.createPost(postRequestDto, images, userDetailsImpl.getUser());
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{postId}")
    public ApiResponse<?> modifyPost(@PathVariable Long postId,
                                     @RequestPart(value = "data", required = false) PostRequestDto postRequestDto,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.updatePost(postId, postRequestDto, userDetailsImpl.getUser());
    }

    @Operation(summary = "게시글 이미지 추가")
    @PutMapping("/append/{postId}")
    public ApiResponse<?> postImageAppend(@PathVariable Long postId,
                                     @RequestPart(value = "file", required = false) List<MultipartFile> images,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.postImageAppend(postId, images, userDetailsImpl.getUser());
    }

    @Operation(summary = "게시글 이미지 삭제")
    @PutMapping("/delete/{postId}/{imageIndex}")
    public ApiResponse<?> PostImageDelete(@PathVariable Long postId,
                                     @PathVariable int imageIndex,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.postImageDelete(postId, imageIndex, userDetailsImpl.getUser());
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping ("/{postId}")
    public ApiResponse<?> removePost(@PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.deletePost(postId, userDetailsImpl.getUser());
    }

    @Operation(summary = "게시글 신고")
    @PostMapping("/report/{postId}")
    public ApiResponse<?> reportPost(@PathVariable Long postId,
                                     @RequestBody PostReportRequestDto postReportRequestDto,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.reportPost(postId, postReportRequestDto, userDetailsImpl.getUser());
    }
}