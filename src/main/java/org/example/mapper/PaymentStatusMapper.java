package org.example.mapper;

import org.example.dto.payment.PaymentStatusDto;
import org.example.model.payment.PaymentStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentStatusMapper {

    PaymentStatusDto toDto(PaymentStatus paymentStatus);
}
