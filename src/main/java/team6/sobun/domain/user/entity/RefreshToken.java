package team6.sobun.domain.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refresh", timeToLive = 7 * 24 * 60 * 60)
public class RefreshToken {
    @Id
    private String id; // email값을 id로 사용
    @Indexed // 해당 필드 값으로 데이터를 찾아올 수 있다
    private String token;
    private String refreshToken;

}