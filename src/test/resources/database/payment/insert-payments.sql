INSERT INTO payment_statuses (id, name)
VALUES (1, 'PAID'), (2, 'PENDING');

INSERT INTO payments (id, payment_status_id, booking_id, session_url, session_id, amount, is_deleted)
VALUES (1, 1, 1, 'session/url', 'testSessionId1',
        3000, false);

INSERT INTO payments (id, payment_status_id, booking_id, session_url, session_id, amount, is_deleted)
VALUES (2, 2, 2, 'session/url', 'testSessionId2',
        4000, false);