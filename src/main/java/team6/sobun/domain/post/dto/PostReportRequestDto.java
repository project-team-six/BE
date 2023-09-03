package team6.sobun.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.post.entity.PostReportEnum;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostReportRequestDto {

    @NotBlank(message = "신고내용을 입력하세요.")
    private PostReportEnum report;
    private List<String> imageUrlList;
}
