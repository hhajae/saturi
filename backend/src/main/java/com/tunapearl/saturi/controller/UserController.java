package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.domain.user.UserEntity;
import com.tunapearl.saturi.dto.user.*;
import com.tunapearl.saturi.service.SocialUserService;
import com.tunapearl.saturi.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;

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
        log.info("Received normal user login request for {}", request.getEmail());
        return ResponseEntity.ok().body(userService.loginUser(request));
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
    public ResponseEntity<String> emailAuthCheck(@RequestBody @Valid EmailCheckDTO request){
        //FIXME exception 수정 필요, 반환 타입 수정 필요
        log.info("Received normal user email auth check request for {}", request.getEmail());
        if(userService.checkAuthNum(request.getEmail(),request.getAuthNum())) {
            return ResponseEntity.ok().body("ok");
        }
        else{
            throw new NullPointerException("error!");
        }
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final SocialUserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> doSocialLogin(@RequestBody @Valid SocialLoginRequest request) {
        // TODO: Implement social login logic
        log.info("Received social login request for {}", request.getCode());
        return ResponseEntity.created(URI.create("/auth/login")).body(userService.doSocialLogin(request));
    }

    @GetMapping("/temp")
    public ResponseEntity<String> doSocialLogin() {
        String[] propertyKeys = {"kakao_account.profile", "kakao_account.name", "kakao_account.email", "kakao_account.age_range", "kakao_account.birthday", "kakao_account.gender"};
        // propertyKeys 배열 형식의 스트링으로 변환
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < propertyKeys.length; i++) {
            sb.append("\"" + propertyKeys[i] + "\"");
            if (i < propertyKeys.length - 1) {
                sb.append(",");
            } else {
                sb.append("]");
            }
        }
        return ResponseEntity.ok(sb.toString());
    }
}
