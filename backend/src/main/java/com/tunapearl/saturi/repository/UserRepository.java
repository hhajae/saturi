package com.tunapearl.saturi.repository;

import com.tunapearl.saturi.domain.user.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public void saveUser(UserEntity user) {
        em.persist(user);
    }

    public Optional<UserEntity> findByUserId(Long userId) {
        return Optional.ofNullable(em.find(UserEntity.class, userId));
    }

    public Optional<List<UserEntity>> findByEmail(String email) {
        return Optional.ofNullable(em.createQuery(
                "select u from UserEntity u" +
                        " where u.email = :email", UserEntity.class)
                .setParameter("email", email)
                .getResultList());
    }

    public Optional<List<UserEntity>> findByNickname(String nickname) {
        return Optional.ofNullable(em.createQuery(
                "select u from UserEntity u" +
                        " where u.nickname = :nickname", UserEntity.class)
                .setParameter("nickname", nickname)
                .getResultList());
    }

    public Optional<List<UserEntity>> findByEmailAndPassword(String email, String password) {
        return Optional.ofNullable(em.createQuery(
                "select u from UserEntity u" +
                    " where u.email = :email and u.password = :password", UserEntity.class)
                .setParameter("email", email)
                .setParameter("password", password)
                .getResultList());
    }

    public Optional<List<UserEntity>> findAll() {
        return Optional.ofNullable(
                em.createQuery("select u from UserEntity u", UserEntity.class)
                .getResultList());
    }
}