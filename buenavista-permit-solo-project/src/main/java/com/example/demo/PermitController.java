package com.example.demo;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PermitController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private LogStore logStore;

    @GetMapping("/")
    public String form(Model model) {
        model.addAttribute("logs", logStore.getLogs());
        return "index";
    }

    @PostMapping("/submit-permit")
    public String submit(@RequestParam String businessName,
                          @RequestParam String owner,
                          @RequestParam String barangay,
                          @RequestParam String location) {

        String message = "Business Permit Application: " + businessName
                + ", Owner: " + owner
                + ", Barangay: " + barangay
                + ", Location: " + location;

        producerTemplate.sendBody("seda:permitProcessingSystem", message);

        return "redirect:/";
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public List<String> apiLogs() {
        return logStore.getLogs();
    }
}
