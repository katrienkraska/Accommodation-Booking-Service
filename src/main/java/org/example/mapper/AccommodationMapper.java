package org.example.mapper;

import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.model.accommodation.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {

    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.name", target = "typeName")
    @Mapping(source = "address", target = "addressDto")
    AccommodationDto toDto(Accommodation accommodation);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "sizeType", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "address", ignore = true)
    Accommodation toModel(AccommodationRequestDto accommodationRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    void updateFromDto(AccommodationRequestDto accommodationRequestDto,
                       @MappingTarget Accommodation accommodation);
}
