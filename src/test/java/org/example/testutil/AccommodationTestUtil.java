package org.example.testutil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.dto.accommodation.AmenityTypeDto;
import org.example.dto.address.AddressDto;
import org.example.dto.address.AddressRequestDto;
import org.example.model.accommodation.Accommodation;

public class AccommodationTestUtil {

    public static AccommodationRequestDto createAccommodationRequestDto() {

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setCountry("Ukraine");
        addressRequestDto.setState("Dnipropetrovska");
        addressRequestDto.setCity("Dnipro");
        addressRequestDto.setStreet("Central Street");
        addressRequestDto.setHouseNumber("10");
        addressRequestDto.setApartmentNumber("15");
        addressRequestDto.setFloor("2");
        addressRequestDto.setZipCode("49000");

        AccommodationRequestDto accommodationRequestDto =
                new AccommodationRequestDto();

        accommodationRequestDto.setTypeId(1L);
        accommodationRequestDto.setAddressDto(addressRequestDto);
        accommodationRequestDto.setSizeType("LARGE");
        accommodationRequestDto.setAmenityTypeIds(Set.of(1L, 2L));
        accommodationRequestDto.setDailyRate(BigDecimal.valueOf(120));
        accommodationRequestDto.setAvailability(5);

        return accommodationRequestDto;
    }

    public static AccommodationDto createExpectedAccommodationDto() {

        AddressDto address = new AddressDto();
        address.setCountry("Ukraine");
        address.setState("Dnipropetrovska");
        address.setCity("Dnipro");
        address.setStreet("Central Street");
        address.setHouseNumber("10");
        address.setApartmentNumber("15");
        address.setFloor("2");
        address.setZipCode("49000");

        AmenityTypeDto swimmingPool = new AmenityTypeDto();
        swimmingPool.setId(1L);
        swimmingPool.setName("SWIMMING_POOL");

        AmenityTypeDto gym = new AmenityTypeDto();
        gym.setId(2L);
        gym.setName("GYM");

        AccommodationDto dto = new AccommodationDto();
        dto.setId(1L);
        dto.setTypeId(1L);
        dto.setTypeName("HOUSE");
        dto.setAddressDto(address);
        dto.setAmenities(Set.of(swimmingPool, gym));
        dto.setDailyRate(BigDecimal.valueOf(120));
        dto.setAvailability(5);
        dto.setSizeType("LARGE");

        return dto;
    }

    public static AccommodationDto createUpdatedAccommodationDto() {

        AddressDto addressDto = new AddressDto();
        addressDto.setCountry("Ukraine");
        addressDto.setState("Kyivska");
        addressDto.setCity("Kyiv");
        addressDto.setStreet("Updated Street");
        addressDto.setHouseNumber("20");
        addressDto.setApartmentNumber("30");
        addressDto.setFloor("5");
        addressDto.setZipCode("01001");

        AmenityTypeDto petFriendly = new AmenityTypeDto();
        petFriendly.setId(3L);
        petFriendly.setName("PET_FRIENDLY");

        AmenityTypeDto freeWifi = new AmenityTypeDto();
        freeWifi.setId(4L);
        freeWifi.setName("FREE_WIFI");

        AccommodationDto accommodationDto = new AccommodationDto();

        accommodationDto.setId(1L);
        accommodationDto.setTypeId(2L);
        accommodationDto.setTypeName("APARTMENT");
        accommodationDto.setAddressDto(addressDto);
        accommodationDto.setAmenities(Set.of(petFriendly, freeWifi));
        accommodationDto.setDailyRate(BigDecimal.valueOf(250));
        accommodationDto.setAvailability(10);
        accommodationDto.setSizeType("MEDIUM");

        return accommodationDto;
    }

    public static AccommodationRequestDto createUpdatedAccommodationRequestDto() {

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setCountry("Ukraine");
        addressRequestDto.setState("Kyivska");
        addressRequestDto.setCity("Kyiv");
        addressRequestDto.setStreet("Updated Street");
        addressRequestDto.setHouseNumber("20");
        addressRequestDto.setApartmentNumber("30");
        addressRequestDto.setFloor("5");
        addressRequestDto.setZipCode("01001");

        AccommodationRequestDto accommodationRequestDto =
                new AccommodationRequestDto();

        accommodationRequestDto.setTypeId(2L);
        accommodationRequestDto.setAddressDto(addressRequestDto);
        accommodationRequestDto.setSizeType("MEDIUM");
        accommodationRequestDto.setAmenityTypeIds(Set.of(3L, 4L));
        accommodationRequestDto.setDailyRate(BigDecimal.valueOf(250));
        accommodationRequestDto.setAvailability(10);

        return accommodationRequestDto;
    }

    public static AccommodationDto getAccommodationDto() {
        AccommodationDto accommodationDto = new AccommodationDto();
        accommodationDto.setId(1L);
        accommodationDto.setTypeId(1L);
        accommodationDto.setTypeName("HOUSE");
        accommodationDto.setDailyRate(
                BigDecimal.valueOf(155).setScale(2, RoundingMode.HALF_UP));
        accommodationDto.setSizeType("Test size");
        accommodationDto.setAvailability(5);

        AddressDto addressDto = new AddressDto();
        addressDto.setCountry("Test country");
        addressDto.setState("Test state");
        addressDto.setCity("Test city");
        addressDto.setStreet("Test street");
        addressDto.setHouseNumber("Test house number");
        addressDto.setFloor("Test floor");
        addressDto.setApartmentNumber("Test apartment number");
        addressDto.setZipCode("Test zip code");

        accommodationDto.setAddressDto(addressDto);

        AmenityTypeDto amenityTypeDto = new AmenityTypeDto();
        amenityTypeDto.setId(1L);
        amenityTypeDto.setName("SWIMMING_POOL");

        AmenityTypeDto amenityTypeDto1 = new AmenityTypeDto();
        amenityTypeDto1.setId(2L);
        amenityTypeDto1.setName("GYM");

        accommodationDto.setAmenities(Set.of(amenityTypeDto, amenityTypeDto1));

        return accommodationDto;
    }

    public static AccommodationRequestDto createSampleAccommodationRequestDto() {
        AccommodationRequestDto accommodationRequestDto = new AccommodationRequestDto();
        accommodationRequestDto.setTypeId(1L);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setCountry("Ukraine");
        addressRequestDto.setState("Lviv Region");
        addressRequestDto.setCity("Lviv");
        addressRequestDto.setStreet("Patona");
        addressRequestDto.setHouseNumber("24");
        addressRequestDto.setFloor("2");
        addressRequestDto.setApartmentNumber("315");
        addressRequestDto.setZipCode("3356");

        accommodationRequestDto.setAddressDto(addressRequestDto);
        accommodationRequestDto.setSizeType("Studio");

        Set<Long> amenityTypeIds = new HashSet<>();
        amenityTypeIds.add(1L);
        amenityTypeIds.add(2L);
        accommodationRequestDto.setAmenityTypeIds(amenityTypeIds);

        accommodationRequestDto.setDailyRate(BigDecimal.valueOf(250.00).setScale(2, RoundingMode.HALF_UP));
        accommodationRequestDto.setAvailability(5);

        return accommodationRequestDto;
    }

    public static Accommodation getAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setSizeType("Test size");
        accommodation.setDailyRate(BigDecimal.valueOf(155, 2));
        accommodation.setDeleted(false);
        accommodation.setAvailability(5);
        accommodation.setType(AccommodationTypeTestUtil.getAccommodationType());
        accommodation.setAddress(AddressTestUtil.getAddress());
        accommodation.setAmenities(Set.of(AmenityTypeTestUtil.getAmenityType()));

        return accommodation;
    }
}
