CREATE TABLE IF NOT EXISTS employees(
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    fullname VARCHAR(100),
    PRIMARY KEY (email)
    );

INSERT INTO employees (email, password, fullname) VALUES ('classta@email.edu', 'encrypted_password', 'TA CS122B');
