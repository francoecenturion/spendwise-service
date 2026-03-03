package com.spendwise.service;

import com.spendwise.dto.RecurrentExpenseRecordDTO;
import com.spendwise.dto.RecurrentExpenseRecordFilterDTO;
import com.spendwise.model.Expense;
import com.spendwise.model.RecurrentExpense;
import com.spendwise.model.RecurrentExpenseRecord;
import com.spendwise.model.auth.User;
import com.spendwise.repository.RecurrentExpenseRecordRepository;
import com.spendwise.service.interfaces.IRecurrentExpenseRecordService;
import com.spendwise.spec.RecurrentExpenseRecordSpecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RecurrentExpenseRecordService implements IRecurrentExpenseRecordService {

    private static final Logger log = LoggerFactory.getLogger(RecurrentExpenseRecordService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final RecurrentExpenseRecordRepository recordRepository;

    @Autowired
    public RecurrentExpenseRecordService(RecurrentExpenseRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public void populate(RecurrentExpenseRecord record, RecurrentExpenseRecordDTO dto) {
        record.setMonth(dto.getMonth());
        record.setYear(dto.getYear());
        record.setCancelled(dto.getCancelled());
        if (dto.getRecurrentExpense() != null) {
            record.setRecurrentExpense(modelMapper.map(dto.getRecurrentExpense(), RecurrentExpense.class));
        }
        if (dto.getExpense() != null) {
            record.setExpense(modelMapper.map(dto.getExpense(), Expense.class));
        }
    }

    @Transactional
    @Override
    public RecurrentExpenseRecordDTO create(RecurrentExpenseRecordDTO dto) {
        RecurrentExpenseRecord record = new RecurrentExpenseRecord();
        this.populate(record, dto);
        record.setUser(currentUser());
        RecurrentExpenseRecord saved = recordRepository.save(record);
        log.debug("RecurrentExpenseRecord with id {} created successfully", saved.getId());
        return modelMapper.map(saved, RecurrentExpenseRecordDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseRecordDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecord record = find(id);
        log.debug("RecurrentExpenseRecord with id {} read successfully", record.getId());
        return modelMapper.map(record, RecurrentExpenseRecordDTO.class);
    }

    @Override
    public Page<RecurrentExpenseRecordDTO> list(RecurrentExpenseRecordFilterDTO filters, Pageable pageable) {
        log.debug("Listing all recurrent expense records");
        Specification<RecurrentExpenseRecord> spec = RecurrentExpenseRecordSpecification.withFilters(filters, currentUser());
        return recordRepository.findAll(spec, pageable)
                .map(r -> modelMapper.map(r, RecurrentExpenseRecordDTO.class));
    }

    @Transactional
    @Override
    public RecurrentExpenseRecordDTO cancel(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecord record = find(id);
        record.setCancelled(true);
        recordRepository.save(record);
        log.debug("RecurrentExpenseRecord with id {} cancelled successfully", record.getId());
        return modelMapper.map(record, RecurrentExpenseRecordDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseRecordDTO uncancel(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecord record = find(id);
        record.setCancelled(false);
        record.setExpense(null);
        recordRepository.save(record);
        log.debug("RecurrentExpenseRecord with id {} uncancelled successfully", record.getId());
        return modelMapper.map(record, RecurrentExpenseRecordDTO.class);
    }

    @Transactional
    @Override
    public RecurrentExpenseRecordDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        RecurrentExpenseRecord record = find(id);
        recordRepository.delete(record);
        log.debug("RecurrentExpenseRecord with id {} deleted successfully", record.getId());
        return modelMapper.map(record, RecurrentExpenseRecordDTO.class);
    }

    protected RecurrentExpenseRecord find(Long id) throws ChangeSetPersister.NotFoundException {
        return recordRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
