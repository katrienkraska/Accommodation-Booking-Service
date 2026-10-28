package org.example.repository;

import org.example.model.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {

    PaymentStatus findPaymentStatusByName(PaymentStatus.PaymentStatusName name);
}
