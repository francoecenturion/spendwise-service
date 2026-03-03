package com.spendwise.unittest;

import com.spendwise.dto.RecurrentExpenseDTO;
import com.spendwise.dto.RecurrentExpenseRecordDTO;
import com.spendwise.dto.RecurrentExpenseRecordFilterDTO;
import com.spendwise.model.Expense;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.service.RecurrentExpenseRecordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrentExpenseRecord Unit Tests")
public class RecurrentExpenseRecordServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private RecurrentExpenseRecordRepository recordRepository;

    @InjectMocks
    private RecurrentExpenseRecordService recordService;

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

    private RecurrentExpense buildRecurrentExpense() {
        RecurrentExpense re = new RecurrentExpense();
        re.setId(1L);
        re.setDescription("Alquiler");
        re.setEnabled(true);
        re.setUser(testUser);
        return re;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create recurrent expense record saves it with the given month and year")
    public void testCreate() {
        // Arrange
        RecurrentExpense recurrentExpense = buildRecurrentExpense();

        RecurrentExpenseRecordDTO dto = new RecurrentExpenseRecordDTO();
        dto.setRecurrentExpense(modelMapper.map(recurrentExpense, RecurrentExpenseDTO.class));
        dto.setMonth(3);
        dto.setYear(2026);
        dto.setCancelled(false);

        Mockito.when(recordRepository.save(any(RecurrentExpenseRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseRecordDTO result = recordService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getMonth());
        assertEquals(2026, result.getYear());
        assertFalse(result.getCancelled());
        Mockito.verify(recordRepository).save(any(RecurrentExpenseRecord.class));
        Mockito.verifyNoMoreInteractions(recordRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND BY ID
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find record by ID returns it when it exists")
    public void testFindById() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpenseRecord entity = new RecurrentExpenseRecord();
        entity.setId(id);
        entity.setRecurrentExpense(buildRecurrentExpense());
        entity.setMonth(3);
        entity.setYear(2026);
        entity.setCancelled(false);
        entity.setUser(testUser);

        Mockito.when(recordRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));

        // Act
        RecurrentExpenseRecordDTO result = recordService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(3, result.getMonth());
        assertEquals(2026, result.getYear());
        Mockito.verify(recordRepository).findByIdAndUser(id, testUser);
        Mockito.verifyNoMoreInteractions(recordRepository);
    }

    @Test
    @DisplayName("Find record by ID throws exception when it does not exist")
    public void testFindNonExistingById() {
        // Arrange
        Long id = 999L;
        Mockito.when(recordRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> recordService.findById(id));
        Mockito.verify(recordRepository).findByIdAndUser(id, testUser);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LIST
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("List all records returns complete list")
    public void testList() {
        // Arrange
        RecurrentExpense re = buildRecurrentExpense();

        RecurrentExpenseRecord r1 = new RecurrentExpenseRecord();
        r1.setId(1L);
        r1.setRecurrentExpense(re);
        r1.setMonth(3);
        r1.setYear(2026);
        r1.setCancelled(false);

        RecurrentExpenseRecord r2 = new RecurrentExpenseRecord();
        r2.setId(2L);
        r2.setRecurrentExpense(re);
        r2.setMonth(2);
        r2.setYear(2026);
        r2.setCancelled(true);

        Page<RecurrentExpenseRecord> page = new PageImpl<>(Arrays.asList(r1, r2));
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseRecordFilterDTO filters = new RecurrentExpenseRecordFilterDTO();

        Mockito.when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseRecordDTO> result = recordService.list(filters, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertFalse(result.getContent().get(0).getCancelled());
        assertTrue(result.getContent().get(1).getCancelled());
    }

    @Test
    @DisplayName("List records filtered by month and year")
    public void testListWithMonthYearFilter() {
        // Arrange
        RecurrentExpenseRecord r1 = new RecurrentExpenseRecord();
        r1.setId(1L);
        r1.setRecurrentExpense(buildRecurrentExpense());
        r1.setMonth(3);
        r1.setYear(2026);
        r1.setCancelled(false);

        Page<RecurrentExpenseRecord> page = new PageImpl<>(Collections.singletonList(r1));
        Pageable pageable = PageRequest.of(0, 20);
        RecurrentExpenseRecordFilterDTO filters = new RecurrentExpenseRecordFilterDTO();
        filters.setMonth(3);
        filters.setYear(2026);

        Mockito.when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<RecurrentExpenseRecordDTO> result = recordService.list(filters, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(3, result.getContent().get(0).getMonth());
        assertEquals(2026, result.getContent().get(0).getYear());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CANCEL / UNCANCEL
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Cancel record sets cancelled to true")
    public void testCancel() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpenseRecord entity = new RecurrentExpenseRecord();
        entity.setId(id);
        entity.setRecurrentExpense(buildRecurrentExpense());
        entity.setMonth(3);
        entity.setYear(2026);
        entity.setCancelled(false);
        entity.setUser(testUser);

        Mockito.when(recordRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(recordRepository.save(any(RecurrentExpenseRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseRecordDTO result = recordService.cancel(id);

        // Assert
        assertTrue(result.getCancelled());
        Mockito.verify(recordRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recordRepository).save(entity);
        Mockito.verifyNoMoreInteractions(recordRepository);
    }

    @Test
    @DisplayName("Uncancel record sets cancelled to false and clears expense reference")
    public void testUncancel() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;

        Expense linkedExpense = new Expense();
        linkedExpense.setId(10L);

        RecurrentExpenseRecord entity = new RecurrentExpenseRecord();
        entity.setId(id);
        entity.setRecurrentExpense(buildRecurrentExpense());
        entity.setMonth(3);
        entity.setYear(2026);
        entity.setCancelled(true);
        entity.setExpense(linkedExpense);
        entity.setUser(testUser);

        Mockito.when(recordRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(recordRepository.save(any(RecurrentExpenseRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RecurrentExpenseRecordDTO result = recordService.uncancel(id);

        // Assert
        assertFalse(result.getCancelled());
        assertNull(result.getExpense());
        Mockito.verify(recordRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recordRepository).save(entity);
        Mockito.verifyNoMoreInteractions(recordRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete record removes it from the database")
    public void testDelete() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long id = 1L;
        RecurrentExpenseRecord entity = new RecurrentExpenseRecord();
        entity.setId(id);
        entity.setRecurrentExpense(buildRecurrentExpense());
        entity.setMonth(3);
        entity.setYear(2026);
        entity.setCancelled(false);
        entity.setUser(testUser);

        Mockito.when(recordRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));

        // Act
        RecurrentExpenseRecordDTO result = recordService.delete(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        Mockito.verify(recordRepository).findByIdAndUser(id, testUser);
        Mockito.verify(recordRepository).delete(entity);
        Mockito.verifyNoMoreInteractions(recordRepository);
    }

}
