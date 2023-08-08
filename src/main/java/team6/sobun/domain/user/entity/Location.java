package team6.sobun.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.domain.user.dto.LocationRquestDto;

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
    private String dong;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Location(String sido, String sigungu, String dong, User user) {
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
        this.user = user;
    }

    public String myAddress(String sido, String sigungu, String dong) {
        return sido+ " " + sigungu + " " + dong;
    }

    public void update(LocationRquestDto locationRquestDto) {
        this.sido = locationRquestDto.getSido();
        this.sigungu = locationRquestDto.getSigungu();
        this.dong = locationRquestDto.getDong();
    }
}
