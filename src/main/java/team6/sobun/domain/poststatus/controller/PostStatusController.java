package team6.sobun.domain.poststatus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import team6.sobun.domain.post.dto.PostResponseDto;
import team6.sobun.domain.poststatus.service.PostStatusService;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.security.UserDetailsImpl;

@Controller
@RequestMapping("/post/{postId}/poststatus")
@RequiredArgsConstructor
public class PostStatusController {

    private final PostStatusService postStatusService;

    @PostMapping
    public ResponseEntity<?> updatePostStatus(@PathVariable Long postId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        User user = userDetailsImpl.getUser();
        return ResponseEntity.ok(postStatusService.updatePostStatus(postId, user));
    }
}
