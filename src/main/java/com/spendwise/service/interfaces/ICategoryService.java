package com.spendwise.service.interfaces;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.PaymentMethod;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {

    void populate(Category category, CategoryDTO dto);
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<CategoryDTO> list(CategoryFilterDTO categoryFilterDTO, Pageable pageable);
    CategoryDTO update(Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException;
    CategoryDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    CategoryDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    CategoryDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
