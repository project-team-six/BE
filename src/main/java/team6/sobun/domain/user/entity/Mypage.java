package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.global.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mypage")
public class Mypage extends Timestamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "mypage_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

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

    @OneToMany
    private List<Post> userPosts = new ArrayList<>();

    @OneToMany
    private List<Post> pinedPosts = new ArrayList<>();


    @Builder
    public Mypage(User user,String email, String phoneNumber, String nickname, String password, String username, String profileImageUrl, UserRoleEnum role, List<Post> userPosts, List<Post> pinedPosts) {
        this.user = user;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.userPosts = userPosts;
        this.pinedPosts = pinedPosts;

    }
}
