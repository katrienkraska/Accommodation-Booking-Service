package org.example.service.payment;

import org.example.dto.payment.PaymentDetailsDto;
import org.example.dto.payment.PaymentDto;
import java.util.List;

public interface PaymentService {
    String createPaymentCheckoutSession(Long id, String email);

    PaymentDto successPayment(String sessionId);

    PaymentDto cancelPaymentAndBooking(String sessionId);

    List<PaymentDetailsDto> findPaymentsByUserEmail(String email);

    List<PaymentDetailsDto> findAllPayments();
}
