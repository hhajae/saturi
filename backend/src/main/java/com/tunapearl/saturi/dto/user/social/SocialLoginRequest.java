package com.tunapearl.saturi.dto.user.social;

import com.tunapearl.saturi.dto.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SocialLoginRequest {
    @NotNull private UserType userType;
    @NotNull private String code;
}
