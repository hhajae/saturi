package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.domain.Token;
import com.tunapearl.saturi.domain.User;
import com.tunapearl.saturi.exception.UnAuthorizedException;
import com.tunapearl.saturi.exception.UserExistException;
import com.tunapearl.saturi.service.TokenService;
import com.tunapearl.saturi.utils.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final TokenService tokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody User user) throws Exception, UserExistException {

        log.info("login user : {}", user.toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        HttpStatus status = HttpStatus.ACCEPTED;

        // 여기부터가 찐임
        //로그인 인증 이후
        String accessToken= jwtUtil.createAccessToken(user.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());
        log.info("access token : {}", accessToken);
        log.info("refresh token : {}", refreshToken);


        //TODO:발급받은 refresh token을 db(redis)에 저장
        tokenService.saveRefreshToken(new Token(user.getUserId(),refreshToken));

        //JSON 으로 token 전달.
        resultMap.put("access-token", accessToken);
        resultMap.put("refresh-token", refreshToken);

        status = HttpStatus.CREATED;

        return new ResponseEntity<Map<String, Object>>(resultMap, status);
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

    //TODO: 회원탈퇴에서도 refreshToken 삭제

}
