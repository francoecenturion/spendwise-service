package com.spendwise.unittest;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.model.Category;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Unit Tests")
public class CategoryServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Create category saves a new category successfully")
    public void testCreate() {

        // Arrange
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Act
        Mockito.when(categoryRepository.save(category)).thenReturn(category);
        CategoryDTO obtained = categoryService.create(categoryDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }

    @Test
    @DisplayName("Find category by ID returns the category when it exists")
    public void testFindById() throws Exception {

        // Arrange
        Long id = 1L;

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Act
        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        CategoryDTO obtained = categoryService.findById(id);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Find category by ID throws exception when category does not exist")
    public void testFindNonExistingById() {

        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> categoryService.findById(id));

    }

    @Test
    @DisplayName("List all categories returns complete list")
    public void testList() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Transporte");
        category2.setEnabled(true);

        List<Category> categories = Arrays.asList(category1, category2);

        // Act
        Mockito.when(categoryRepository.findAll()).thenReturn(categories);
        List<CategoryDTO> obtained = categoryService.list();

        // Assert
        assertEquals(2, obtained.size());
        assertEquals("Alimentación", obtained.get(0).getName());
        assertEquals("Transporte", obtained.get(1).getName());
        assertTrue(obtained.get(0).getEnabled());
        assertTrue(obtained.get(1).getEnabled());

    }

    @Test
    @DisplayName("Update category modifies existing category successfully")
    public void testUpdate() throws Exception {

        // Arrange
        Long id = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(true);

        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setId(id);
        newCategoryDTO.setName("GROCERIES");
        newCategoryDTO.setEnabled(true);

        CategoryDTO expected = newCategoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.update(id, newCategoryDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }

    @Test
    @DisplayName("Delete category removes it from the database")
    public void testDelete() throws Exception {

        // Arrange
        Long id = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Act
        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        CategoryDTO deleted = categoryService.delete(id);

        // Assert
        assertEquals(expected, deleted);
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).delete(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Disable category sets enabled flag to false")
    public void testDisable() throws Exception {

        // Arrange
        Long id = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(true);

        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setId(id);
        newCategoryDTO.setName("Groceries");
        newCategoryDTO.setEnabled(false);

        CategoryDTO expected = newCategoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.disable(id, newCategoryDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }

}
