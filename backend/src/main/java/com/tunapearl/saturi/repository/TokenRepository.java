package com.tunapearl.saturi.repository;

import com.tunapearl.saturi.domain.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface TokenRepository extends CrudRepository<Token,Long> {

//    Optional<Token> findById(Long id);
}
