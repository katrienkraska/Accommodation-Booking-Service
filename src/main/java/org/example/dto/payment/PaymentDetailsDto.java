package org.example.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDetailsDto {
    private Long id;
    private PaymentStatusDto status;
    private Long bookingId;
    private BigDecimal amount;
}
