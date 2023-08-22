package team6.sobun.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import team6.sobun.domain.pin.service.PinService;
import team6.sobun.domain.user.entity.User;
import team6.sobun.global.security.UserDetailsImpl;

@Tag(name = "게시글 관심 등록 API", description = "한번 동작시 관심 등록, 한번더 누르면 등록 취소")
@Controller
@RequestMapping("/post/{postId}/pin")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;

    @Operation(summary = "게시글 관심 누르기")
    @PostMapping
    public ResponseEntity<?> updatePin(@PathVariable Long postId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        User user = userDetailsImpl.getUser();
        return ResponseEntity.ok(pinService.updatePin(postId, user));
    }
}
