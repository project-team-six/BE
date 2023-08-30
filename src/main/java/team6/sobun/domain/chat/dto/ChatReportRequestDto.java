package team6.sobun.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatReportRequestDto {
    private ChatReportEnum report;
    private List<String> imageUrlList;

}
