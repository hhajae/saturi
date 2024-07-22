package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.user.AgeRange;
import com.tunapearl.saturi.dto.user.UserType;
import com.tunapearl.saturi.dto.user.social.SocialAuthResponse;
import com.tunapearl.saturi.dto.user.social.SocialUserResponse;
import com.tunapearl.saturi.exception.InvalidTokenException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverLoginServiceImpl implements SocialLoginService {
    private final RestTemplate restTemplate;
    private MultiValueMap<String, String> body;

    @Value("${social.client.naver.grant-type-read}")
    private String grantType;
    @Value("${social.client.naver.client-id}")
    private String clientId;
    @Value("${social.client.naver.redirect-uri}")
    private String redirectUri;
    @Value("${social.client.naver.client-secret}")
    private String clientSecret;
    private String code;

    @PostConstruct
    public void init() {
        body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
    }

    @Override
    public UserType getServiceName() {
        return UserType.NAVER;
    }

    @Override
    public SocialAuthResponse getAccessToken(String code) {

        //쿼리 파라미터 + URI 설정
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token");
        for(String key: body.keySet()){
            builder.queryParam(key, body.get(key));
        }
        URI uri = builder.queryParam("code", code).build().toUri();

        //헤더, 바디 설정(할거 없음 그냥 형식상)
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        //요청
        ResponseEntity<SocialAuthResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                SocialAuthResponse.class
        );
        
        //로깅
        log.info("Naver Access Token Headers: {}", response.getHeaders());
        log.info("Naver Access Token Response: {}", response.getBody());
        return response.getBody();
    }

    @Override
    public void checkTokenValidity(String accessToken) throws InvalidTokenException, RuntimeException {

    }

    @Override
    public SocialUserResponse getUserInfo(String accessToken) {
        return null;
    }
}
