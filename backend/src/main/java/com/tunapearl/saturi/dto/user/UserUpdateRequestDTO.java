package com.tunapearl.saturi.dto.user;

import com.tunapearl.saturi.domain.user.Gender;
import com.tunapearl.saturi.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.tunapearl.saturi.domain.user.Location;

@Getter
@AllArgsConstructor
public class UserUpdateRequestDTO {
    private Long userId;
    private String nickname;
    private Location location;
    private Gender gender;
    private Role role;
    //TODO 쿼카 수정 추가
}
