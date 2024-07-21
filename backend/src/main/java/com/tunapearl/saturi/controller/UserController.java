package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.dto.LoginResponse;
import com.tunapearl.saturi.dto.SocialLoginRequest;
import com.tunapearl.saturi.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> doSocialLogin(@RequestBody @Valid SocialLoginRequest request) {
        // TODO: Implement social login logic
        log.info("Received social login request for {}", request.getCode());
        return ResponseEntity.created(URI.create("/auth/login")).body(userService.doSocialLogin(request));
    }


}
