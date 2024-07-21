package com.tunapearl.saturi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SocialAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private String refreshTokenExpiresIn;
    private String idToken;
    private String tokenType;
    private String scope;
}
