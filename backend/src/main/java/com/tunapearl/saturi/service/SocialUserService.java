package com.tunapearl.saturi.service;

import com.tunapearl.saturi.dto.user.UserLoginRequestDTO;
import com.tunapearl.saturi.dto.user.UserType;
import com.tunapearl.saturi.dto.user.social.*;
import com.tunapearl.saturi.exception.InvalidTokenException;
import com.tunapearl.saturi.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SocialUserService {

    /* 일반 로그인, 카카오 로그인, 네이버 로그인 Service 리스트*/
    private final List<LoginService> loginServices;
    private final UserRepository userRepository;

    /* 소셜 로그인용 필드 */
    private RestTemplate restTemplate = new RestTemplate();
    Base64.Decoder decoder = Base64.getDecoder();
    BasicJsonParser parser = new BasicJsonParser();
    StringTokenizer st;

    public LoginResponse doSocialLogin(UserLoginRequestDTO request) {

        // 유저가 로그인 한 방식 식별
        LoginService loginService = getLoginService(request.getUserType());

        // 유저 토큰 정보 얻기
        SocialAuthResponse authResponse = loginService.getAccessToken(request.getCode());

        // 토큰 유효성 검사
        try {
            ((KakaoLoginServiceImpl) loginService).checkTokenValidity(authResponse.getAccessToken());
        }
        catch (InvalidTokenException e) {
            e.getStackTrace();
        }
        catch (RuntimeException e) {
            e.getStackTrace();
        }

        // 유저 개인 정보 얻기
        SocialUserResponse userResponse = loginService.getUserInfo(authResponse.getAccessToken());
        log.info("User info: {}", userResponse);

        // TODO: 실제 유저 정보로 바꿔야함
        return LoginResponse.builder().id(1L).build();
    }

    /* 여러 로그인 서비스 API 중에 어떤 서비스인지 확인하는 메서드 */
    private LoginService getLoginService(UserType type){
        for(LoginService loginService: loginServices){
            if(loginService.getServiceName().equals(type)){
                log.info("Selected login service: {}", loginService.getServiceName());
                return loginService;
            }
        }

        //TODO: 일반 로그인 서비스가 구현됐을 때 return 값 변경
        return null;
    }

    private void checkTokenPayload(String idToken) {
        //토큰 payload 분리
        st = new StringTokenizer(idToken, "."); st.nextToken();
        String decodedPayload = new String(decoder.decode(st.nextToken()));

        //맵으로 저장
        Map<String, Object> payloadMap = parser.parseMap(decodedPayload);

        // 페이로드 검사
        // 1. iss 값이 "https://kauth.kakao.com"인지 확인
        // 2. aud 값이 "your_kakao_app_id"인지 확인
        try {
            Jwts.parserBuilder()
                    .requireIssuer(payloadMap.get("iss").toString())
                    .requireAudience(payloadMap.get("aud").toString())
                    .build()
                    .parseClaimsJws(idToken);
        }
        catch (ExpiredJwtException e) {
            log.error("Token has expired", e);
        } catch (SignatureException e) {
            log.error("Invalid token signature", e);
        } catch (Exception e) {
            log.error("Token validation failed", e);
        }
    }
    
    private void checkTokenSignature(String idToken) {
        //토큰 payload 분리
        st = new StringTokenizer(idToken, ".");
        String decodedHeader = new String(decoder.decode(st.nextToken()));

        //맵으로 저장
        Map<String, Object> headerMap = parser.parseMap(decodedHeader);
        
        // 헤더에서 key id 가져오기
        String kid = headerMap.get("kid").toString();

        // 카카오 인증 서버에서 공개키 리스트 가져오기
        PublicKeyResponse keyResponse
                = restTemplate.getForObject("https://kauth.kakao.com/.well-known/jwks.json", PublicKeyResponse.class);

        // 공개키 목록에서 kid에 해당하는 공개키 확인
        PublicKeyResponse.Key keys = keyResponse.getKeys().stream()
                .filter(o -> o.getKid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Key not found"));

        // 공개키로 서명 검증
        try {
            Jwts.parserBuilder()
                   .setSigningKey(getRSAPublicKey(keys.getN(), keys.getE()))
                   .build()
                   .parseClaimsJws(idToken);
        }
        catch (SignatureException e) {
            log.error("Invalid token signature", e);
        }
        catch (Exception e) {
            log.error("Token validation failed", e);
        }
    }


    private Key getRSAPublicKey(String modules, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        BigInteger n = new BigInteger(1, decoder.decode(modules));
        BigInteger e = new BigInteger(1, decoder.decode(exponent));

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        return keyFactory.generatePublic(keySpec);
    }
}
