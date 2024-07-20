package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.UserEntity;
import com.tunapearl.saturi.dto.*;
import com.tunapearl.saturi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    /* 일반 로그인, 카카오 로그인, 네이버 로그인 Service 리스트*/
    private final List<SocialLoginService> loginServices;
    private final UserRepository userRepository;

    public LoginResponse doSocialLogin(SocialLoginRequest request) {

        // 유저가 로그인 한 방식 식별
        SocialLoginService loginService = getLoginService(request.getUserType());

        // 유저 토큰 정보 얻기
        SocialAuthResponse authResponse = loginService.getAccessToken(request.getCode());

        // 유저 개인 정보 얻기
        SocialUserResponse userResponse = loginService.getUserInfo(authResponse.getAccessToken());
        log.info("User info: {}", userResponse);

        // DB에 저장되지 않은 유저라면 회원가입
        Optional<UserEntity> user = userRepository.findByUserId(userResponse.getUserId());
        if(user.isEmpty()) {
            //TODO: 회원가입 or 회원이 아니다. 처리
        }

        // TODO: 실제 유저 정보로 바꿔야함
        return LoginResponse.builder().id(1L).build();
    }

    /* 여러 로그인 서비스 API 중에 어떤 서비스인지 확인하는 메서드 */
    private SocialLoginService getLoginService(UserType type){
        for(SocialLoginService loginService: loginServices){
            if(loginService.getServiceName().equals(type)){
                log.info("Selected login service: {}", loginService.getServiceName());
                return loginService;
            }
        }

        //TODO: 일반 로그인 서비스가 구현됐을 때 return 값 변경
        return null;
    }
}
