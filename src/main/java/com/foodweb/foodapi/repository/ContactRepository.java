package com.foodweb.foodapi.repository;

import com.foodweb.foodapi.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}

