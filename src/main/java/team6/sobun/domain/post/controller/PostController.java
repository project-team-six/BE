package team6.sobun.domain.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.dto.PostSearchCondition;
import team6.sobun.domain.post.service.PostService;
import team6.sobun.domain.user.dto.UserDetailResponseDto;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ApiResponse<?> searchPost(PostSearchCondition condition, Pageable pageable) {
        return postService.searchPost(condition, pageable);
    }


    @GetMapping("/{postId}")
    public ApiResponse<?> readOnePost(@PathVariable Long postId, HttpServletRequest req) {
        return postService.getSinglePost(postId, req);
    }

    @PostMapping
    public ApiResponse<?> createPost(@RequestPart(value = "data") PostRequestDto postRequestDto,
                                     @RequestPart(value = "file", required = false) List<MultipartFile> images,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.createPost(postRequestDto, images, userDetailsImpl.getUser());
    }

    @PutMapping("/{postId}")
    public ApiResponse<?> modifyPost(@PathVariable Long postId,
                                     @RequestPart(value = "data") PostRequestDto postRequestDto,
                                     @RequestPart(value = "file", required = false) List<MultipartFile> images,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.updatePost(postId, postRequestDto, images, userDetailsImpl.getUser());
    }

    @DeleteMapping ("/{postId}")
    public ApiResponse<?> removePost(@PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postService.deletePost(postId, userDetailsImpl.getUser());
    }


}