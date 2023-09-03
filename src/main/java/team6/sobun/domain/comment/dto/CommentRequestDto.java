package team6.sobun.domain.comment.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @Pattern(regexp = ".{1,50}", message = "댓글은 1자에서 50자까지만 입력할 수 있습니다.")
    private String content;
}
