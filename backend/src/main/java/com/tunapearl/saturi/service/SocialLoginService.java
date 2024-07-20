package com.tunapearl.saturi.service;

import com.tunapearl.saturi.dto.SocialAuthResponse;
import com.tunapearl.saturi.dto.SocialUserResponse;
import com.tunapearl.saturi.dto.UserType;

public interface SocialLoginService {
    UserType getServiceName();
    SocialAuthResponse getAccessToken(String code);
    SocialUserResponse getUserInfo(String accessToken);
}
