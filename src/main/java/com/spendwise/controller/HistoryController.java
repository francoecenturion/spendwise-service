package com.spendwise.controller;

import com.spendwise.dto.HistorySummaryDTO;
import com.spendwise.service.interfaces.IHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final IHistoryService historyService;

    @Autowired
    public HistoryController(IHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/summary")
    public ResponseEntity<HistorySummaryDTO> getSummary() {
        return ResponseEntity.ok(historyService.getSummary());
    }
}
