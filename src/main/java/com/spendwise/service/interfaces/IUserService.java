package com.spendwise.service.interfaces;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.UserFilterDTO;
import com.spendwise.model.user.User;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    void populate(User user, UserDTO dto);
    UserDTO create(UserDTO dto);
    UserDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<UserDTO> list(UserFilterDTO filters, Pageable pageable);
    UserDTO update(Long id, UserDTO dto) throws ChangeSetPersister.NotFoundException;
    UserDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    UserDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    UserDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
