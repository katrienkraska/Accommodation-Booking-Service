package org.example.testutil;

import org.example.model.accommodation.AmenityType;

public class AmenityTypeTestUtil {

    public static AmenityType getAmenityType() {
        AmenityType amenityType = new AmenityType();
        amenityType.setId(1L);
        amenityType.setName(AmenityType.AmenityTypeName.SWIMMING_POOL);
        amenityType.setName(AmenityType.AmenityTypeName.GYM);

        return amenityType;
    }
}
