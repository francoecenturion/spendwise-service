package com.spendwise.controller;

import com.spendwise.dto.CategoryFilterDTO;
import com.spendwise.dto.PaymentMethodDTO;
import com.spendwise.dto.PaymentMethodFilterDTO;
import com.spendwise.service.interfaces.IPaymentMethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment-methods")
public class PaymentMethodController {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodController.class);

    private final IPaymentMethodService iPaymentMethodService;

    @Autowired
    public PaymentMethodController(IPaymentMethodService iPaymentMethodService) {
        this.iPaymentMethodService = iPaymentMethodService;
    }

    @PostMapping
    public ResponseEntity<PaymentMethodDTO> create(@RequestBody PaymentMethodDTO dto) {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.create(dto);
        log.debug("POST to Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> getById(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.findById(id);
        log.debug("GET to Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }

    @GetMapping
    public ResponseEntity<?> list(
        @ModelAttribute PaymentMethodFilterDTO filters,
        Pageable pageable
    ) {
        Page<PaymentMethodDTO> paymentMethods = iPaymentMethodService.list(filters, pageable);
        log.debug("LIST Payment Methods Finished");
        return ResponseEntity.ok(paymentMethods);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> update(@PathVariable Long id, @RequestBody PaymentMethodDTO dto) throws ChangeSetPersister.NotFoundException {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.update(id, dto);
        log.debug("PUT to Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> update(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.delete(id);
        log.debug("DELETE to Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<PaymentMethodDTO> disable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.disable(id);
        log.debug("DISABLE Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<PaymentMethodDTO> enable(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        PaymentMethodDTO paymentMethod = iPaymentMethodService.enable(id);
        log.debug("ENABLE Payment Method Finished {}", paymentMethod);
        return ResponseEntity
                .ok(paymentMethod);
    }
}
