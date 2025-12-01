package com.visa.cardapi.controller;
import com.visa.cardapi.dto.CardRequest;
import com.visa.cardapi.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cards")
public class CardController {
    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkCard(@RequestParam String cardNumber) {
        Long id = service.findCard(cardNumber);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Card not found");
        }
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PostMapping("/single")
    public ResponseEntity<?> insertSingle(@RequestBody CardRequest req) {
        Long id = service.insertSingle(req.getCardNumber());
        if (id == null)
            return ResponseEntity.status(409).body("Card already exist");
        return ResponseEntity.ok(id);
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Long>> upload(@RequestParam("file") MultipartFile file) {
        List<Long> ids = service.insertFromFile(file);
        return ResponseEntity.ok(ids);
    }
}