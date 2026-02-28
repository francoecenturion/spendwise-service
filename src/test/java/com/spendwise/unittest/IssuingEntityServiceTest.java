package com.spendwise.unittest;

import com.spendwise.dto.IssuingEntityDTO;
import com.spendwise.dto.IssuingEntityFilterDTO;
import com.spendwise.model.IssuingEntity;
import com.spendwise.repository.IssuingEntityRepository;
import com.spendwise.service.IssuingEntityService;
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
@DisplayName("IssuingEntity Unit Tests")
public class IssuingEntityServiceTest {

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private IssuingEntityRepository issuingEntityRepository;

    @InjectMocks
    private IssuingEntityService issuingEntityService;

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
    @DisplayName("Create issuing entity saves a new entity successfully")
    public void testCreate() {

        // Arrange
        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setDescription("Banco Nación");
        dto.setEnabled(true);

        IssuingEntityDTO expected = dto;
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);
        entity.setUser(testUser);

        // Act
        Mockito.when(issuingEntityRepository.save(entity)).thenReturn(entity);
        IssuingEntityDTO obtained = issuingEntityService.create(dto);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(issuingEntityRepository).save(entity);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

    @Test
    @DisplayName("Find issuing entity by ID returns the entity when it exists")
    public void testFindById() throws Exception {

        // Arrange
        Long id = 1L;

        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setId(id);
        dto.setDescription("Banco Nación");
        dto.setEnabled(true);

        IssuingEntityDTO expected = dto;
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);

        // Act
        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        IssuingEntityDTO obtained = issuingEntityService.findById(id);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(issuingEntityRepository).findByIdAndUser(id, testUser);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

    @Test
    @DisplayName("Find issuing entity by ID throws exception when entity does not exist")
    public void testFindNonExistingById() {

        // Arrange
        Long id = 1L;

        // Act & Assert
        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> issuingEntityService.findById(id));
    }

    @Test
    @DisplayName("List all issuing entities returns complete list")
    public void testList() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Banco Nación");
        entity1.setEnabled(true);

        IssuingEntity entity2 = new IssuingEntity();
        entity2.setId(2L);
        entity2.setDescription("Banco Galicia");
        entity2.setEnabled(true);

        List<IssuingEntity> entities = Arrays.asList(entity1, entity2);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("Banco Nación", obtained.getContent().get(0).getDescription());
        assertEquals("Banco Galicia", obtained.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("List issuing entities with description filter")
    public void testListWithDescriptionFilter() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Banco Nación");
        entity1.setEnabled(true);

        List<IssuingEntity> entities = Arrays.asList(entity1);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();
        filters.setDescription("Nación");

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Banco Nación", obtained.getContent().get(0).getDescription());
    }

    @Test
    @DisplayName("List issuing entities with enabled filter true")
    public void testListWithEnabledFilterTrue() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Banco Nación");
        entity1.setEnabled(true);

        List<IssuingEntity> entities = Arrays.asList(entity1);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();
        filters.setEnabled(true);

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List issuing entities with enabled filter false")
    public void testListWithEnabledFilterFalse() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Entidad Inactiva");
        entity1.setEnabled(false);

        List<IssuingEntity> entities = Arrays.asList(entity1);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();
        filters.setEnabled(false);

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertFalse(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List issuing entities with multiple filters (description + enabled)")
    public void testListWithMultipleFilters() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Banco Nación");
        entity1.setEnabled(true);

        List<IssuingEntity> entities = Arrays.asList(entity1);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();
        filters.setDescription("Banco");
        filters.setEnabled(true);

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Banco Nación", obtained.getContent().get(0).getDescription());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List issuing entities with no results")
    public void testListWithNoResults() {

        // Arrange
        List<IssuingEntity> entities = Collections.emptyList();
        Page<IssuingEntity> entityPage = new PageImpl<>(entities);

        Pageable pageable = PageRequest.of(0, 20);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();
        filters.setDescription("NoExiste");

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List issuing entities with pagination")
    public void testListWithPagination() {

        // Arrange
        IssuingEntity entity1 = new IssuingEntity();
        entity1.setId(1L);
        entity1.setDescription("Banco Nación");
        entity1.setEnabled(true);

        IssuingEntity entity2 = new IssuingEntity();
        entity2.setId(2L);
        entity2.setDescription("Banco Galicia");
        entity2.setEnabled(true);

        List<IssuingEntity> entities = Arrays.asList(entity1, entity2);
        Page<IssuingEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        IssuingEntityFilterDTO filters = new IssuingEntityFilterDTO();

        // Act
        Mockito.when(issuingEntityRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(entityPage);

        Page<IssuingEntityDTO> obtained = issuingEntityService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals(3, obtained.getTotalPages());
        assertEquals(0, obtained.getNumber());
    }

    @Test
    @DisplayName("Update issuing entity modifies existing entity successfully")
    public void testUpdate() throws Exception {

        // Arrange
        Long id = 1L;
        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setId(id);
        dto.setDescription("Banco Nación");
        dto.setEnabled(true);

        IssuingEntityDTO newDto = new IssuingEntityDTO();
        newDto.setId(id);
        newDto.setDescription("BANCO NACIÓN");
        newDto.setEnabled(true);

        IssuingEntityDTO expected = newDto;
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);

        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(issuingEntityRepository.save(any(IssuingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        IssuingEntityDTO obtained = issuingEntityService.update(id, newDto);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(issuingEntityRepository).findByIdAndUser(id, testUser);
        Mockito.verify(issuingEntityRepository).save(entity);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

    @Test
    @DisplayName("Delete issuing entity removes it from the database")
    public void testDelete() throws Exception {

        // Arrange
        Long id = 1L;
        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setId(id);
        dto.setDescription("Banco Nación");
        dto.setEnabled(true);

        IssuingEntityDTO expected = dto;
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);

        // Act
        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        IssuingEntityDTO deleted = issuingEntityService.delete(id);

        // Assert
        assertEquals(expected, deleted);
        Mockito.verify(issuingEntityRepository).findByIdAndUser(id, testUser);
        Mockito.verify(issuingEntityRepository).delete(entity);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

    @Test
    @DisplayName("Disable issuing entity sets enabled flag to false")
    public void testDisable() throws Exception {

        // Arrange
        Long id = 1L;
        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setId(id);
        dto.setDescription("Banco Nación");
        dto.setEnabled(true);
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);

        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(issuingEntityRepository.save(any(IssuingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        IssuingEntityDTO obtained = issuingEntityService.disable(id);

        // Assert
        assertFalse(obtained.getEnabled());
        Mockito.verify(issuingEntityRepository).findByIdAndUser(id, testUser);
        Mockito.verify(issuingEntityRepository).save(entity);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

    @Test
    @DisplayName("Enable issuing entity sets enabled flag to true")
    public void testEnable() throws Exception {

        // Arrange
        Long id = 1L;
        IssuingEntityDTO dto = new IssuingEntityDTO();
        dto.setId(id);
        dto.setDescription("Banco Nación");
        dto.setEnabled(false);
        IssuingEntity entity = modelMapper.map(dto, IssuingEntity.class);

        Mockito.when(issuingEntityRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(entity));
        Mockito.when(issuingEntityRepository.save(any(IssuingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        IssuingEntityDTO obtained = issuingEntityService.enable(id);

        // Assert
        assertTrue(obtained.getEnabled());
        Mockito.verify(issuingEntityRepository).findByIdAndUser(id, testUser);
        Mockito.verify(issuingEntityRepository).save(entity);
        Mockito.verifyNoMoreInteractions(issuingEntityRepository);
    }

}
