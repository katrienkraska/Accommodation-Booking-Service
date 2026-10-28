package org.example.testutil;

import org.example.dto.accommodation.AccommodationTypeDto;
import org.example.model.accommodation.AccommodationType;

public class AccommodationTypeTestUtil {

    public static AccommodationType getAccommodationType() {
        AccommodationType accommodationType = new AccommodationType();
        accommodationType.setId(1L);
        accommodationType.setName(AccommodationType.AccommodationTypeName.APARTMENT);

        return accommodationType;
    }

    public static AccommodationTypeDto getAccommodationTypeDto() {
        AccommodationTypeDto accommodationTypeDto = new AccommodationTypeDto();
        accommodationTypeDto.setId(1L);
        accommodationTypeDto.setName("APARTMENT");

        return accommodationTypeDto;
    }
}
