package com.foodweb.foodapi.controller;

import com.foodweb.foodapi.request.ContactRequest;
import com.foodweb.foodapi.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private ContactService service;

    @PostMapping
    public ResponseEntity<String> submitContactMessage(@RequestBody ContactRequest req) {
        service.handleContactForm(req);
        return ResponseEntity.ok("Message sent successfully");
    }
}
