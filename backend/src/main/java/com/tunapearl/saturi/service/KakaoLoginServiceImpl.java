package com.tunapearl.saturi.service;

import com.tunapearl.saturi.dto.user.social.SocialAuthResponse;
import com.tunapearl.saturi.dto.user.social.SocialUserResponse;
import com.tunapearl.saturi.dto.UserType;
import com.tunapearl.saturi.exception.InvalidTokenException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements LoginService {
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

    public void checkTokenValidity(String accessToken) throws InvalidTokenException, RuntimeException{
        String url = "https://kapi.kakao.com/v1/user/access_token_info";

        //헤더 셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        //토큰 검증 요청
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if(response.getStatusCode() == HttpStatus.OK) {
            log.info("Kakao Access Token Validity OK: {}", response.getBody());
            return;
        }
        else{
            GsonJsonParser parser = new GsonJsonParser();
            Map<String, Object> map = parser.parseMap(response.getBody());
            int code = (int) map.get("code");
            String msg = (String) map.get("msg");

            if(code == -401){
                throw new InvalidTokenException(msg, accessToken);
            }
            else{
                throw new RuntimeException(msg);
            }
        }
    }

    @Override
    public SocialUserResponse getUserInfo(String accessToken) {
        String url = "\thttps://kapi.kakao.com/v2/user/me";

        //헤더 셋팅
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return null;
    }
}
