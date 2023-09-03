package team6.sobun.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.comment.entity.CommentReportEnum;

import java.util.List;

@Getter
@NoArgsConstructor
public class CommentReportRequestDto {

    @NotBlank(message = "신고내용을 입력하세요.")
    private CommentReportEnum report;
    private List<String> imageUrlList;
}
