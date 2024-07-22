package com.tunapearl.saturi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("token")
@AllArgsConstructor
@Getter
@Setter
public class Token {

    @Id
    private Long user_id;//user_id로 할 것인지는 아직 결정안함
    private String refreshToken;

    @Override
    public String toString() {
        return "Token{" +
                "user_id=" + user_id +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
