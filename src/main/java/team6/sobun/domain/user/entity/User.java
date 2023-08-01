// User.java
package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.pin.entity.Pin;
import team6.sobun.domain.user.dto.KakaoDto;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

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
    private String username;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    private String location;

    private double mannerTemperature = 36.5;

    @OneToMany(mappedBy = "user")
    private List<Pin> pins = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;


    @Builder
    public User(String email, String username,String nickname, String location, String password, UserRoleEnum role, String profileImageUrl) {
        this.email = email;
        this.username = username;
        this.nickname = nickname;
        this.location = location;
        this.password = password;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String email, String location, String nickname, String password, UserRoleEnum role) {
        super();
    }

    public void updateMannerTemperature(double newTemperature) {
        this.mannerTemperature = newTemperature;
    }

    public void update(MypageRequestDto mypageRequestDto) {
        this.nickname = mypageRequestDto.getNickname();
    }

    public User(KakaoDto kakaoDto,String password, String profileImageUrl){
        this.email = kakaoDto.getEmail();
        this.location = kakaoDto.getLocation();
        this.nickname = kakaoDto.getNickname();
        this.username = kakaoDto.getUsername();
        this.password = password;
        //카카오 유저는 기본 USER
        this.role = UserRoleEnum.USER;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String email, String nickname,String username, String password, String location, String profileImageUrl) {
        this.email = email;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.location = location;
        this.role = UserRoleEnum.USER;
        this.profileImageUrl = profileImageUrl;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImageUrl(String profileImageUrl) {
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
