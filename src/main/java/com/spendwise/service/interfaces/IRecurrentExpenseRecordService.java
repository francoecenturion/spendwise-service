package com.spendwise.service.interfaces;

import com.spendwise.dto.RecurrentExpenseRecordDTO;
import com.spendwise.dto.RecurrentExpenseRecordFilterDTO;
import com.spendwise.model.RecurrentExpenseRecord;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRecurrentExpenseRecordService {

    void populate(RecurrentExpenseRecord record, RecurrentExpenseRecordDTO dto);
    RecurrentExpenseRecordDTO create(RecurrentExpenseRecordDTO dto);
    RecurrentExpenseRecordDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<RecurrentExpenseRecordDTO> list(RecurrentExpenseRecordFilterDTO filters, Pageable pageable);
    RecurrentExpenseRecordDTO cancel(Long id) throws ChangeSetPersister.NotFoundException;
    RecurrentExpenseRecordDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException;
    RecurrentExpenseRecordDTO delete(Long id) throws ChangeSetPersister.NotFoundException;

}
