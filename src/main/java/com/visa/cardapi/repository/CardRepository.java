package com.visa.cardapi.repository;
import com.visa.cardapi.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardHash(String hash);
    boolean existsByCardHash(String hash);
    List<Card> findAllByCardHashIn(List<String> hashes);
}