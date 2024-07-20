package com.tunapearl.saturi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SocialLoginRequest {
    @NotNull private UserType userType;
    @NotNull private String code;
}
