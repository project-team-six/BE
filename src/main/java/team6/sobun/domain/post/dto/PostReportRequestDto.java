package team6.sobun.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.post.entity.PostReportEnum;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostReportRequestDto {
    private PostReportEnum report;
    private List<String> imageUrlList;
}
