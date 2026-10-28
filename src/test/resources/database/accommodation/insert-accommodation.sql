INSERT INTO accommodation_types(id, name)
VALUES(1, 'HOUSE'), (2, 'APARTMENT');

INSERT INTO amenity_types(id, name)
VALUES(1, 'SWIMMING_POOL'), (2, 'GYM'), (3, 'PET_FRIENDLY'),
    (4, 'FREE_WIFI');

INSERT INTO addresses(id, country, state, city, street, house_number, apartment_number, floor, zip_code, is_deleted)
VALUES(1, 'Ukraine', 'Dnipropetrovska', 'Dnipro',
'Central Street', 10, 15, 2, '49000', false);

INSERT INTO accommodations(id, type_id, address_id, size_type, daily_rate, availability, is_deleted)
VALUES (1, 1, 1, 'LARGE', 120, 5, false);

INSERT INTO accommodations_amenity_types(accommodation_id, amenity_type_id)
VALUES(1, 1), (1, 2);