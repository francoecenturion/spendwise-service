package com.spendwise.service.interfaces;

import com.spendwise.dto.CategoryDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.model.Category;
import com.spendwise.model.PaymentMethod;

import java.util.List;

public interface IPaymentMethodService {

    void populate(PaymentMethod paymentMethod, PaymentMethodDTO dto);
    PaymentMethodDTO create(PaymentMethodDTO dto);
    PaymentMethodDTO findById(Long id);
    List<PaymentMethodDTO> list();
    PaymentMethodDTO update(Long id, PaymentMethodDTO dto);
    PaymentMethodDTO delete(Long id);
    PaymentMethodDTO disable(Long id);

}
