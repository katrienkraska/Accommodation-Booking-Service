package org.example.mapper;

import org.example.dto.booking.BookingStatusDto;
import org.example.model.booking.BookingStatusName;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingStatusMapper {

    BookingStatusDto toDto(BookingStatusName statusName);
}
