package team6.sobun.domain.pin.controller;

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


@Controller
@RequestMapping("/api/post/{postId}/like")
@RequiredArgsConstructor
public class PinController {

    private final PinService pinService;

    @PostMapping
    public ResponseEntity<?> updateLike(@PathVariable Long postId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        User user = userDetailsImpl.getUser();
        return ResponseEntity.ok(pinService.updatePin(postId, user));
    }
}
