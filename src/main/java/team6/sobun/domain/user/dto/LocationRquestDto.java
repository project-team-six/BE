package team6.sobun.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationRquestDto {
    @NotBlank(message = "필수 입력사항 입니다.")
    private String sido;

    @NotBlank(message = "필수 입력사항 입니다.")
    private String sigungu;

    @NotBlank(message = "필수 입력사항 입니다.")
    private String dong;

}
