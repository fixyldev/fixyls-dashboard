package dev.fixyl.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dev.fixyl.dashboard.service.ProcessorService;
import dev.fixyl.dashboard.service.SystemService;

@Controller
public class TemplateController {

    private final SystemService systemService;
    private final ProcessorService processorService;

    public TemplateController(SystemService systemService, ProcessorService processorService) {
        this.systemService = systemService;
        this.processorService = processorService;
    }

    @GetMapping
    public String index(Model model) {
        systemService.getSystem().ifPresent(system ->
            model.addAttribute("system", system)
        );

        processorService.getProcessor().ifPresent(processor ->
            model.addAttribute("processor", processor)
        );

        processorService.getUpdate().ifPresent(update ->
            model.addAttribute("processorUpdate", update)
        );

        return "index";
    }

}
