package com.visa.cardapi.model;
import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_card", nullable = false)
    private String encryptedCard;

    @Column(name = "card_hash", nullable = false)
    private String cardHash;

    public Card(String encryptedCard, String hash) {
        this.encryptedCard = encryptedCard;
        this.cardHash = hash;
    }

    public Card() {}

    public Card(String cardHash) {
        this.cardHash = cardHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCardHash() { return cardHash; }

    public String getEncryptedCard() {
        return encryptedCard;
    }
}