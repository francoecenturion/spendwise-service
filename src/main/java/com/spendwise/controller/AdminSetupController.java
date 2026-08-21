package com.spendwise.controller;

import com.spendwise.dto.RecommendedCategoryDTO;
import com.spendwise.dto.RecommendedCurrencyDTO;
import com.spendwise.dto.RecommendedEntityDTO;
import com.spendwise.dto.RecommendedMerchantShortcutDTO;
import com.spendwise.enums.CategoryType;
import com.spendwise.model.RecommendedCategory;
import com.spendwise.model.RecommendedCurrency;
import com.spendwise.model.RecommendedEntity;
import com.spendwise.model.RecommendedMerchantShortcut;
import com.spendwise.repository.RecommendedCategoryRepository;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedMerchantShortcutRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminSetupController {

    private final RecommendedCurrencyRepository currencyRepo;
    private final RecommendedEntityRepository entityRepo;
    private final RecommendedCategoryRepository categoryRepo;
    private final RecommendedMerchantShortcutRepository merchantShortcutRepo;

    public AdminSetupController(RecommendedCurrencyRepository currencyRepo,
                                RecommendedEntityRepository entityRepo,
                                RecommendedCategoryRepository categoryRepo,
                                RecommendedMerchantShortcutRepository merchantShortcutRepo) {
        this.currencyRepo = currencyRepo;
        this.entityRepo = entityRepo;
        this.categoryRepo = categoryRepo;
        this.merchantShortcutRepo = merchantShortcutRepo;
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
        return entityRepo.findAllByOrderByIdAsc().stream()
                .map(this::toEntityDTO)
                .toList();
    }

    @PostMapping("/recommended-entities")
    public RecommendedEntityDTO createEntity(@RequestBody RecommendedEntityDTO dto) {
        RecommendedEntity e = new RecommendedEntity();
        e.setName(dto.getName());
        e.setIconUrl(dto.getIconUrl());
        return toEntityDTO(entityRepo.save(e));
    }

    @PutMapping("/recommended-entities/{id}")
    public ResponseEntity<RecommendedEntityDTO> updateEntity(@PathVariable Long id,
                                                              @RequestBody RecommendedEntityDTO dto) {
        return entityRepo.findById(id).map(e -> {
            if (dto.getName() != null) e.setName(dto.getName());
            if (dto.getIconUrl() != null) e.setIconUrl(dto.getIconUrl());
            return ResponseEntity.ok(toEntityDTO(entityRepo.save(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-entities/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        if (!entityRepo.existsById(id)) return ResponseEntity.notFound().build();
        entityRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Recommended Categories ───────────────────────────────────────────────

    @GetMapping("/recommended-categories")
    public List<RecommendedCategoryDTO> listCategories() {
        return categoryRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toCategoryDTO)
                .toList();
    }

    @PostMapping("/recommended-categories")
    public RecommendedCategoryDTO createCategory(@RequestBody RecommendedCategoryDTO dto) {
        RecommendedCategory cat = new RecommendedCategory();
        cat.setName(dto.getName());
        cat.setIcon(dto.getIcon());
        if (dto.getType() != null) cat.setType(CategoryType.valueOf(dto.getType()));
        cat.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        return toCategoryDTO(categoryRepo.save(cat));
    }

    @PutMapping("/recommended-categories/{id}")
    public ResponseEntity<RecommendedCategoryDTO> updateCategory(@PathVariable Long id,
                                                                  @RequestBody RecommendedCategoryDTO dto) {
        return categoryRepo.findById(id).map(cat -> {
            if (dto.getName() != null) cat.setName(dto.getName());
            if (dto.getIcon() != null) cat.setIcon(dto.getIcon());
            if (dto.getType() != null) cat.setType(CategoryType.valueOf(dto.getType()));
            if (dto.getDisplayOrder() != null) cat.setDisplayOrder(dto.getDisplayOrder());
            return ResponseEntity.ok(toCategoryDTO(categoryRepo.save(cat)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) return ResponseEntity.notFound().build();
        categoryRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Recommended Merchant Shortcuts ───────────────────────────────────────

    @GetMapping("/recommended-merchant-shortcuts")
    public List<RecommendedMerchantShortcutDTO> listMerchantShortcuts() {
        return merchantShortcutRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toMerchantShortcutDTO)
                .toList();
    }

    @PostMapping("/recommended-merchant-shortcuts")
    public RecommendedMerchantShortcutDTO createMerchantShortcut(@RequestBody RecommendedMerchantShortcutDTO dto) {
        RecommendedMerchantShortcut m = new RecommendedMerchantShortcut();
        m.setName(dto.getName());
        m.setIcon(dto.getIcon());
        m.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        if (dto.getRecommendedCategoryId() != null) {
            categoryRepo.findById(dto.getRecommendedCategoryId()).ifPresent(m::setCategory);
        }
        return toMerchantShortcutDTO(merchantShortcutRepo.save(m));
    }

    @PutMapping("/recommended-merchant-shortcuts/{id}")
    public ResponseEntity<RecommendedMerchantShortcutDTO> updateMerchantShortcut(@PathVariable Long id,
                                                                                  @RequestBody RecommendedMerchantShortcutDTO dto) {
        return merchantShortcutRepo.findById(id).map(m -> {
            if (dto.getName() != null) m.setName(dto.getName());
            if (dto.getIcon() != null) m.setIcon(dto.getIcon());
            if (dto.getDisplayOrder() != null) m.setDisplayOrder(dto.getDisplayOrder());
            if (dto.getRecommendedCategoryId() != null) {
                categoryRepo.findById(dto.getRecommendedCategoryId()).ifPresent(m::setCategory);
            }
            return ResponseEntity.ok(toMerchantShortcutDTO(merchantShortcutRepo.save(m)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/recommended-merchant-shortcuts/{id}")
    public ResponseEntity<Void> deleteMerchantShortcut(@PathVariable Long id) {
        if (!merchantShortcutRepo.existsById(id)) return ResponseEntity.notFound().build();
        merchantShortcutRepo.deleteById(id);
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
        dto.setIconUrl(e.getIconUrl());
        return dto;
    }

    private RecommendedCategoryDTO toCategoryDTO(RecommendedCategory cat) {
        RecommendedCategoryDTO dto = new RecommendedCategoryDTO();
        dto.setId(cat.getId());
        dto.setName(cat.getName());
        dto.setIcon(cat.getIcon());
        if (cat.getType() != null) dto.setType(cat.getType().name());
        dto.setDisplayOrder(cat.getDisplayOrder());
        return dto;
    }

    private RecommendedMerchantShortcutDTO toMerchantShortcutDTO(RecommendedMerchantShortcut m) {
        RecommendedMerchantShortcutDTO dto = new RecommendedMerchantShortcutDTO();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setIcon(m.getIcon());
        dto.setDisplayOrder(m.getDisplayOrder());
        if (m.getCategory() != null) {
            dto.setRecommendedCategoryId(m.getCategory().getId());
        }
        return dto;
    }
}
