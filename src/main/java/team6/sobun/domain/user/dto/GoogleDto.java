
package team6.sobun.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleDto {
    private String sub;
    private String name;
    private String given_name;
    private String picture;
    private String email;
    private boolean email_verified;
    private String locale;
}
