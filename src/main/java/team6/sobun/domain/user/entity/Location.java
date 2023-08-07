package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Location {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="loaction_id")
    private Long id;

    @Column(nullable = false)
    private String sido;

    @Column(nullable = false)
    private String sigungu;

    @Column(nullable = false)
    private String bname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Location(String sido, String sigungu, String bname, User user) {
        this.sido = sido;
        this.sigungu = sigungu;
        this.bname = bname;
        this.user = user;
    }

    public String myAddress(String sido, String sigungu, String bname) {
        return sido+ " " + sigungu + " " + bname;
    }

}
