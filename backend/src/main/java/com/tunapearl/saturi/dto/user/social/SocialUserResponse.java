package com.tunapearl.saturi.dto.user.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SocialUserResponse {
    private String nickname;
    private String email;
    private String gender;
    private String ageRange;
}
