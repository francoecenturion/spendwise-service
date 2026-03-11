package com.spendwise.controller;

import com.spendwise.dto.RecommendedCurrencyDTO;
import com.spendwise.dto.RecommendedEntityDTO;
import com.spendwise.dto.RecommendedPaymentMethodDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.RecommendedCurrency;
import com.spendwise.model.RecommendedEntity;
import com.spendwise.model.RecommendedPaymentMethod;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedPaymentMethodRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminSetupController {

    private final RecommendedCurrencyRepository currencyRepo;
    private final RecommendedEntityRepository entityRepo;
    private final RecommendedPaymentMethodRepository pmRepo;

    public AdminSetupController(RecommendedCurrencyRepository currencyRepo,
                                RecommendedEntityRepository entityRepo,
                                RecommendedPaymentMethodRepository pmRepo) {
        this.currencyRepo = currencyRepo;
        this.entityRepo = entityRepo;
        this.pmRepo = pmRepo;
    }

    // ── Recommended Currencies ────────────────────────────────────────────────

    @GetMapping("/recommended-currencies")
    public List<RecommendedCurrencyDTO> listCurrencies() {
        return currencyRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toCurrencyDTO)
                .toList();
    }

    @PostMapping("/recommended-currencies")
    public RecommendedCurrencyDTO createCurrency(@RequestBody RecommendedCurrencyDTO dto) {
        RecommendedCurrency c = new RecommendedCurrency();
        c.setName(dto.getName());
        c.setSymbol(dto.getSymbol());
        c.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        c.setDefaultSelected(dto.getDefaultSelected() != null ? dto.getDefaultSelected() : false);
        return toCurrencyDTO(currencyRepo.save(c));
    }

    @PutMapping("/recommended-currencies/{id}")
    public ResponseEntity<RecommendedCurrencyDTO> updateCurrency(@PathVariable Long id,
                                                                  @RequestBody RecommendedCurrencyDTO dto) {
        return currencyRepo.findById(id).map(c -> {
            if (dto.getName() != null) c.setName(dto.getName());
            if (dto.getSymbol() != null) c.setSymbol(dto.getSymbol());
            if (dto.getDisplayOrder() != null) c.setDisplayOrder(dto.getDisplayOrder());
            if (dto.getDefaultSelected() != null) c.setDefaultSelected(dto.getDefaultSelected());
            return ResponseEntity.ok(toCurrencyDTO(currencyRepo.save(c)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-currencies/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
        if (!currencyRepo.existsById(id)) return ResponseEntity.notFound().build();
        currencyRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Recommended Entities ─────────────────────────────────────────────────

    @GetMapping("/recommended-entities")
    public List<RecommendedEntityDTO> listEntities() {
        return entityRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toEntityDTO)
                .toList();
    }

    @PostMapping("/recommended-entities")
    public RecommendedEntityDTO createEntity(@RequestBody RecommendedEntityDTO dto) {
        RecommendedEntity e = new RecommendedEntity();
        e.setName(dto.getName());
        e.setImageUrl(dto.getImageUrl());
        e.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        return toEntityDTO(entityRepo.save(e));
    }

    @PutMapping("/recommended-entities/{id}")
    public ResponseEntity<RecommendedEntityDTO> updateEntity(@PathVariable Long id,
                                                              @RequestBody RecommendedEntityDTO dto) {
        return entityRepo.findById(id).map(e -> {
            if (dto.getName() != null) e.setName(dto.getName());
            if (dto.getImageUrl() != null) e.setImageUrl(dto.getImageUrl());
            if (dto.getDisplayOrder() != null) e.setDisplayOrder(dto.getDisplayOrder());
            return ResponseEntity.ok(toEntityDTO(entityRepo.save(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-entities/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        if (!entityRepo.existsById(id)) return ResponseEntity.notFound().build();
        entityRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Recommended Payment Methods ──────────────────────────────────────────

    @GetMapping("/recommended-payment-methods")
    public List<RecommendedPaymentMethodDTO> listPaymentMethods() {
        return pmRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toPmDTO)
                .toList();
    }

    @PostMapping("/recommended-payment-methods")
    public RecommendedPaymentMethodDTO createPaymentMethod(@RequestBody RecommendedPaymentMethodDTO dto) {
        RecommendedPaymentMethod pm = new RecommendedPaymentMethod();
        pm.setName(dto.getName());
        pm.setImageUrl(dto.getImageUrl());
        pm.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        if (dto.getPaymentMethodType() != null) {
            pm.setPaymentMethodType(PaymentMethodType.valueOf(dto.getPaymentMethodType()));
        }
        if (dto.getRecommendedEntityId() != null) {
            entityRepo.findById(dto.getRecommendedEntityId()).ifPresent(pm::setEntity);
        }
        return toPmDTO(pmRepo.save(pm));
    }

    @PutMapping("/recommended-payment-methods/{id}")
    public ResponseEntity<RecommendedPaymentMethodDTO> updatePaymentMethod(@PathVariable Long id,
                                                                            @RequestBody RecommendedPaymentMethodDTO dto) {
        return pmRepo.findById(id).map(pm -> {
            if (dto.getName() != null) pm.setName(dto.getName());
            if (dto.getImageUrl() != null) pm.setImageUrl(dto.getImageUrl());
            if (dto.getDisplayOrder() != null) pm.setDisplayOrder(dto.getDisplayOrder());
            if (dto.getPaymentMethodType() != null) {
                pm.setPaymentMethodType(PaymentMethodType.valueOf(dto.getPaymentMethodType()));
            }
            if (dto.getRecommendedEntityId() != null) {
                entityRepo.findById(dto.getRecommendedEntityId()).ifPresent(pm::setEntity);
            } else if (dto.getRecommendedEntityId() == null && dto.getName() != null) {
                pm.setEntity(null);
            }
            return ResponseEntity.ok(toPmDTO(pmRepo.save(pm)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-payment-methods/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        if (!pmRepo.existsById(id)) return ResponseEntity.notFound().build();
        pmRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private RecommendedCurrencyDTO toCurrencyDTO(RecommendedCurrency c) {
        RecommendedCurrencyDTO dto = new RecommendedCurrencyDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSymbol(c.getSymbol());
        dto.setDisplayOrder(c.getDisplayOrder());
        dto.setDefaultSelected(c.getDefaultSelected());
        return dto;
    }

    private RecommendedEntityDTO toEntityDTO(RecommendedEntity e) {
        RecommendedEntityDTO dto = new RecommendedEntityDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setImageUrl(e.getImageUrl());
        dto.setDisplayOrder(e.getDisplayOrder());
        return dto;
    }

    private RecommendedPaymentMethodDTO toPmDTO(RecommendedPaymentMethod pm) {
        RecommendedPaymentMethodDTO dto = new RecommendedPaymentMethodDTO();
        dto.setId(pm.getId());
        dto.setName(pm.getName());
        dto.setImageUrl(pm.getImageUrl());
        dto.setDisplayOrder(pm.getDisplayOrder());
        if (pm.getPaymentMethodType() != null) {
            dto.setPaymentMethodType(pm.getPaymentMethodType().name());
        }
        if (pm.getEntity() != null) {
            dto.setRecommendedEntityId(pm.getEntity().getId());
        }
        return dto;
    }
}
