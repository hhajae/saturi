package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.Token;
import com.tunapearl.saturi.exception.UserExistException;
import com.tunapearl.saturi.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;

    public void saveRefreshToken(Token token) {

        Optional<Token> registeredUser = tokenRepository.findById(token.getUserId());
        if (registeredUser.isPresent()) {
            throw new UserExistException("[" + token.getUserId() + "] 등록된 사용자 입니다.");
        }

        tokenRepository.save(token);
    }

    public String getRefreshToken(Long id) throws Exception {

        Optional<Token> tokenOptional = tokenRepository.findById(id);

        if (tokenOptional.isEmpty()) {
            throw new Exception("Refresh token not found for id: " + id);
        }
        else{
            Token token = tokenOptional.get();
            log.info("refreshToken found for id: {}", token.getRefreshToken());
            return token.getRefreshToken();
        }
    }

    public void deleteRefreshToken(Long id){

        Optional<Token> registeredUser = tokenRepository.findById(id);
        if (registeredUser.isPresent()) {
            throw new UserExistException("[" + id + "] 등록된 사용자 입니다.");
        }
        tokenRepository.deleteById(id);
    }

}
