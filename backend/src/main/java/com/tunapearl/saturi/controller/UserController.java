package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.domain.*;
import com.tunapearl.saturi.dto.user.*;
import com.tunapearl.saturi.exception.*;
import com.tunapearl.saturi.service.*;
import com.tunapearl.saturi.utils.*;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;
    private final SocialUserService socialUserService;
    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    /**
     * 일반회원 회원가입
     */
    @PostMapping("/auth")
    public ResponseEntity<UserMsgResponseDTO> userRegister(@RequestBody @Valid UserRegisterRequestDTO request) {
        log.info("Received normal user register request for {}", request.getEmail());
        return ResponseEntity.created(URI.create("/auth")).body(userService.registerUser(request));
    }

    /**
     * 이메일 중복확인
     */
    @GetMapping("/auth/email-dupcheck")
    public ResponseEntity<UserMsgResponseDTO> userEmailDuplicateCheck(@RequestBody @Valid EmailRequestDTO request) {
        log.info("Received email duplicate check request for {}", request.getEmail());
        userService.validateDuplicateUserEmail(request.getEmail());
        return ResponseEntity.ok().body(new UserMsgResponseDTO("ok"));
    }

    /**
     * 닉네임 중복확인
     */
    @GetMapping("/auth/nickname-dupcheck")
    public ResponseEntity<UserMsgResponseDTO> userNicknameDuplicateCheck(@RequestBody @Valid UserNicknameRequestDTO request) {
        log.info("Received nickname duplicate check request for {}", request.getNickname());
        userService.validateDuplicateUserNickname(request.getNickname());
        return ResponseEntity.ok().body(new UserMsgResponseDTO("ok"));
    }

    /**
     * 일반회원 로그인
     */
    @PostMapping("/auth/login")
    public ResponseEntity<UserLoginResponseDTO> userLogin(@RequestBody @Valid UserLoginRequestDTO request) {
        //TODO: 일반, 소셜 로그인 분리하기

        if(request.getUserType() == UserType.NORMAL){
            //TODO: JWT 서비스로 이동(login, , )
            // String accessToken= jwtUtil.createAccessToken(user.getUserId());
            // String refreshToken = jwtUtil.createRefreshToken(user.getUserId());
            log.info("Received normal user login request for {}", request.getEmail());
            return ResponseEntity.ok().body(userService.loginUser(request));
        }
        else{
            log.info("Received social login request for {}", request.getCode());
            return ResponseEntity.created(URI.create("/auth/login")).body(socialUserService.doSocialLogin(request));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String,Object>> logout(HttpServletRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;

        //TODO: 1.header에서 accessToken 받아오기
        //TODO: 2.accessToken으로 사용자 Id 가져오기 jwtUtil.getUserId이용
        //TODO: 3.tokenService.deleteRefreshToken(userId)로 refreshToken 삭제하기

        String accessToken = request.getHeader("access-token");

        try {

            tokenService.deleteRefreshToken(jwtUtil.getUserId(accessToken));

            status = HttpStatus.OK;
        } catch (Exception e) {
            log.error("로그아웃 실패 : {}", e);
            resultMap.put("message", e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (UnAuthorizedException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);

    }

    /**
     * 회원 프로필 조회
     */
    @GetMapping("/auth/profile")
    public ResponseEntity<UserInfoResponseDTO> userProfile(@RequestBody @Valid UserInfoRequestDTO request) {
        log.info("Received user profile info request for {}", request.getToken());
        //TODO 토큰 디코딩해서 id 빼기
        Long userId = userService.getUserIdByToken();
        return ResponseEntity.ok().body(userService.getUserProfile(userId));
    }

    /**
     * 회원 수정
     */
    @PutMapping("/auth")
    public ResponseEntity<UserMsgResponseDTO> userUpdate(@RequestBody @Valid UserUpdateRequestDTO request) {
        log.info("Received user update request for {}", request.getUserId());
        return ResponseEntity.ok().body(userService.updateUser(request));
    }

    /**
     * 회원 비밀번호 변경
     */
    @PutMapping("/auth/password-update")
    public ResponseEntity<UserPasswordUpdateResponseDTO> userUpdatePassword(@RequestBody @Valid UserPasswordUpdateRequestDTO request) {
        log.info("Received normal user password update request for {}", request.getUserId());
        return ResponseEntity.ok().body(userService.updateUserPassword(request));
    }

    /**
     * 회원 삭제(DB 삭제 x, 회원탈퇴 상태로 변경)
     */
    @DeleteMapping("/auth")
    public ResponseEntity<UserMsgResponseDTO> userDelete(@RequestBody @Valid UserDeleteRequestDTO request) {
        log.info("Received normal user delete request for {}", request.getUserId());
        return ResponseEntity.ok().body(userService.deleteUser(request.getUserId()));
    }


    /**
     * 이메일 인증 메일 전송
     */
    @PostMapping("/auth/email-valid")
    public ResponseEntity<String> emailSend(@RequestBody @Valid EmailRequestDTO request) {
        //FIXME exception 수정 필요, 반환 타입 수정 필요
        log.info("Received normal user email send request for {}", request.getEmail());
        try {
            return ResponseEntity.ok().body(userService.setEmailSend(request.getEmail()));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 이메일 인증번호 인증
     */
    @PostMapping("/auth/email-valid-code")
    public ResponseEntity<String> emailAuthCheck(@RequestBody @Valid EmailCheckDTO request) {
        //FIXME exception 수정 필요, 반환 타입 수정 필요
        log.info("Received normal user email auth check request for {}", request.getEmail());
        if (userService.checkAuthNum(request.getEmail(), request.getAuthNum())) {
            return ResponseEntity.ok().body("ok");
        } else {
            throw new NullPointerException("error!");
        }
    }

    //accessToken 의 유효성 검사
    @GetMapping("/token-check")
    public ResponseEntity<Map<String,Object>> checkToken(HttpServletRequest request) throws Exception{
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;

        if (jwtUtil.checkToken(request.getHeader("Authorization"))) {
            log.info("사용 가능한 토큰!!!");
            try {

                //TODO: 로그인 사용자 정보 resultMap에 저장

                status = HttpStatus.OK;
            } catch (Exception e) {
                log.error("정보조회 실패 : {}", e);
                resultMap.put("message", e.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else {
            log.error("사용 불가능 토큰!!!");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }

    //accessToken 재발급
    @PostMapping("/token-refresh")
    public ResponseEntity<?> refreshToken(@RequestBody User user, HttpServletRequest request)
            throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = HttpStatus.ACCEPTED;

        //TODO: 1.header에서 refreshToken 받아오기
        //TODO: refreshToken값 가져와서 db와 비교할 것

        String refreshToken = request.getHeader("refresh-token");
//        log.debug("refresh-token : {}, user : {}", token, user);
        if (jwtUtil.checkToken(refreshToken)) {
            if (refreshToken.equals(tokenService.getRefreshToken(user.getUserId()))) {
                String accessToken = jwtUtil.createAccessToken(user.getUserId());

//                log.debug("정상적으로 access token 재발급!!!");
                log.info("access-token : {}", accessToken);

                resultMap.put("access-token", accessToken);
                status = HttpStatus.CREATED;
            }
        } else {
            log.debug("refresh token도 사용 불가!!!!!!!");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<Map<String, Object>>(resultMap, status);
    }
}
