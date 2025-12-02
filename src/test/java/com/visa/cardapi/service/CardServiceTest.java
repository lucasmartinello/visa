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

    // =========================================================
    // findCard
    // =========================================================
    @Test
    void givenExistingCard_whenFindCard_thenReturnId() {
        String number = "1234567890123456";
        String hash = "hash123";
        Card card = new Card("encrypted", hash);
        card.setId(1L);
        when(crypto.hashCard(number)).thenReturn(hash);
        when(repo.findByCardHash(hash)).thenReturn(Optional.of(card));
        Long result = service.findCard(number);
        assertEquals(1L, result);
        verify(crypto).hashCard(number);
        verify(repo).findByCardHash(hash);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void givenNonExistingCard_whenFindCard_thenReturnNull() {
        String number = "0000000000000000";
        String hash = "h0";
        when(crypto.hashCard(number)).thenReturn(hash);
        when(repo.findByCardHash(hash)).thenReturn(Optional.empty());
        Long result = service.findCard(number);
        assertNull(result);
        verify(repo).findByCardHash(hash);
        verify(crypto).hashCard(number);
        verifyNoMoreInteractions(repo);
    }

    // =========================================================
    // insertSingle
    // =========================================================
    @Test
    void givenNewCard_whenInsertSingle_thenSaveAndReturnId() {
        String number = "1111222233334444";
        String hash = "h1";
        String encrypted = "enc";

        when(crypto.hashCard(number)).thenReturn(hash);
        when(repo.existsByCardHash(hash)).thenReturn(false);
        when(crypto.encrypt(number)).thenReturn(encrypted);
        Card saved = new Card(encrypted, hash);
        saved.setId(100L);
        when(repo.save(any(Card.class))).thenReturn(saved);
        Long result = service.insertSingle(number);
        assertEquals(100L, result);
        verify(repo).existsByCardHash(hash);
        verify(crypto).encrypt(number);
        verify(repo).save(argThat(c ->
                c.getEncryptedCard().equals(encrypted) &&
                        c.getCardHash().equals(hash)
        ));
        verifyNoMoreInteractions(repo);
    }

    @Test
    void givenDuplicateCard_whenInsertSingle_thenReturnNull() {
        String number = "1111222233334444";
        String hash = "h1";

        when(crypto.hashCard(number)).thenReturn(hash);
        when(repo.existsByCardHash(hash)).thenReturn(true);

        Long result = service.insertSingle(number);
        assertNull(result);

        verify(repo).existsByCardHash(hash);
        verify(crypto).hashCard(number);
        verifyNoMoreInteractions(repo);
    }

    // =========================================================
    // insertFromFile
    // =========================================================
    @Test
    void givenFileWithDuplicates_whenInsertFromFile_thenDeduplicateAndSave() throws Exception {
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

        when(crypto.hashCard("1234567890123456")).thenReturn("h1");
        when(crypto.encrypt("1234567890123456")).thenReturn("e1");

        when(crypto.hashCard("1111222233334444")).thenReturn("h2");
        when(crypto.encrypt("1111222233334444")).thenReturn("e2");

        when(repo.findAllByCardHashIn(anyList()))
                .thenReturn(Collections.emptyList());

        when(repo.saveAll(anyList())).thenAnswer(inv -> {
            List<Card> list = inv.getArgument(0);
            long id = 1L;
            for (Card c : list) c.setId(id++);
            return list;
        });

        List<Long> ids = service.insertFromFile(file);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));

        verify(crypto, times(3)).hashCard(anyString());
        verify(crypto, times(3)).encrypt(anyString());

        verify(repo).findAllByCardHashIn(argThat(list -> list.size() == 2));
        verify(repo).saveAll(argThat(iter -> {
            List<Card> list = new ArrayList<>();
            iter.forEach(list::add);
            return list.size() == 2 &&
                    list.stream().anyMatch(c -> "h1".equals(c.getCardHash())) &&
                    list.stream().anyMatch(c -> "h2".equals(c.getCardHash()));
        }));
    }
}