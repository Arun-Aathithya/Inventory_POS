package com.inventory.pos.controller;

import com.inventory.pos.dto.ReorderSuggestionDto;
import com.inventory.pos.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// "AI Sales Prediction" — reorder recommendations from the enhancement doc.
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/reorder")
    public List<ReorderSuggestionDto> reorderSuggestions() {
        return predictionService.getReorderSuggestions();
    }
}
