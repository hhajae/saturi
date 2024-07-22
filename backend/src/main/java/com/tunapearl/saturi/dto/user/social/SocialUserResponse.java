package com.tunapearl.saturi.dto.user.social;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SocialUserResponse {
    private Long userId;
    private String email;
    private String gender;
    private String ageRange;
}
