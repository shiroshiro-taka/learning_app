package com.example.learning_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.learning_app.entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {}
