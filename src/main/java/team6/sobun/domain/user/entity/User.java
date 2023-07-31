// User.java
package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.user.dto.KakaoDto;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.global.utils.Timestamped;

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

    @Column
    private String profileImageUrl;

    @Column
    private String location;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;


    @Builder
    public User(String email, String nickname, String location, String password, UserRoleEnum role, String profileImageUrl) {
        this.email = email;
        this.nickname = nickname;
        this.location = location;
        this.password = password;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public void update(MypageRequestDto mypageRequestDto) {
        this.nickname = mypageRequestDto.getNickname();
    }

    public User(KakaoDto kakaoDto, String password,String location){
        this.email = kakaoDto.getEmail();
        this.nickname = kakaoDto.getNickname();
        this.password = password;
        this.location = location;
        //카카오 유저는 기본 USER
        this.role = UserRoleEnum.USER;
        this.profileImageUrl = profileImageUrl;
    }

    // 사용자의 역할 정보를 반환하는 메서드 추가
    public UserRoleEnum getRole() {
        return this.role;
    }

    public User kakaoIdUpdate(KakaoDto kakaoDto){
        return this;
    }
}
