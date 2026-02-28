package com.spendwise.unittest;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.enums.CategoryType;
import com.spendwise.model.Category;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.service.CategoryService;
import com.spendwise.model.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test");
        testUser.setEnabled(true);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create category saves a new category successfully")
    public void testCreate() {

        // Arrange
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Groceries");
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setUser(testUser);

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
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Act
        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(category));
        CategoryDTO obtained = categoryService.findById(id);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findByIdAndUser(id, testUser);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Find category by ID throws exception when category does not exist")
    public void testFindNonExistingById() {

        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());
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
        category1.setType(CategoryType.EXPENSE);
        category1.setEnabled(true);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Salario");
        category2.setType(CategoryType.INCOME);
        category2.setEnabled(true);

        List<Category> categories = Arrays.asList(category1, category2);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Alimentación", obtained.getContent().get(0).getName());
        assertEquals(CategoryType.EXPENSE, obtained.getContent().get(0).getType());
        assertEquals("Salario", obtained.getContent().get(1).getName());
        assertEquals(CategoryType.INCOME, obtained.getContent().get(1).getType());
    }

    @Test
    @DisplayName("Update category modifies existing category successfully")
    public void testUpdate() throws Exception {

        // Arrange
        Long id = 1L;
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName("Groceries");
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(true);

        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setId(id);
        newCategoryDTO.setName("GROCERIES");
        newCategoryDTO.setType(CategoryType.EXPENSE);
        newCategoryDTO.setEnabled(true);

        CategoryDTO expected = newCategoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.update(id, newCategoryDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(categoryRepository).findByIdAndUser(id, testUser);
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
        category1.setType(CategoryType.EXPENSE);
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
        category1.setType(CategoryType.EXPENSE);
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
        category1.setType(CategoryType.EXPENSE);
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
    @DisplayName("List categories with type filter INCOME")
    public void testListWithTypeFilterIncome() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Salario");
        category1.setType(CategoryType.INCOME);
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setType(CategoryType.INCOME);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(CategoryType.INCOME, obtained.getContent().get(0).getType());
    }

    @Test
    @DisplayName("List categories with type filter EXPENSE")
    public void testListWithTypeFilterExpense() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setType(CategoryType.EXPENSE);
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setType(CategoryType.EXPENSE);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(CategoryType.EXPENSE, obtained.getContent().get(0).getType());
    }

    @Test
    @DisplayName("List categories with type filter SAVING")
    public void testListWithTypeFilterSaving() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fondo de emergencia");
        category1.setType(CategoryType.SAVING);
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setType(CategoryType.SAVING);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals(CategoryType.SAVING, obtained.getContent().get(0).getType());
    }

    @Test
    @DisplayName("List categories without type filter returns all types")
    public void testListWithoutTypeFilter() {

        // Arrange
        Category expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Alimentación");
        expenseCategory.setType(CategoryType.EXPENSE);
        expenseCategory.setEnabled(true);

        Category incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setName("Salario");
        incomeCategory.setType(CategoryType.INCOME);
        incomeCategory.setEnabled(true);

        Category savingCategory = new Category();
        savingCategory.setId(3L);
        savingCategory.setName("Fondo de emergencia");
        savingCategory.setType(CategoryType.SAVING);
        savingCategory.setEnabled(true);

        List<Category> categories = Arrays.asList(expenseCategory, incomeCategory, savingCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(3, obtained.getTotalElements());

        long incomeCount = obtained.getContent().stream()
                .filter(c -> CategoryType.INCOME == c.getType())
                .count();
        long expenseCount = obtained.getContent().stream()
                .filter(c -> CategoryType.EXPENSE == c.getType())
                .count();
        long savingCount = obtained.getContent().stream()
                .filter(c -> CategoryType.SAVING == c.getType())
                .count();

        assertEquals(1, incomeCount);
        assertEquals(1, expenseCount);
        assertEquals(1, savingCount);
    }

    @Test
    @DisplayName("List categories with multiple filters (name + type)")
    public void testListWithMultipleFilters() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setType(CategoryType.EXPENSE);
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 20);
        CategoryFilterDTO filters = new CategoryFilterDTO();
        filters.setName("Aliment");
        filters.setType(CategoryType.EXPENSE);

        // Act
        Mockito.when(categoryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(categoryPage);

        Page<CategoryDTO> obtained = categoryService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Alimentación", obtained.getContent().get(0).getName());
        assertEquals(CategoryType.EXPENSE, obtained.getContent().get(0).getType());
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
        category1.setType(CategoryType.EXPENSE);
        category1.setEnabled(true);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Transporte");
        category2.setType(CategoryType.EXPENSE);
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
        assertEquals(25, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals(3, obtained.getTotalPages());
        assertEquals(0, obtained.getNumber());
    }

    @Test
    @DisplayName("List categories with custom page size")
    public void testListWithCustomPageSize() {

        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Alimentación");
        category1.setType(CategoryType.EXPENSE);
        category1.setEnabled(true);

        List<Category> categories = Arrays.asList(category1);
        Page<Category> categoryPage = new PageImpl<>(categories);

        Pageable pageable = PageRequest.of(0, 5);
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
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(true);

        CategoryDTO expected = categoryDTO;
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Act
        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(category));
        CategoryDTO deleted = categoryService.delete(id);

        // Assert
        assertEquals(expected, deleted);
        Mockito.verify(categoryRepository).findByIdAndUser(id, testUser);
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
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(true);
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.disable(id);

        // Assert
        assertFalse(obtained.getEnabled());
        Mockito.verify(categoryRepository).findByIdAndUser(id, testUser);
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
        categoryDTO.setType(CategoryType.EXPENSE);
        categoryDTO.setEnabled(false);
        Category category = modelMapper.map(categoryDTO, Category.class);

        Mockito.when(categoryRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CategoryDTO obtained = categoryService.enable(id);

        // Assert
        assertTrue(obtained.getEnabled());
        Mockito.verify(categoryRepository).findByIdAndUser(id, testUser);
        Mockito.verify(categoryRepository).save(category);
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

}
