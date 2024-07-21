package com.tunapearl.saturi.controller;


import com.tunapearl.saturi.dto.user.*;
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
    public ResponseEntity<UserRegisterResponseDTO> userRegister(@RequestBody @Valid UserRegisterRequestDTO request) {
        //TODO log 남기기
        return ResponseEntity.created(URI.create("/auth")).body(userService.registerUser(request));
    }

    /**
     * 일반회원 로그인
     */
    @PostMapping("/auth/login")
    public ResponseEntity<UserLoginResponseDTO> userLogin(@RequestBody @Valid UserLoginRequestDTO request) {
        //TODO log 남기기
        return ResponseEntity.created(URI.create("/auth/login")).body(userService.loginUser(request));
    }

    /**
     * 회원 수정
     */
    @PutMapping("/auth")
    public ResponseEntity<UserUpdateResponseDTO> userUpdate(@RequestBody @Valid UserUpdateRequestDTO request) {
        //TODO log 남기기
        return ResponseEntity.created(URI.create("/auth")).body(userService.updateUser(request));
    }

    /**
     * 회원 비밀번호 변경
     */
    @PutMapping("/auth/password-update")
    public ResponseEntity<UserPasswordUpdateResponseDTO> userUpdatePassword(@RequestBody @Valid UserPasswordUpdateRequestDTO request) {
        //TODO log 남기기
        return ResponseEntity.created(URI.create("/auth/password-update")).body(userService.updateUserPassword(request));
    }

    /**
     * 회원 삭제(DB 삭제 x, 회원탈퇴 상태로 변경)
     */
    @DeleteMapping("/auth/{userId}")
    public ResponseEntity<UserDeleteResponseDTO> userDelete(@PathVariable("userId") Long userId) {
        //TODO log 남기기
        return ResponseEntity.created(URI.create("/auth")).body(userService.deleteUser(userId));
    }

    @PostMapping("/auth/email-valid")
    public ResponseEntity<String> mailSend(@RequestBody @Valid EmailRequestDTO request) {
        try {
            return ResponseEntity.created(URI.create("/auth/email-valid")).body(userService.joinEmail(request.getEmail()));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/auth/email-valid-code")
    public ResponseEntity<String> AuthCheck(@RequestBody @Valid EmailCheckDTO request){
        Boolean Checked = userService.CheckAuthNum(request.getEmail(),request.getAuthNum());
        if(Checked) {
            return ResponseEntity.created(URI.create("/auth/email-valid-code")).body("ok");
        }
        else{
            throw new NullPointerException("error!");
        }
    }
}
