package com.spendwise.unittest;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.UserFilterDTO;
import com.spendwise.model.user.User;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Unit Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ───────────────────────── helpers ──────────────────────────

    private User buildUser(Long id, String email, String name, String surname,
                           String passwordHash, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setSurname(surname);
        user.setPasswordHash(passwordHash);
        user.setEnabled(enabled);
        return user;
    }

    // ───────────────────────── create ───────────────────────────

    @Test
    @DisplayName("Create user hashes password and saves a new user successfully")
    public void testCreate() {

        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("john@example.com");
        userDTO.setName("John");
        userDTO.setSurname("Doe");
        userDTO.setPassword("rawPassword");

        String hashed = "$2a$10$mockedHash";
        Mockito.when(passwordEncoder.encode("rawPassword")).thenReturn(hashed);
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO obtained = userService.create(userDTO);

        // Assert
        assertEquals("john@example.com", obtained.getEmail());
        assertEquals("John", obtained.getName());
        assertEquals("Doe", obtained.getSurname());
        assertTrue(obtained.getEnabled());
        assertNull(obtained.getPassword()); // passwordHash is never mapped back to password
        Mockito.verify(passwordEncoder).encode("rawPassword");
        Mockito.verify(userRepository).save(any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // ───────────────────────── findById ─────────────────────────

    @Test
    @DisplayName("Find user by ID returns the user when it exists")
    public void testFindById() throws Exception {

        // Arrange
        Long id = 1L;
        User user = buildUser(id, "john@example.com", "John", "Doe", "$2a$10$hash", true);

        UserDTO expected = new UserDTO();
        expected.setId(id);
        expected.setEmail("john@example.com");
        expected.setName("John");
        expected.setSurname("Doe");
        expected.setEnabled(true);

        // Act
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        UserDTO obtained = userService.findById(id);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(userRepository).findById(id);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Find user by ID throws exception when user does not exist")
    public void testFindNonExistingById() {

        // Arrange
        Long id = 99L;

        // Act & Assert
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> userService.findById(id));
    }

    // ───────────────────────── list ─────────────────────────────

    @Test
    @DisplayName("List all users returns complete list")
    public void testList() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);
        User user2 = buildUser(2L, "jane@example.com", "Jane", "Smith", "$2a$10$h2", true);

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(2, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals("John", obtained.getContent().get(0).getName());
        assertEquals("Jane", obtained.getContent().get(1).getName());
    }

    @Test
    @DisplayName("List users with name filter")
    public void testListWithNameFilter() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setName("Joh");

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("John", obtained.getContent().get(0).getName());
    }

    @Test
    @DisplayName("List users with surname filter")
    public void testListWithSurnameFilter() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setSurname("Do");

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("Doe", obtained.getContent().get(0).getSurname());
    }

    @Test
    @DisplayName("List users with email filter")
    public void testListWithEmailFilter() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setEmail("john");

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("john@example.com", obtained.getContent().get(0).getEmail());
    }

    @Test
    @DisplayName("List users with enabled filter true")
    public void testListWithEnabledFilterTrue() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setEnabled(true);

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertTrue(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List users with enabled filter false")
    public void testListWithEnabledFilterFalse() {

        // Arrange
        User user1 = buildUser(1L, "inactive@example.com", "Inactive", "User", "$2a$10$h1", false);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setEnabled(false);

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertFalse(obtained.getContent().get(0).getEnabled());
    }

    @Test
    @DisplayName("List users with multiple filters (name + email)")
    public void testListWithMultipleFilters() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setName("John");
        filters.setEmail("john");

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(1, obtained.getTotalElements());
        assertEquals("John", obtained.getContent().get(0).getName());
        assertEquals("john@example.com", obtained.getContent().get(0).getEmail());
    }

    @Test
    @DisplayName("List users with no results")
    public void testListWithNoResults() {

        // Arrange
        Page<User> userPage = new PageImpl<>(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 20);
        UserFilterDTO filters = new UserFilterDTO();
        filters.setName("NoExiste");

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(0, obtained.getTotalElements());
        assertTrue(obtained.getContent().isEmpty());
    }

    @Test
    @DisplayName("List users with pagination")
    public void testListWithPagination() {

        // Arrange
        User user1 = buildUser(1L, "john@example.com", "John", "Doe", "$2a$10$h1", true);
        User user2 = buildUser(2L, "jane@example.com", "Jane", "Smith", "$2a$10$h2", true);

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 25);

        Pageable pageable = PageRequest.of(0, 10);
        UserFilterDTO filters = new UserFilterDTO();

        // Act
        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);

        Page<UserDTO> obtained = userService.list(filters, pageable);

        // Assert
        assertEquals(25, obtained.getTotalElements());
        assertEquals(2, obtained.getContent().size());
        assertEquals(3, obtained.getTotalPages());
        assertEquals(0, obtained.getNumber());
    }

    // ───────────────────────── update ───────────────────────────

    @Test
    @DisplayName("Update user modifies data fields without changing enabled or password")
    public void testUpdate() throws Exception {

        // Arrange
        Long id = 1L;
        User existingUser = buildUser(id, "old@example.com", "John", "Doe", "$2a$10$oldHash", true);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("new@example.com");
        updateDTO.setName("Johnny");
        updateDTO.setSurname("Doe");
        // no password in this update

        UserDTO expected = new UserDTO();
        expected.setId(id);
        expected.setEmail("new@example.com");
        expected.setName("Johnny");
        expected.setSurname("Doe");
        expected.setEnabled(true); // preserved from existing user

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO obtained = userService.update(id, updateDTO);

        // Assert
        assertEquals(expected, obtained);
        Mockito.verify(userRepository).findById(id);
        Mockito.verify(userRepository).save(existingUser);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Update user with new password hashes and stores the new password")
    public void testUpdateWithPassword() throws Exception {

        // Arrange
        Long id = 1L;
        User existingUser = buildUser(id, "john@example.com", "John", "Doe", "$2a$10$oldHash", true);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("john@example.com");
        updateDTO.setName("John");
        updateDTO.setSurname("Doe");
        updateDTO.setPassword("newPassword");

        String newHash = "$2a$10$newHashMocked";
        Mockito.when(passwordEncoder.encode("newPassword")).thenReturn(newHash);
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO obtained = userService.update(id, updateDTO);

        // Assert
        assertEquals("john@example.com", obtained.getEmail());
        assertTrue(obtained.getEnabled());
        assertNull(obtained.getPassword());
        Mockito.verify(passwordEncoder).encode("newPassword");
        Mockito.verify(userRepository).findById(id);
        Mockito.verify(userRepository).save(existingUser);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // ───────────────────────── delete ───────────────────────────

    @Test
    @DisplayName("Delete user removes it from the database")
    public void testDelete() throws Exception {

        // Arrange
        Long id = 1L;
        User user = buildUser(id, "john@example.com", "John", "Doe", "$2a$10$hash", true);

        UserDTO expected = new UserDTO();
        expected.setId(id);
        expected.setEmail("john@example.com");
        expected.setName("John");
        expected.setSurname("Doe");
        expected.setEnabled(true);

        // Act
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        UserDTO deleted = userService.delete(id);

        // Assert
        assertEquals(expected, deleted);
        Mockito.verify(userRepository).findById(id);
        Mockito.verify(userRepository).delete(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // ───────────────────────── enable / disable ─────────────────

    @Test
    @DisplayName("Disable user sets enabled flag to false")
    public void testDisable() throws Exception {

        // Arrange
        Long id = 1L;
        User user = buildUser(id, "john@example.com", "John", "Doe", "$2a$10$hash", true);

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO obtained = userService.disable(id);

        // Assert
        assertFalse(obtained.getEnabled());
        Mockito.verify(userRepository).findById(id);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Enable user sets enabled flag to true")
    public void testEnable() throws Exception {

        // Arrange
        Long id = 1L;
        User user = buildUser(id, "john@example.com", "John", "Doe", "$2a$10$hash", false);

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO obtained = userService.enable(id);

        // Assert
        assertTrue(obtained.getEnabled());
        Mockito.verify(userRepository).findById(id);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}
