package team6.sobun.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupRequestDto {
    @Email(message = "잘못된 이메일 형식입니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Pattern(regexp = "^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]*$", message = "닉네임은 한글, 영문 대소문자, 숫자만 입력하여야 합니다.")
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @Pattern(regexp = "^(\\+?82|0)1[0-9]{1}[0-9]{3,4}[0-9]{4}$", message = "휴대전화 형식이 잘못 되었습니다.")
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;

    @Pattern(regexp = "^[A-Za-z가-힣]+$", message = "이름은 영문 또는 한글만 가능합니다.")
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;

    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$", message = "비밀번호는 영문 대소문자 중 1개 이상, 특수문자 1개 이상을 포함한 8~25자 여야 합니다.")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    private  String profileImageUrl;


    private boolean admin = false;
    private String adminToken = "";
}

