package com.tunapearl.saturi.repository;


import com.tunapearl.saturi.domain.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    @PersistenceContext
    EntityManager em;

    public Optional<UserEntity> findByUserId(Long userId) {
        return Optional.ofNullable(em.find(UserEntity.class, userId));
    }
}
