
INSERT INTO "USER" (user_id, pseudo, email, password, role) VALUES
(1, 'admin', 'admin@example.com', '$2a$10$yfYxjCE8iO3oU5WzOGZ1POYaz5voVgpS/gVsK0aOi7f4xQwx7.qOq', 'ADMIN'),
(2, 'user1', 'user1@example.com', '$2a$10$yfYxjCE8iO3oU5WzOGZ1POYaz5voVgpS/gVsK0aOi7f4xQwx7.qOq', 'USER'),
(3, 'user2', 'user2@example.com', '$2a$10$yfYxjCE8iO3oU5WzOGZ1POYaz5voVgpS/gVsK0aOi7f4xQwx7.qOq', 'USER');

INSERT INTO "GROUP" (group_id, name, invit_code) VALUES
 (1, 'Famille', 'FAM12345'),
 (2, 'Projet A', 'PRJA2022'),
 (3, 'Étudiants', 'EDU98765');

INSERT INTO USER_GROUP (user_id, group_id, role) VALUES
 (1, 1, 'OWNER'),
 (1, 2, 'OWNER'),
 (2, 1, 'MEMBER'),
 (2, 3, 'OWNER'),
 (3, 2, 'MEMBER'),
 (3, 3, 'MEMBER');

INSERT INTO TASK (task_id, name, repetition) VALUES
 (1, 'Réunion', 'DAILY'),
 (2, 'Développement', 'DAILY'),
 (3, 'Révisions', 'WEEKLY'),
 (4, 'Sport', 'WEEKLY');

ALTER SEQUENCE IF EXISTS user_id_seq RESTART WITH 4;
ALTER SEQUENCE IF EXISTS group_id_seq RESTART WITH 4;
ALTER SEQUENCE IF EXISTS task_id_seq RESTART WITH 5;