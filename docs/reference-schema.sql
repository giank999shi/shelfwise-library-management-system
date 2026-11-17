-- REFERENCE ONLY. You do NOT need to run this: the app creates these tables by itself.
-- It's here so you can show the database design (matches the five tables in the system flow).
CREATE TABLE members (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  member_type VARCHAR(20) NOT NULL,          -- STUDENT / TEACHER
  active BOOLEAN NOT NULL
);
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,       -- BCrypt hash, never the real password
  role VARCHAR(20) NOT NULL,                 -- ADMIN / LIBRARIAN / TEACHER / STUDENT
  member_id BIGINT NULL,
  active BOOLEAN NOT NULL,
  FOREIGN KEY (member_id) REFERENCES members(id)
);
CREATE TABLE books (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  author VARCHAR(150) NOT NULL,
  total_copies INT NOT NULL,
  available_copies INT NOT NULL
);
CREATE TABLE loans (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  member_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  borrow_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE NULL,
  status VARCHAR(20) NOT NULL,               -- BORROWED / RETURNED
  FOREIGN KEY (member_id) REFERENCES members(id),
  FOREIGN KEY (book_id) REFERENCES books(id)
);
CREATE TABLE activity_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50),
  action VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL
);
