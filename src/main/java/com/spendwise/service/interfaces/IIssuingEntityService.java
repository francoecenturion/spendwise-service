package com.spendwise.service.interfaces;

import com.spendwise.dto.IssuingEntityDTO;
import com.spendwise.dto.IssuingEntityFilterDTO;
import com.spendwise.model.IssuingEntity;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IIssuingEntityService {

    void populate(IssuingEntity issuingEntity, IssuingEntityDTO dto);

    IssuingEntityDTO create(IssuingEntityDTO dto);

    IssuingEntityDTO findById(Long id) throws ChangeSetPersister.NotFoundException;

    Page<IssuingEntityDTO> list(IssuingEntityFilterDTO filters, Pageable pageable);

    IssuingEntityDTO update(Long id, IssuingEntityDTO dto) throws ChangeSetPersister.NotFoundException;

    IssuingEntityDTO delete(Long id) throws ChangeSetPersister.NotFoundException;

    IssuingEntityDTO disable(Long id) throws ChangeSetPersister.NotFoundException;

    IssuingEntityDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
