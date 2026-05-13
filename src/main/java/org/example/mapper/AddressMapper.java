package org.example.mapper;

import org.example.dto.address.AddressDto;
import org.example.dto.address.AddressRequestDto;
import org.example.model.accommodation.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(AddressRequestDto dto);

    AddressDto toDto(Address address);
}
