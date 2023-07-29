package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.user.kakao.dto.KakaoDto;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column
    private Long kakaoId;

    @Column(name = "kakao_email")
    private String kakaoEmail;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    @Column(unique = true)
    private String phonenumber;

    @Column(nullable = false)
    private String password;

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    public User(String email, String nickname, UserRoleEnum role) {
        this.email = email;
        this.phonenumber = phonenumber;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }
    public User(KakaoDto kakaoDto, String password, String profileImageUrl){
        this.email = kakaoDto.getEmail();
        this.nickname = kakaoDto.getUsername();
        this.password = password;
        //카카오 유저는 기본 USER
        this.role = UserRoleEnum.USER;
        this.profileImageUrl = profileImageUrl;
    }
    @Builder
    public User(Long kakaoId, String kakaoProfileImg, String kakaoNickname,
                 String kakaoEmail, UserRoleEnum role) {

        this.kakaoId = kakaoId;
        this.profileImageUrl = kakaoProfileImg;
        this.nickname = kakaoNickname;
        this.kakaoEmail = kakaoEmail;
        this.role = role;
    }


    public User kakaoIdUpdate(KakaoDto kakaoDto){
        this.kakaoId = kakaoDto.getId();
        return this;
    }
}


