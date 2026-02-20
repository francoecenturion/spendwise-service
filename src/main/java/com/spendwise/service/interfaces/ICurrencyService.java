package com.spendwise.service.interfaces;

import com.spendwise.dto.CurrencyDTO;
import com.spendwise.dto.CurrencyFilterDTO;
import com.spendwise.model.Currency;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICurrencyService {

    void populate(Currency category, CurrencyDTO dto);
    CurrencyDTO create(CurrencyDTO dto);
    CurrencyDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<CurrencyDTO> list(CurrencyFilterDTO categoryFilterDTO, Pageable pageable);
    CurrencyDTO update(Long id, CurrencyDTO dto) throws ChangeSetPersister.NotFoundException;
    CurrencyDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    CurrencyDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    CurrencyDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
