package com.spendwise.service.interfaces;

import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.dto.PaymentMethodFilterDTO;
import com.spendwise.model.PaymentMethod;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPaymentMethodService {

    void populate(PaymentMethod paymentMethod, PaymentMethodDTO dto);
    PaymentMethodDTO create(PaymentMethodDTO dto);
    PaymentMethodDTO findById(Long id) throws ChangeSetPersister.NotFoundException;
    Page<PaymentMethodDTO> list(PaymentMethodFilterDTO filters, Pageable pageable);
    PaymentMethodDTO update(Long id, PaymentMethodDTO dto) throws ChangeSetPersister.NotFoundException;
    PaymentMethodDTO delete(Long id) throws ChangeSetPersister.NotFoundException;
    PaymentMethodDTO disable(Long id) throws ChangeSetPersister.NotFoundException;
    PaymentMethodDTO enable(Long id) throws ChangeSetPersister.NotFoundException;

}
