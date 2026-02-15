package com.spendwise.service.interfaces;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.model.Category;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;

public interface ICategoryService {

    void populate(Category category, CategoryDTO dto);
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    List<CategoryDTO> list();
    CategoryDTO update(Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException;
    CategoryDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    CategoryDTO disable(Long id, CategoryDTO dto) throws ChangeSetPersister.NotFoundException;

}
