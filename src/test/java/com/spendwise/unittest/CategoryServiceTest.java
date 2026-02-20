package com.spendwise.unittest;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.CategoryFilterDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO(); // todos los campos en null

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Alimentación", obtained.getContent().get(0).getName());
        assertEquals("Transporte", obtained.getContent().get(1).getName());
        assertTrue(obtained.getContent().get(0).getEnabled());
        assertTrue(obtained.getContent().get(1).getEnabled());
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
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.update(id, newCategoryDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }


    @Test
    @DisplayName("List categories with name filter")
    public void testListWithNameFilter() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setName("Aliment");

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Alimentación", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List categories with enabled filter true")
    public void testListWithEnabledFilterTrue() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setEnabled(true);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List categories with enabled filter false")
    public void testListWithEnabledFilterFalse() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Inactiva");
        category1.setEnabled(false);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setEnabled(false);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertFalse(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List categories with isIncome filter true (income categories only)")
    public void testListWithIsIncomeFilterTrue() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Salario");
        category1.setEnabled(true);
        category1.setIsIncome(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setIsIncome(true);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertTrue(obtained.getContent().get(0).getIsIncome());
    }

    @Test
    @DisplayName("List categories with isIncome filter false (expense categories only)")
    public void testListWithIsIncomeFilterFalse() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);
        category1.setIsIncome(false);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setIsIncome(false);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertFalse(obtained.getContent().get(0).getIsIncome());
    }

    @Test
    @DisplayName("List all categories without isIncome filter (both income and expense categories)")
    public void testListWithoutIsIncomeFilter() {
        // Arrange
        Category expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Alimentación");
        expenseCategory.setEnabled(true);
        expenseCategory.setIsIncome(false);

        Category incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setName("Salario");
        incomeCategory.setEnabled(true);
        incomeCategory.setIsIncome(true);

        List<Category> categories = Arrays.asList(expenseCategory, incomeCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        // No se setea isIncome - debería traer ambos tipos

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());

        // Verificar que hay una de cada tipo
        long incomeCount = obtained.getContent().stream()
                .filter(CategoryDTO::getIsIncome)
                .count();
        long expenseCount = obtained.getContent().stream()
                .filter(c -> !c.getIsIncome())
                .count();

        assertEquals(1, incomeCount);
        assertEquals(1, expenseCount);
    }



    @Test
    @DisplayName("List categories with multiple filters")
    public void testListWithMultipleFilters() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setName("Aliment");
        filters.setEnabled(true);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Alimentación", obtained.getContent().get(0).getName());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List categories with no results")
    public void testListWithNoResults() {
        // Arrange
        List<Category> categories = Collections.emptyList();
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setName("NoExiste");

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List categories with pagination")
    public void testListWithPagination() {
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
        Page<Category> categoryPage = new PageImpl<>(categories, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        CategoryFilterDTO filters = new CategoryFilterDTO();

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements()); // total de registros
        assertEquals(2, obtained.getContent().size()); // registros en esta página
        assertEquals(3, obtained.getTotalPages()); // total de páginas (25/10 = 3)
        assertEquals(0, obtained.getNumber()); // página actual
    }

    @Test
    @DisplayName("List categories with custom page size")
    public void testListWithCustomPageSize() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 5); // tamaño de página personalizado
        CategoryFilterDTO filters = new CategoryFilterDTO();

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(5, pageable.getPageSize());
        assertEquals(1, obtained.getContent().size());
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
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.disable(id);

        // Assert
        assertFalse(obtained.getEnabled());
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }

    @Test
    @DisplayName("Enable category sets enabled flag to true")
    public void testEnable() throws Exception {

        // Arrange
        Long id = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setEnabled(false);
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.enable(id);

        // Assert
        assertTrue(obtained.getEnabled());
        Mockito.verify(categoryRepository).findById(id);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);

    }

}
