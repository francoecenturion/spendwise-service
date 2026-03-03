package com.spendwise.service.interfaces;

import com.spendwise.dto.SavingDTO;
import com.spendwise.dto.SavingFilterDTO;
import com.spendwise.model.Saving;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISavingService {

    void populate(Saving saving, SavingDTO dto);
    SavingDTO create(SavingDTO dto);
    SavingDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<SavingDTO> list(SavingFilterDTO filters, Pageable pageable);
    SavingDTO update(Long id, SavingDTO dto) throws ChangeSetPersister.NotFoundException;
    SavingDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    SavingDTO disable(Long id, SavingDTO dto) throws ChangeSetPersister.NotFoundException;

}
