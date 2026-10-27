INSERT INTO roles (id, role)
VALUES (1, 'ROLE_USER'),(2, 'ROLE_ADMIN');

INSERT INTO users (id, email, password, first_name, last_name, is_deleted)
VALUES (1, 'admin@example.com',
        '$2a$12$Fr7derozRohomkIKBFvPJuw7WfpwghiUf6S/KGHQrAWq9YY621PV2',
        'Admin', 'User', false);

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 2);

INSERT INTO users (id, email, password, first_name, last_name, is_deleted)
VALUES (2, 'user@example.com',
        '$2a$12$Fr7derozRohomkIKBFvPJuw7WfpwghiUf6S/KGHQrAWq9YY621PV2',
        'Kate', 'Kraska', false);

INSERT INTO users_roles (user_id, role_id)
VALUES (2, 1);