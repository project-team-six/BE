package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.global.utils.Timestamped;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @Builder
    public User(String email, String nickname, String password, String location, UserRoleEnum role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.location = location;
        this.role = role;
    }

    @Builder
    public User(String nickname, String location, String password, UserRoleEnum role) {
        this.nickname = nickname;
        this.location = location;
        this.password = password;
        this.role = role;
    }

    public void update(MypageRequestDto mypageRequestDto) {
        this.nickname = mypageRequestDto.getNickname();
    }

    public void setRoles(String role) {

    }
}

