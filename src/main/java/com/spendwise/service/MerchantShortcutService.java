package com.spendwise.service;

import com.spendwise.dto.MerchantShortcutDTO;
import com.spendwise.dto.MerchantShortcutFilterDTO;
import com.spendwise.model.Category;
import com.spendwise.model.MerchantShortcut;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.MerchantShortcutRepository;
import com.spendwise.service.interfaces.IMerchantShortcutService;
import com.spendwise.spec.MerchantShortcutEspecification;
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

import com.spendwise.model.auth.User;

@Service
public class MerchantShortcutService implements IMerchantShortcutService {

    private static final Logger log = LoggerFactory.getLogger(MerchantShortcutService.class);

    private final MerchantShortcutRepository merchantShortcutRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public MerchantShortcutService(MerchantShortcutRepository merchantShortcutRepository,
                                   CategoryRepository categoryRepository) {
        this.merchantShortcutRepository = merchantShortcutRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void populate(MerchantShortcut merchantShortcut, MerchantShortcutDTO dto) {
        merchantShortcut.setName(dto.getName());
        merchantShortcut.setIcon(dto.getIcon());

        if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            Category category = categoryRepository.findByIdAndUser(dto.getCategory().getId(), currentUser())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategory().getId()));
            merchantShortcut.setCategory(category);
        } else {
            merchantShortcut.setCategory(null);
        }
    }

    @Transactional
    @Override
    public MerchantShortcutDTO create(MerchantShortcutDTO dto) {
        MerchantShortcut merchantShortcut = new MerchantShortcut();
        this.populate(merchantShortcut, dto);
        merchantShortcut.setEnabled(true);
        merchantShortcut.setUser(currentUser());
        MerchantShortcut saved = merchantShortcutRepository.save(merchantShortcut);
        log.debug("MerchantShortcut with id {} created successfully", saved.getId());
        return modelMapper.map(saved, MerchantShortcutDTO.class);
    }

    @Transactional
    @Override
    public MerchantShortcutDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcut merchantShortcut = find(id);
        log.debug("MerchantShortcut with id {} read successfully", merchantShortcut.getId());
        return modelMapper.map(merchantShortcut, MerchantShortcutDTO.class);
    }

    @Override
    public Page<MerchantShortcutDTO> list(MerchantShortcutFilterDTO filters, Pageable pageable) {
        log.debug("Listing all merchant shortcuts");
        Specification<MerchantShortcut> spec = MerchantShortcutEspecification.withFilters(filters, currentUser());
        return merchantShortcutRepository.findAll(spec, pageable)
                .map(merchantShortcut -> modelMapper.map(merchantShortcut, MerchantShortcutDTO.class));
    }

    @Transactional
    @Override
    public MerchantShortcutDTO update(Long id, MerchantShortcutDTO dto) throws ChangeSetPersister.NotFoundException {
        MerchantShortcut merchantShortcut = find(id);
        this.populate(merchantShortcut, dto);
        MerchantShortcut updated = merchantShortcutRepository.save(merchantShortcut);
        log.debug("MerchantShortcut with id {} updated successfully", merchantShortcut.getId());
        return modelMapper.map(updated, MerchantShortcutDTO.class);
    }

    @Transactional
    @Override
    public MerchantShortcutDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcut merchantShortcut = find(id);
        merchantShortcutRepository.delete(merchantShortcut);
        log.debug("MerchantShortcut with id {} deleted successfully", merchantShortcut.getId());
        return modelMapper.map(merchantShortcut, MerchantShortcutDTO.class);
    }

    @Transactional
    @Override
    public MerchantShortcutDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcut merchantShortcut = find(id);
        merchantShortcut.setEnabled(false);
        MerchantShortcut saved = merchantShortcutRepository.save(merchantShortcut);
        log.debug("MerchantShortcut with id {} disabled successfully", merchantShortcut.getId());
        return modelMapper.map(saved, MerchantShortcutDTO.class);
    }

    @Transactional
    @Override
    public MerchantShortcutDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        MerchantShortcut merchantShortcut = find(id);
        merchantShortcut.setEnabled(true);
        MerchantShortcut saved = merchantShortcutRepository.save(merchantShortcut);
        log.debug("MerchantShortcut with id {} enabled successfully", merchantShortcut.getId());
        return modelMapper.map(saved, MerchantShortcutDTO.class);
    }

    protected MerchantShortcut find(Long id) throws ChangeSetPersister.NotFoundException {
        return merchantShortcutRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
