package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRedefinicaoSenhaRepository
        extends JpaRepository<TokenRedefinicaoSenha, Integer> {

    Optional<TokenRedefinicaoSenha>
    findByTokenHashAndUtilizadoFalse(
            String tokenHash
    );
}