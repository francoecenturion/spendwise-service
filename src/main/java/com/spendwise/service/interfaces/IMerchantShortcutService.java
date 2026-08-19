package com.spendwise.service.interfaces;

import com.spendwise.dto.MerchantShortcutDTO;
import com.spendwise.dto.MerchantShortcutFilterDTO;
import com.spendwise.model.MerchantShortcut;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMerchantShortcutService {

    void populate(MerchantShortcut merchantShortcut, MerchantShortcutDTO dto);
    MerchantShortcutDTO create(MerchantShortcutDTO dto);
    MerchantShortcutDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<MerchantShortcutDTO> list(MerchantShortcutFilterDTO filters, Pageable pageable);
    MerchantShortcutDTO update(Long id, MerchantShortcutDTO dto) throws ChangeSetPersister.NotFoundException;
    MerchantShortcutDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    MerchantShortcutDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    MerchantShortcutDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
