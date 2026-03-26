package com.spendwise.service;

import com.spendwise.dto.DebtDTO;
import com.spendwise.dto.ExpenseDTO;
import com.spendwise.dto.MailImportConfirmDTO;
import com.spendwise.dto.MailImportDTO;
import com.spendwise.dto.MailImportFilterDTO;
import com.spendwise.dto.MerchantBindingDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.enums.MailImportStatus;
import com.spendwise.model.Category;
import com.spendwise.model.MailImport;
import com.spendwise.model.MerchantBinding;
import com.spendwise.model.PaymentMethod;
import com.spendwise.model.auth.User;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.MailImportRepository;
import com.spendwise.repository.MerchantBindingRepository;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.service.interfaces.IDebtService;
import com.spendwise.service.interfaces.IExpenseService;
import com.spendwise.service.interfaces.IMailImportService;
import com.spendwise.spec.MailImportSpecification;
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

import java.time.LocalDate;

@Service
public class MailImportService implements IMailImportService {

    private static final Logger log = LoggerFactory.getLogger(MailImportService.class);

    private final MailImportRepository mailImportRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final MerchantBindingRepository merchantBindingRepository;
    private final IExpenseService expenseService;
    private final IDebtService debtService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public MailImportService(
            MailImportRepository mailImportRepository,
            CategoryRepository categoryRepository,
            PaymentMethodRepository paymentMethodRepository,
            MerchantBindingRepository merchantBindingRepository,
            IExpenseService expenseService,
            IDebtService debtService) {
        this.mailImportRepository = mailImportRepository;
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.merchantBindingRepository = merchantBindingRepository;
        this.expenseService = expenseService;
        this.debtService = debtService;
    }

    @Override
    public Page<MailImportDTO> list(MailImportFilterDTO filters, Pageable pageable) {
        Specification<MailImport> spec = MailImportSpecification.withFilters(filters, currentUser());
        return mailImportRepository.findAll(spec, pageable)
                .map(m -> modelMapper.map(m, MailImportDTO.class));
    }

    @Override
    public MailImportDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        return modelMapper.map(find(id), MailImportDTO.class);
    }

    @Transactional
    @Override
    public MailImportDTO confirm(Long id, MailImportConfirmDTO dto) throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = find(id);
        if (mailImport.getStatus() != MailImportStatus.PENDING) {
            throw new IllegalStateException("Only PENDING imports can be confirmed");
        }
        return confirmInternal(mailImport, dto, currentUser());
    }

    @Transactional
    @Override
    public void autoConfirmIfBound(Long mailImportId) {
        MailImport mailImport = mailImportRepository.findById(mailImportId).orElse(null);
        if (mailImport == null || mailImport.getParsedMerchant() == null) return;
        if (mailImport.getStatus() != MailImportStatus.PENDING) return;

        User user = mailImport.getUser();
        merchantBindingRepository.findByUserAndMerchantNameIgnoreCase(user, mailImport.getParsedMerchant())
                .ifPresent(binding -> {
                    try {
                        MailImportConfirmDTO dto = new MailImportConfirmDTO();
                        if (binding.getCategory() != null) dto.setCategoryId(binding.getCategory().getId());
                        if (binding.getPaymentMethod() != null) dto.setPaymentMethodId(binding.getPaymentMethod().getId());
                        dto.setDescription(binding.getDescription());
                        confirmInternal(mailImport, dto, user);
                        log.debug("Auto-confirmed MailImport {} for merchant '{}'", mailImportId, mailImport.getParsedMerchant());
                    } catch (Exception e) {
                        log.warn("Auto-confirm failed for MailImport {}: {}", mailImportId, e.getMessage());
                    }
                });
    }

    private MailImportDTO confirmInternal(MailImport mailImport, MailImportConfirmDTO dto, User user) throws ChangeSetPersister.NotFoundException {
        String description = dto.getDescription() != null ? dto.getDescription() : mailImport.getParsedMerchant();
        LocalDate date = mailImport.getParsedDate() != null ? mailImport.getParsedDate() : LocalDate.now();

        if (Boolean.TRUE.equals(mailImport.getParsedIsDebt())) {
            // ── Credit card payment → create Debt ───────────────────────────
            DebtDTO debtDTO = new DebtDTO();
            debtDTO.setDescription(description);
            debtDTO.setAmountInPesos(mailImport.getParsedAmount());
            debtDTO.setDate(date);
            debtDTO.setPersonal(true);
            debtDTO.setCreditor(mailImport.getSenderEntity());

            PaymentMethod debtPm = null;
            if (dto.getPaymentMethodId() != null) {
                debtPm = paymentMethodRepository.findByIdAndUser(dto.getPaymentMethodId(), user)
                        .orElseThrow(ChangeSetPersister.NotFoundException::new);
                PaymentMethodDTO pmDTO = new PaymentMethodDTO();
                pmDTO.setId(debtPm.getId());
                pmDTO.setName(debtPm.getName());
                debtDTO.setPaymentMethod(pmDTO);
            }

            debtService.create(debtDTO);
            saveBinding(mailImport, description, null, debtPm);
            mailImport.setStatus(MailImportStatus.CONFIRMED);
            MailImport saved = mailImportRepository.save(mailImport);
            log.debug("MailImport {} confirmed as Debt (creditor={})", mailImport.getId(), mailImport.getSenderEntity());
            return modelMapper.map(saved, MailImportDTO.class);
        }

        // ── Regular payment → create Expense ────────────────────────────────
        Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setDescription(description);
        expenseDTO.setAmountInPesos(mailImport.getParsedAmount());
        expenseDTO.setInputAmount(mailImport.getParsedAmount());
        expenseDTO.setDate(date);

        com.spendwise.dto.CategoryDTO categoryDTO = new com.spendwise.dto.CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        expenseDTO.setCategory(categoryDTO);

        PaymentMethod pm = null;
        if (dto.getPaymentMethodId() != null) {
            pm = paymentMethodRepository.findByIdAndUser(dto.getPaymentMethodId(), user)
                    .orElseThrow(ChangeSetPersister.NotFoundException::new);
            PaymentMethodDTO pmDTO = new PaymentMethodDTO();
            pmDTO.setId(pm.getId());
            pmDTO.setName(pm.getName());
            expenseDTO.setPaymentMethod(pmDTO);
        } else {
            expenseDTO.setPaymentMethod(new PaymentMethodDTO());
        }

        ExpenseDTO createdExpense = expenseService.create(expenseDTO);
        saveBinding(mailImport, description, category, pm);

        mailImport.setStatus(MailImportStatus.CONFIRMED);
        mailImport.setExpense(new com.spendwise.model.Expense());
        mailImport.getExpense().setId(createdExpense.getId());

        MailImport saved = mailImportRepository.save(mailImport);
        log.debug("MailImport {} confirmed, Expense {} created", mailImport.getId(), createdExpense.getId());
        return modelMapper.map(saved, MailImportDTO.class);
    }

    @Transactional
    @Override
    public MailImportDTO ignore(Long id) throws ChangeSetPersister.NotFoundException {
        MailImport mailImport = find(id);
        mailImport.setStatus(MailImportStatus.IGNORED);
        MailImport saved = mailImportRepository.save(mailImport);
        log.debug("MailImport {} ignored", id);
        return modelMapper.map(saved, MailImportDTO.class);
    }

    @Override
    public long getPendingCount() {
        return mailImportRepository.countByUserAndStatus(currentUser(), MailImportStatus.PENDING);
    }

    @Override
    public MerchantBindingDTO lookupBinding(String merchant) {
        return merchantBindingRepository
                .findByUserAndMerchantNameIgnoreCase(currentUser(), merchant)
                .map(b -> {
                    MerchantBindingDTO dto = new MerchantBindingDTO();
                    if (b.getCategory() != null) dto.setCategoryId(b.getCategory().getId());
                    if (b.getPaymentMethod() != null) dto.setPaymentMethodId(b.getPaymentMethod().getId());
                    dto.setDescription(b.getDescription());
                    return dto;
                })
                .orElse(null);
    }

    private void saveBinding(MailImport mailImport, String description, Category category, PaymentMethod pm) {
        if (mailImport.getParsedMerchant() == null) return;
        User user = currentUser();
        MerchantBinding binding = merchantBindingRepository
                .findByUserAndMerchantNameIgnoreCase(user, mailImport.getParsedMerchant())
                .orElseGet(() -> {
                    MerchantBinding b = new MerchantBinding();
                    b.setUser(user);
                    b.setMerchantName(mailImport.getParsedMerchant());
                    return b;
                });
        binding.setDescription(description);
        binding.setCategory(category);
        binding.setPaymentMethod(pm);
        merchantBindingRepository.save(binding);
        log.debug("MerchantBinding saved for merchant='{}'", mailImport.getParsedMerchant());
    }

    private MailImport find(Long id) throws ChangeSetPersister.NotFoundException {
        return mailImportRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(currentUser().getId()))
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
