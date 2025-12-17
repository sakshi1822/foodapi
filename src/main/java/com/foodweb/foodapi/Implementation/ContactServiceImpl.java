package com.foodweb.foodapi.Implementation;

import com.foodweb.foodapi.repository.ContactRepository;
import com.foodweb.foodapi.request.ContactRequest;
import com.foodweb.foodapi.service.ContactService;
import com.foodweb.foodapi.entity.Contact;
import com.foodweb.foodapi.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository repo;

    @Autowired
    private EmailService emailService;

    public void handleContactForm(ContactRequest req) {

        Contact entity = new Contact();
        entity.setFirstName(req.getFirstName());
        entity.setLastName(req.getLastName());
        entity.setEmail(req.getEmail());
        entity.setMessage(req.getMessage());

        repo.save(entity);


        emailService.sendContactMessage("sakshiwadavkar7@gmail.com", req);
    }
}
