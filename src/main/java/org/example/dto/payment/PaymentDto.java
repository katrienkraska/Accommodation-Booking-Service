package org.example.dto.payment;

import lombok.Data;

@Data
public class PaymentDto {
    private Long id;
    private PaymentStatusDto status;
    private String sessionId;
}
