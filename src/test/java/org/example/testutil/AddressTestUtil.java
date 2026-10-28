package org.example.testutil;

import org.example.model.accommodation.Address;

public class AddressTestUtil {

    public static Address getAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setDeleted(false);
        address.setCity("Test City");
        address.setFloor("Test floor");
        address.setCountry("Test country");
        address.setState("Test state");
        address.setStreet("Test street");
        address.setApartmentNumber("Test number");
        address.setHouseNumber("Test apartment number");
        address.setZipCode("Test zipcode");

        return address;
    }
}
