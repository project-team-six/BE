package team6.sobun.domain.user.kakao.dto;

import lombok.Data;

@Data
public class KakaoTokeninfo {
    Long id;
    Integer expires_in;
    Integer app_id;
}
