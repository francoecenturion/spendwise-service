package com.spendwise.controller;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.UserFilterDTO;
import com.spendwise.service.interfaces.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final IUserService iUserService;

    @Autowired
    public UserController(IUserService iUserService) {
        this.iUserService = iUserService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO dto) {
        UserDTO user = iUserService.create(dto);
        log.debug("POST to User finished {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        UserDTO user = iUserService.findById(id);
        log.debug("GET to User finished {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @ModelAttribute UserFilterDTO filters,
            Pageable pageable
    ) {
        Page<UserDTO> users = iUserService.list(filters, pageable);
        log.debug("LIST Users finished");
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody UserDTO dto) throws ChangeSetPersister.NotFoundException {
        UserDTO user = iUserService.update(id, dto);
        log.debug("PUT to User finished {}", user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> delete(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        UserDTO user = iUserService.delete(id);
        log.debug("DELETE to User finished {}", user);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<UserDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        UserDTO user = iUserService.disable(id);
        log.debug("DISABLE User finished {}", user);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<UserDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        UserDTO user = iUserService.enable(id);
        log.debug("ENABLE User finished {}", user);
        return ResponseEntity.ok(user);
    }
}
