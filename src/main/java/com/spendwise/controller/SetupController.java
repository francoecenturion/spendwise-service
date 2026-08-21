package com.spendwise.controller;

import com.spendwise.dto.RecommendedCurrencyDTO;
import com.spendwise.dto.RecommendedEntityDTO;
import com.spendwise.dto.SetupRecommendationsDTO;
import com.spendwise.repository.RecommendedCurrencyRepository;
import com.spendwise.repository.RecommendedEntityRepository;
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
    private final ModelMapper modelMapper = new ModelMapper();

    public SetupController(RecommendedCurrencyRepository currencyRepo,
                           RecommendedEntityRepository entityRepo) {
        this.currencyRepo = currencyRepo;
        this.entityRepo = entityRepo;
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

        SetupRecommendationsDTO result = new SetupRecommendationsDTO();
        result.setCurrencies(currencies);
        result.setEntities(entities);
        return ResponseEntity.ok(result);
    }

}
