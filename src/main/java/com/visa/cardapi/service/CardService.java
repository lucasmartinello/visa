package com.visa.cardapi.service;
import com.visa.cardapi.model.Card;
import com.visa.cardapi.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CardService {
    private final CardRepository repo;
    private final CryptoService crypto;
    private static final int BATCH_SIZE = 500;

    public CardService(CardRepository repo, CryptoService crypto) {
        this.repo = repo;
        this.crypto = crypto;
    }

    public Long findCard(String cardNumber) {
        String hash = crypto.hashCard(cardNumber);
        return repo.findByCardHash(hash)
                .map(Card::getId)
                .orElse(null);
    }

    public Long insertSingle(String cardNumber) {
        String encrypted = crypto.encrypt(cardNumber);
        String hash = crypto.hashCard(cardNumber);
        if (!repo.existsByCardHash(hash)) {
            Card card = new Card(encrypted, hash);
            Card saved = repo.save(card);
            return saved.getId();
        }
        return null;
    }

    @Transactional
    public List<Long> insertFromFile(MultipartFile file) {
        List<Card> batch = new ArrayList<>();
        List<Long> insertedIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip lines that don't have a card (batch line, header)
                if (!line.startsWith("C")) continue;

                // Extract card number: columns 8 to 26 (index 7 to 26)
                String cardNumber = line.substring(7, Math.min(line.length(), 26)).trim();
                if (cardNumber.isEmpty()) continue;

                String hash = crypto.hashCard(cardNumber);
                String encrypted = crypto.encrypt(cardNumber);

                // Create Card object
                Card card = new Card(encrypted, hash);
                batch.add(card);

                // Insert into batch
                if (batch.size() >= BATCH_SIZE) {
                    insertedIds.addAll(saveBatchIgnoringDuplicates(batch));
                    batch.clear();
                }
            }

            // Insert remaining
            if (!batch.isEmpty()) {
                insertedIds.addAll(saveBatchIgnoringDuplicates(batch));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing file: " + e.getMessage(), e);
        }
        return insertedIds;
    }

    private List<Long> saveBatchIgnoringDuplicates(List<Card> batch) {
        Set<String> seenHashes = new HashSet<>();
        batch.removeIf(c -> !seenHashes.add(c.getCardHash()));
        List<String> hashes = batch.stream().map(Card::getCardHash).collect(Collectors.toList());
        List<String> existingHashes = repo.findAllByCardHashIn(hashes)
                .stream()
                .map(Card::getCardHash)
                .collect(Collectors.toList());
        batch.removeIf(c -> existingHashes.contains(c.getCardHash()));
        List<Card> saved = repo.saveAll(batch);
        return saved.stream().map(Card::getId).collect(Collectors.toList());
    }
}