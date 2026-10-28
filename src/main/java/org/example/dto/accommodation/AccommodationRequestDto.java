package org.example.dto.accommodation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.example.dto.address.AddressRequestDto;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class AccommodationRequestDto {
    @NotNull
    @Positive
    private Long typeId;

    @NotNull
    private AddressRequestDto addressDto;

    @NotBlank
    private String sizeType;

    @NotEmpty
    private Set<Long> amenityTypeIds;

    @NotNull
    @Positive
    private BigDecimal dailyRate;

    @Positive
    @NotNull
    private Integer availability;
}
