package org.example.mapper;

import java.time.LocalDate;
import java.util.List;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.model.accommodation.Address;
import org.example.model.booking.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = {
                AccommodationMapper.class,
                UserMapper.class,
                BookingStatusMapper.class
        })
public interface BookingMapper {

    String DESCRIPTION = "Booking at %s, %s, %s %s %s from %s to %s";

    @Mapping(source = "accommodation.id", target = "accommodationId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    BookingDto toDto(Booking booking);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    Booking toModelWithoutStatusAndUser(CreateBookingRequest requestDto);

    void setUpdateInfoToBooking(@MappingTarget Booking booking, CreateBookingRequest requestDto);

    List<BookingDto> toDtos(List<Booking> bookingList);

    default String getBookingDescription(Booking booking) {
        Address address = booking.getAccommodation().getAddress();
        LocalDate checkInDate = booking.getCheckInDate();
        LocalDate checkOutDate = booking.getCheckOutDate();
        return DESCRIPTION.formatted(address.getCountry(), address.getState(),
                address.getCity(), address.getStreet(), address.getHouseNumber(),
                checkInDate, checkOutDate);
    }
}
