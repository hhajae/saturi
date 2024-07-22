package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.user.Role;
import com.tunapearl.saturi.domain.user.UserEntity;
import com.tunapearl.saturi.dto.user.UserLoginRequestDTO;
import com.tunapearl.saturi.dto.user.UserLoginResponseDTO;
import com.tunapearl.saturi.dto.user.UserType;
import com.tunapearl.saturi.dto.user.social.*;
import com.tunapearl.saturi.exception.InvalidTokenException;
import com.tunapearl.saturi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SocialUserService {

    /* 카카오 로그인, 네이버 로그인 Service 리스트*/
    private final List<SocialLoginService> loginServices;
    private final UserRepository userRepository;

    public UserLoginResponseDTO doSocialLogin(UserLoginRequestDTO request) {

        // 유저가 로그인 한 방식 식별
        SocialLoginService loginService = getLoginService(request.getUserType());

        // 유저 토큰 정보 얻기
        SocialAuthResponse authResponse = loginService.getAccessToken(request.getCode());
        if(loginService instanceof NaverLoginServiceImpl){
            log.info("Naver login request Here");
            return UserLoginResponseDTO.builder().build();
        }

        // 토큰 유효성 검사
        try {
            loginService.checkTokenValidity(authResponse.getAccessToken());
        }
        catch (InvalidTokenException e) {
            //TODO: 토큰 갱신 필요(refresh)
            e.getStackTrace();
        }
        catch (RuntimeException e) {
            e.getStackTrace();
        }

        // 유저 개인 정보 얻기
        SocialUserResponse userResponse = loginService.getUserInfo(authResponse.getAccessToken());

        // 기존재 하던 계정인지 검사
        Optional<List<UserEntity>> user = userRepository.findByEmail(userResponse.getEmail());
        
        // 없던 계정이면 회원가입
        if(user.isEmpty()){
            UserEntity userEntity = createNewUser(userResponse);
            userRepository.saveUser(userEntity);
            user = Optional.of(List.of(userEntity));
        }

        //TODO: 회원 엔티티로 JWT 생성
        //user.get().get(0);

        // TODO: 생성된 JWT 토큰 반환
        return UserLoginResponseDTO.builder().build();
    }

    /* 여러 로그인 서비스 API 중에 어떤 서비스인지 확인하는 메서드 */
    private SocialLoginService getLoginService(UserType type){
        for(SocialLoginService loginService: loginServices){
            if(loginService.getServiceName().equals(type)){
                log.info("Selected login service: {}", loginService.getServiceName());
                return loginService;
            }
        }

        throw new RuntimeException("The wrong approach: No selceted Service");
    }

    private UserEntity createNewUser(SocialUserResponse socialUserResponse){
        UserEntity user = new UserEntity();
        user.setEmail(socialUserResponse.getEmail());
        user.setPassword(null);
        user.setNickname(socialUserResponse.getNickname());
        user.setGender(socialUserResponse.getGender());
        user.setAgeRange(socialUserResponse.getAgeRange());
        user.setRegDate(LocalDateTime.now());
        user.setExp(0L);
        user.setRole(Role.BASIC);
        return user;
    }
}
