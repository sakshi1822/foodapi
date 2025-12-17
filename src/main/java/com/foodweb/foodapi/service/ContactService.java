package com.foodweb.foodapi.service;

import com.foodweb.foodapi.request.ContactRequest;

public interface ContactService {
    public void handleContactForm(ContactRequest request);
}
