package com.visa.cardapi.service;
import com.visa.cardapi.model.Card;
import com.visa.cardapi.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {
    @Mock
    private CardRepository repo;
    @Mock
    private CryptoService crypto;
    @InjectMocks
    private CardService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =======================
    // Test findCard
    // =======================
    @Test
    void testFindCardExists() {
        String cardNumber = "1234567890123456";
        String hash = "hash123";
        Card card = new Card("encrypted123", hash);
        card.setId(1L);

        when(crypto.hashCard(cardNumber)).thenReturn(hash);
        when(repo.findByCardHash(hash)).thenReturn(Optional.of(card));

        Long result = service.findCard(cardNumber);
        assertEquals(1L, result);

        verify(crypto).hashCard(cardNumber);
        verify(repo).findByCardHash(hash);
    }

    @Test
    void testFindCardNotExists() {
        String cardNumber = "0000000000000000";
        String hash = "hash0";

        when(crypto.hashCard(cardNumber)).thenReturn(hash);
        when(repo.findByCardHash(hash)).thenReturn(Optional.empty());

        Long result = service.findCard(cardNumber);
        assertNull(result);

        verify(crypto).hashCard(cardNumber);
        verify(repo).findByCardHash(hash);
    }

    // =======================
    // Test insertSingle
    // =======================
    @Test
    void testInsertSingleNewCard() {
        String cardNumber = "1111222233334444";
        String hash = "hash1111";
        String encrypted = "enc1111";

        when(crypto.hashCard(cardNumber)).thenReturn(hash);
        when(crypto.encrypt(cardNumber)).thenReturn(encrypted);
        when(repo.existsByCardHash(hash)).thenReturn(false);

        Card savedCard = new Card(encrypted, hash);
        savedCard.setId(100L);
        when(repo.save(any(Card.class))).thenReturn(savedCard);

        Long result = service.insertSingle(cardNumber);
        assertEquals(100L, result);

        verify(repo).existsByCardHash(hash);
        verify(repo).save(any(Card.class));
        verify(crypto).hashCard(cardNumber);
        verify(crypto).encrypt(cardNumber);
    }

    @Test
    void testInsertSingleDuplicateCard() {
        String cardNumber = "1111222233334444";
        String hash = "hash1111";

        when(crypto.hashCard(cardNumber)).thenReturn(hash);
        when(repo.existsByCardHash(hash)).thenReturn(true);

        Long result = service.insertSingle(cardNumber);
        assertNull(result);

        verify(repo).existsByCardHash(hash);
        verify(crypto).hashCard(cardNumber);
        verifyNoMoreInteractions(repo);
    }

    // =======================
    // Test insertFromFile
    // =======================
    @Test
    void testInsertFromFile() throws Exception {
        // Criar mock de arquivo TXT
        String content = ""
                + "C1     1234567890123456\n"
                + "C2     1111222233334444\n"
                + "C3     1111222233334444\n";

        MultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        // Configurar mocks do crypto
        when(crypto.hashCard("1234567890123456")).thenReturn("hash1");
        when(crypto.encrypt("1234567890123456")).thenReturn("enc1");

        when(crypto.hashCard("1111222233334444")).thenReturn("hash2");
        when(crypto.encrypt("1111222233334444")).thenReturn("enc2");

        // Repo: nenhuma existe no banco
        when(repo.findAllByCardHashIn(anyList())).thenReturn(Collections.emptyList());
        when(repo.saveAll(anyList())).thenAnswer(invocation -> {
            List<Card> cards = invocation.getArgument(0);
            long id = 1L;
            for (Card c : cards) {
                c.setId(id++);
            }
            return cards;
        });

        List<Long> insertedIds = service.insertFromFile(file);

        assertEquals(2, insertedIds.size()); // duplicado removido
        assertTrue(insertedIds.contains(1L));
        assertTrue(insertedIds.contains(2L));

        verify(crypto, times(3)).hashCard(anyString());
        verify(crypto, times(3)).encrypt(anyString());
        verify(repo, atLeastOnce()).findAllByCardHashIn(anyList());
        verify(repo, atLeastOnce()).saveAll(anyList());
    }
}