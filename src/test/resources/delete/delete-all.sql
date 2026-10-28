SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM payments;
DELETE FROM payment_statuses;
DELETE FROM bookings;
DELETE FROM accommodations_amenity_types;
DELETE FROM accommodations;
DELETE FROM addresses;
DELETE FROM amenity_types;
DELETE FROM accommodation_types;
DELETE FROM telegram_chats;
DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

SET FOREIGN_KEY_CHECKS = 1;