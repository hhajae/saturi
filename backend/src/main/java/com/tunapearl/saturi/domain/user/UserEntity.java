package com.tunapearl.saturi.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter @Setter
@Table(name = "user")
public class UserEntity {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(STRING)
    private Location location;

    private String email;

    private String nickname;

    private String password;

    private String jwtToken;

    private String kToken;

    private LocalDateTime regDate;

    private Long exp;

    @Enumerated(STRING)
    private Gender gender;

    @Enumerated(STRING)
    private AgeRange ageRange;

    @Enumerated(STRING)
    private Role role;

    private LocalDateTime deletedDt;

    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "quokka_id")
    private QuokkaEntity quokka;

    private LocalDateTime returnDt;
}
