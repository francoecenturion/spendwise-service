package com.spendwise.controller;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.service.interfaces.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final ICategoryService iCategoryService;

    @Autowired
    public CategoryController(ICategoryService iCategoryService) {
        this.iCategoryService = iCategoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO dto) {
        CategoryDTO category = iCategoryService.create(dto);
        log.debug("POST to Category Finished {}", category);
        return ResponseEntity
                .ok(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CategoryDTO category = iCategoryService.findById(id);
        log.debug("GET to Category Finished {}", category);
        return ResponseEntity
                .ok(category);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute CategoryFilterDTO filters,
        Pageable pageable
    ) {
        Page<CategoryDTO> categories = iCategoryService.list(filters, pageable);
        log.debug("LIST Categories Finished");
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @RequestBody CategoryDTO dto) throws ChangeSetPersister.NotFoundException {
        CategoryDTO category = iCategoryService.update(id, dto);
        log.debug("PUT to Category Finished {}", category);
        return ResponseEntity
                .ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        CategoryDTO category = iCategoryService.delete(id);
        log.debug("DELETE    to Category Finished {}", category);
        return ResponseEntity
                .ok(category);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<CategoryDTO> disable(@PathVariable Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException {
        CategoryDTO category = iCategoryService.disable(id, dto);
        log.debug("DISABLE Category Finished {}", category);
        return ResponseEntity
                .ok(category);
    }
}
