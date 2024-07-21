package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.dto.LoginResponse;
import com.tunapearl.saturi.dto.SocialLoginRequest;
import com.tunapearl.saturi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> doSocialLogin(@RequestBody @Valid SocialLoginRequest request) {
        // TODO: Implement social login logic
        log.info("Received social login request for {}", request.getCode());
        return ResponseEntity.created(URI.create("/auth/login")).body(userService.doSocialLogin(request));
    }

    @GetMapping("kakao-login")
    public void login(){
        log.info("Redirect to Kakao login");
    }



}
