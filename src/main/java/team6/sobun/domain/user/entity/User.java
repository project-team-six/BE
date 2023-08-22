// User.java
package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.user.dto.location.LocationRquestDto;
import team6.sobun.domain.user.dto.mypage.MypageRequestDto;
import team6.sobun.domain.user.dto.social.KakaoDto;
import team6.sobun.global.utils.Timestamped;

import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String username;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column
    private String profileImageUrl;

    private double mannerTemperature = 36.5;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "is_verified")
    private boolean verified = false; // 이메일 인증 상태를 나타내는 필드

    @Column
    private String roomId;

    @Column
    private String sessionId;


    @Builder
    public User(String email, String phoneNumber, String nickname, String password, String username, String profileImageUrl, UserRoleEnum role) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }
    public User(KakaoDto kakaoDto, String password, String profileImageUrl){
        this.email = kakaoDto.getEmail();
        this.location = kakaoDto.getLocation();
        this.phoneNumber = kakaoDto.getPhoneNumber();
        this.nickname = kakaoDto.getNickname();
        this.username = kakaoDto.getUsername();
        this.password = password;
        this.role = UserRoleEnum.KAKAO;
        this.profileImageUrl = profileImageUrl;
    }


    public void update(MypageRequestDto mypageRequestDto) {
        this.nickname = mypageRequestDto.getNickname();
        this.phoneNumber = mypageRequestDto.getPhoneNumber();
    }


    // 이메일 인증 상태 설정
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // 이메일 인증 상태 가져오기
    public boolean isVerified() {
        return this.verified;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
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

    public void updateMannerTemperature(double newTemperature) {
        this.mannerTemperature = newTemperature;
    }


    public void setNickname(String name) {
    }

    public void updateLocation(LocationRquestDto locationRquestDto, User user) {
        this.location = new Location(locationRquestDto.getSido(), locationRquestDto.getSigungu(), locationRquestDto.getDong(),user);
    }

    public void setLocation(Location defaultLocation) {
        this.location = defaultLocation;
    }


    public void setRole(UserRoleEnum userRoleEnum) {
        this.role = userRoleEnum;
    }
}

