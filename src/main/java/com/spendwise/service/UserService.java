package com.spendwise.service;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.UserFilterDTO;
import com.spendwise.model.user.User;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.interfaces.IUserService;
import com.spendwise.spec.UserEspecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void populate(User user, UserDTO dto) {
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setProfilePicture(dto.getProfilePicture());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
    }

    @Transactional
    @Override
    public UserDTO create(UserDTO dto) {
        User user = new User();
        this.populate(user, dto);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        log.debug("User with id {} created successfully", savedUser.getId());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        User user = find(id);
        log.debug("User with id {} read successfully", user.getId());
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public Page<UserDTO> list(UserFilterDTO filters, Pageable pageable) {
        log.debug("Listing all users");
        Specification<User> spec = UserEspecification.withFilters(filters);
        return userRepository.findAll(spec, pageable)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Transactional
    @Override
    public UserDTO update(Long id, UserDTO dto) throws ChangeSetPersister.NotFoundException {
        User user = find(id);
        this.populate(user, dto);
        User updatedUser = userRepository.save(user);
        log.debug("User with id {} updated successfully", user.getId());
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        User user = find(id);
        userRepository.delete(user);
        log.debug("User with id {} deleted successfully", user.getId());
        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        User user = find(id);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        log.debug("User with id {} disabled successfully", user.getId());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        User user = find(id);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        log.debug("User with id {} enabled successfully", user.getId());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    protected User find(Long id) throws ChangeSetPersister.NotFoundException {
        return userRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }
}
