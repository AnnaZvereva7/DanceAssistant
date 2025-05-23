package com.example.ZverevaDanceWCS.service.model.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    public Optional<Payment> findById (int id);
    public List<Payment> findByStudentId (int studentId);
}
