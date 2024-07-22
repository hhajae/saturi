package com.tunapearl.saturi.service;

import com.tunapearl.saturi.dto.SocialAuthResponse;
import com.tunapearl.saturi.dto.SocialUserResponse;
import com.tunapearl.saturi.dto.UserType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements SocialLoginService {
    private final RestTemplate restTemplate;
    private MultiValueMap<String, String> body;

    @Value("${social.client.kakao.grant-type}")
    private String grantType;
    @Value("${social.client.kakao.client-id}")
    private String clientId;
    @Value("${social.client.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${social.client.kakao.client-secret}")
    private String clientSecret;
    private String code;

    @PostConstruct
    public void init() {
        body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("client_secret", clientSecret);
    }

    @Override
    public UserType getServiceName() {
        return UserType.KAKAO;
    }

    @Override
    public SocialAuthResponse getAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        //헤더 셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        //바디 셋팅(code 제외 나머지는 고정값)
        body.add("code", code);

        //요청
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<SocialAuthResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                SocialAuthResponse.class
        );
        log.info("Kakao Access Token Headers: {}", response.getHeaders());
        log.info("Kakao Access Token Response: {}", response.getBody());
        body.remove("code");
        return response.getBody();
    }

    public void checkTokenValidity(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/access_token_info";

        //헤더 셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

    }

    @Override
    public SocialUserResponse getUserInfo(String accessToken) {
        return null;
    }
}
