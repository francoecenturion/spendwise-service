package com.spendwise.service;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.model.Category;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.service.interfaces.ICategoryService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void populate(Category category, CategoryDTO dto) {
        category.setName(dto.getName());
    }

    @Transactional
    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category category = new Category();
        this.populate(category, dto);
        category.setEnabled(true);
        Category savedCategory = categoryRepository.save(category);
        log.debug("Category with id {} created successfully", savedCategory.getId());
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Transactional
    @Override
    public CategoryDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        Category category = find(id);
        log.debug("Category with id {} read successfully", category.getId());
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> list() {
        log.debug("Listing all categories");
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CategoryDTO update(Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException {
        Category category = find(id);
        this.populate(category, dto);
        Category updatedCategory = categoryRepository.save(category);
        log.debug("Category with id {} updated successfully", category.getId());
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Transactional
    @Override
    public CategoryDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        Category category = find(id);
        categoryRepository.delete(category);
        log.debug("Category with id {} deleted successfully", category.getId());
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Transactional
    @Override
    public CategoryDTO disable(Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException {
        Category category = find(id);
        this.populate(category, dto);
        category.setEnabled(false);
        Category savedCategory = categoryRepository.save(category);
        log.debug("Category with id {} disabled successfully", category.getId());
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    protected Category find(Long id) throws ChangeSetPersister.NotFoundException {
        return categoryRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }
}
