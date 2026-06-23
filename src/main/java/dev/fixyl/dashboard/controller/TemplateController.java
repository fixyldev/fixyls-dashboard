package dev.fixyl.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dev.fixyl.dashboard.service.processor.ProcessorService;

@Controller
public class TemplateController {

    private final ProcessorService processorService;

    public TemplateController(ProcessorService processorService) {
        this.processorService = processorService;
    }

    @GetMapping
    public String index(Model model) {
        processorService.getProcessor().ifPresent(processor ->
            model.addAttribute("processor", processor)
        );

        processorService.getUpdate().ifPresent(update ->
            model.addAttribute("processorUpdate", update)
        );

        return "index";
    }

}
