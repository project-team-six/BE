package team6.sobun.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatReportRequestDto {

    @NotBlank(message = "신고내용을 입력하세요.")
    private ChatReportEnum report;

}
