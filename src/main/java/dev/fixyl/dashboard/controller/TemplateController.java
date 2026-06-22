package dev.fixyl.dashboard.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dev.fixyl.dashboard.service.ProcessorService;

@Controller
public class TemplateController {

    private final ProcessorService processorService;

    public TemplateController(ProcessorService processorService) {
        this.processorService = processorService;
    }

    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("processor", processorService.getStatic());
        } catch (IOException _) { /* Don't do anything */ }

        return "index";
    }

}
