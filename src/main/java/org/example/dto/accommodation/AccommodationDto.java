package org.example.dto.accommodation;

import lombok.Data;
import org.example.dto.address.AddressDto;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class AccommodationDto {
    private Long id;
    private Long typeId;
    private String typeName;
    private AddressDto addressDto;
    private Set<AmenityTypeDto> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
    private String sizeType;
}
