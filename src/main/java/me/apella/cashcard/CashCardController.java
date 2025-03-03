package me.apella.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    @GetMapping("/{requestId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestId, Principal principal) {
        CashCard cashCard = findCashCard(requestId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(
            @RequestBody CashCard newCashCardRequest,
            UriComponentsBuilder ucb,
            Principal principal
    ) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashcard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb.path("/cashcards/{id}").buildAndExpand(savedCashcard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(
            @PathVariable Long requestedId,
            @RequestBody CashCard cashCardUpdate,
            Principal principal
    ) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            CashCard updatedCashcard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            cashCardRepository.save(updatedCashcard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(
            @PathVariable Long requestedId,
            Principal principal
    ) {
        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());

        if (cashCard != null) {
            cashCardRepository.delete(cashCard);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();

    }
}
