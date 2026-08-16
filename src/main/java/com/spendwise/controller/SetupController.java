package com.spendwise.controller;

import com.spendwise.dto.RecommendedCurrencyDTO;
import com.spendwise.dto.RecommendedEntityDTO;
import com.spendwise.dto.RecommendedPaymentMethodDTO;
import com.spendwise.dto.SetupRecommendationsDTO;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
import com.spendwise.repository.RecommendedPaymentMethodRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/setup")
public class SetupController {

    private final RecommendedCurrencyRepository currencyRepo;
    private final RecommendedEntityRepository entityRepo;
    private final RecommendedPaymentMethodRepository pmRepo;
    private final ModelMapper modelMapper = new ModelMapper();

    public SetupController(RecommendedCurrencyRepository currencyRepo,
                           RecommendedEntityRepository entityRepo,
                           RecommendedPaymentMethodRepository pmRepo) {
        this.currencyRepo = currencyRepo;
        this.entityRepo = entityRepo;
        this.pmRepo = pmRepo;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<SetupRecommendationsDTO> getRecommendations() {
        List<RecommendedCurrencyDTO> currencies = currencyRepo.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(c -> modelMapper.map(c, RecommendedCurrencyDTO.class))
                .toList();

        List<RecommendedEntityDTO> entities = entityRepo.findAllByOrderByIdAsc()
                .stream()
                .map(e -> modelMapper.map(e, RecommendedEntityDTO.class))
                .toList();

        List<RecommendedPaymentMethodDTO> paymentMethods = pmRepo.findAllByOrderByIdAsc()
                .stream()
                .map(pm -> {
                    RecommendedPaymentMethodDTO dto = modelMapper.map(pm, RecommendedPaymentMethodDTO.class);
                    if (pm.getEntity() != null) dto.setRecommendedEntityId(pm.getEntity().getId());
                    dto.setPaymentMethodType(pm.getPaymentMethodType().name());
                    return dto;
                })
                .toList();

        SetupRecommendationsDTO result = new SetupRecommendationsDTO();
        result.setCurrencies(currencies);
        result.setEntities(entities);
        result.setPaymentMethods(paymentMethods);
        return ResponseEntity.ok(result);
    }

}
