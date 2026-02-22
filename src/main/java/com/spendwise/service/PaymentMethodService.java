package com.spendwise.service;

import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.dto.PaymentMethodFilterDTO;
import com.spendwise.enums.PaymentMethodType;
import com.spendwise.model.PaymentMethod;
import com.spendwise.repository.PaymentMethodRepository;
import com.spendwise.service.interfaces.IPaymentMethodService;
import com.spendwise.spec.PaymentMethodEspecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodService implements IPaymentMethodService {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodService.class);

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public void populate(PaymentMethod paymentMethod, PaymentMethodDTO dto) {
        paymentMethod.setName(dto.getName());
        paymentMethod.setPaymentMethodType(PaymentMethodType.valueOf(dto.getPaymentMethodType()));
        paymentMethod.setIcon(dto.getIcon());
        paymentMethod.setIssuingEntity(dto.getIssuingEntity());
        paymentMethod.setBrand(dto.getBrand());
    }

    @Transactional
    @Override
    public PaymentMethodDTO create(PaymentMethodDTO dto) {
        PaymentMethod paymentMethod = new PaymentMethod();
        this.populate(paymentMethod, dto);
        paymentMethod.setEnabled(true);
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.debug("PaymentMethod with id {} created successfully", savedPaymentMethod.getId());
        return modelMapper.map(savedPaymentMethod, PaymentMethodDTO.class);
    }

    @Transactional
    @Override
    public PaymentMethodDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethod paymentMethod = find(id);
        log.debug("PaymentMethod with id {} read successfully", paymentMethod.getId());
        return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
    }

    @Override
    public Page<PaymentMethodDTO> list(PaymentMethodFilterDTO filters, Pageable pageable) {
        log.debug("Listing all payment methods");
        Specification<PaymentMethod> spec = PaymentMethodEspecification.withFilters(filters);
        return paymentMethodRepository.findAll(spec, pageable)
                .map(paymentMethod -> modelMapper.map(paymentMethod, PaymentMethodDTO.class));
    }

    @Transactional
    @Override
    public PaymentMethodDTO update(Long id, PaymentMethodDTO dto) throws ChangeSetPersister.NotFoundException {
        PaymentMethod paymentMethod = find(id);
        this.populate(paymentMethod, dto);
        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.debug("PaymentMethod with id {} updated successfully", paymentMethod.getId());
        return modelMapper.map(updatedPaymentMethod, PaymentMethodDTO.class);
    }

    @Transactional
    @Override
    public PaymentMethodDTO delete(Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethod paymentMethod = find(id);
        paymentMethodRepository.delete(paymentMethod);
        log.debug("PaymentMethod with id {} deleted successfully", paymentMethod.getId());
        return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
    }

    @Transactional
    @Override
    public PaymentMethodDTO disable(Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethod paymentMethod = find(id);
        paymentMethod.setEnabled(false);
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.debug("PaymentMethod with id {} disabled successfully", paymentMethod.getId());
        return modelMapper.map(savedPaymentMethod, PaymentMethodDTO.class);
    }

    @Transactional
    @Override
    public PaymentMethodDTO enable(Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethod paymentMethod = find(id);
        paymentMethod.setEnabled(true);
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.debug("Payment Method with id {} enabled successfully", paymentMethod.getId());
        return modelMapper.map(savedPaymentMethod, PaymentMethodDTO.class);
    }

    protected PaymentMethod find(Long id) throws ChangeSetPersister.NotFoundException {
        return paymentMethodRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
    }
}
