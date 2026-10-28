package org.example.mapper;

import org.example.dto.payment.PaymentDetailsDto;
import org.example.dto.payment.PaymentDto;
import org.example.model.payment.Payment;
import org.mapstruct.Mapper;
import org.example.config.MapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = {BookingMapper.class, PaymentStatusMapper.class})
public interface PaymentMapper {

    @Mapping(source = "status", target = "status")
    PaymentDetailsDto toDetailsDto(Payment payment);

    @Mapping(source = "status", target = "status")
    PaymentDto toDto(Payment payment);
}
