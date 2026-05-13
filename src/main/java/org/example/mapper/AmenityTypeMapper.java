package org.example.mapper;

import org.example.dto.accommodation.AmenityTypeDto;
import org.example.model.accommodation.AmenityType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AmenityTypeMapper {

    AmenityTypeDto toDto(AmenityType amenityType);
}
