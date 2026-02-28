package com.spendwise.service;

import com.spendwise.dto.IssuingEntityDTO;
import com.spendwise.dto.IssuingEntityFilterDTO;
import com.spendwise.model.IssuingEntity;
import com.spendwise.repository.IssuingEntityRepository;
import com.spendwise.service.interfaces.IIssuingEntityService;
import com.spendwise.spec.IssuingEntityEspecification;
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

import com.spendwise.model.user.User;

@Service
public class IssuingEntityService implements IIssuingEntityService {

    private static final Logger log = LoggerFactory.getLogger(IssuingEntityService.class);

    private final IssuingEntityRepository issuingEntityRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public IssuingEntityService(IssuingEntityRepository issuingEntityRepository) {
        this.issuingEntityRepository = issuingEntityRepository;
    }

    @Override
    public void populate(IssuingEntity issuingEntity, IssuingEntityDTO dto) {
        issuingEntity.setDescription(dto.getDescription());
    }

    @Transactional
    @Override
    public IssuingEntityDTO create(IssuingEntityDTO dto) {
        IssuingEntity issuingEntity = new IssuingEntity();
        this.populate(issuingEntity, dto);
        issuingEntity.setEnabled(true);
        issuingEntity.setUser(currentUser());
        IssuingEntity saved = issuingEntityRepository.save(issuingEntity);
        log.debug("IssuingEntity with id {} created successfully", saved.getId());
        return modelMapper.map(saved, IssuingEntityDTO.class);
    }

    @Transactional
    @Override
    public IssuingEntityDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntity issuingEntity = find(id);
        log.debug("IssuingEntity with id {} read successfully", issuingEntity.getId());
        return modelMapper.map(issuingEntity, IssuingEntityDTO.class);
    }

    @Override
    public Page<IssuingEntityDTO> list(IssuingEntityFilterDTO filters, Pageable pageable) {
        log.debug("Listing all issuing entities");
        Specification<IssuingEntity> spec = IssuingEntityEspecification.withFilters(filters, currentUser());
        return issuingEntityRepository.findAll(spec, pageable)
                .map(entity -> modelMapper.map(entity, IssuingEntityDTO.class));
    }

    @Transactional
    @Override
    public IssuingEntityDTO update(Long id, IssuingEntityDTO dto) throws ChangeSetPersister.NotFoundException {
        IssuingEntity issuingEntity = find(id);
        this.populate(issuingEntity, dto);
        IssuingEntity updated = issuingEntityRepository.save(issuingEntity);
        log.debug("IssuingEntity with id {} updated successfully", issuingEntity.getId());
        return modelMapper.map(updated, IssuingEntityDTO.class);
    }

    @Transactional
    @Override
    public IssuingEntityDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntity issuingEntity = find(id);
        issuingEntityRepository.delete(issuingEntity);
        log.debug("IssuingEntity with id {} deleted successfully", issuingEntity.getId());
        return modelMapper.map(issuingEntity, IssuingEntityDTO.class);
    }

    @Transactional
    @Override
    public IssuingEntityDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntity issuingEntity = find(id);
        issuingEntity.setEnabled(false);
        IssuingEntity saved = issuingEntityRepository.save(issuingEntity);
        log.debug("IssuingEntity with id {} disabled successfully", issuingEntity.getId());
        return modelMapper.map(saved, IssuingEntityDTO.class);
    }

    @Transactional
    @Override
    public IssuingEntityDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        IssuingEntity issuingEntity = find(id);
        issuingEntity.setEnabled(true);
        IssuingEntity saved = issuingEntityRepository.save(issuingEntity);
        log.debug("IssuingEntity with id {} enabled successfully", issuingEntity.getId());
        return modelMapper.map(saved, IssuingEntityDTO.class);
    }

    protected IssuingEntity find(Long id) throws ChangeSetPersister.NotFoundException {
        return issuingEntityRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
