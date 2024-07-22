package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.dto.user.social.LoginResponse;
import com.tunapearl.saturi.dto.user.social.SocialLoginRequest;
import com.tunapearl.saturi.service.SocialUserService;
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
