SET @teacher1_id = (REPLACE(UUID(), '-', ''));
SET @teacher2_id = (REPLACE(UUID(), '-', ''));
SET @student1_id = (REPLACE(UUID(), '-', ''));
SET @student2_id = (REPLACE(UUID(), '-', ''));
SET @student3_id = (REPLACE(UUID(), '-', ''));
SET @course1_id = (REPLACE(UUID(), '-', ''));
SET @course2_id = (REPLACE(UUID(), '-', ''));

start transaction;

insert into users ( id, nfcCardId, name, isStudent, isTeacher, teacherId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt )
VALUES (@teacher1_id, "teacher1", "teacher 1", false, true, "", -1,  "initial data", NOW(), -1, "initial data", NOW()),
       (@teacher2_id, "teacher2", "teacher 2", false, true, "", -1,  "initial data", NOW(), -1, "initial data", NOW());


insert into users ( id, nfcCardId, name, isStudent, isTeacher, teacherId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt )
    VALUES (@student1_id, "student1", "student 1", true, false, @teacher1_id, -1,  "initial data", NOW(), -1, "initial data", NOW()),
           (@student2_id, "student2", "student 2", true, false, @teacher1_id, -1,  "initial data", NOW(), -1, "initial data", NOW()),
           (@student3_id, "student3", "student 3", true, false, @teacher2_id, -1,  "initial data", NOW(), -1, "initial data", NOW());

insert into courses ( id, name, teacherId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt )
    VALUES (@course1_id, "course 1", @teacher1_id, -1,  "initial data", NOW(), -1, "initial data", NOW()),
           (@course2_id, "course 2", @teacher2_id, -1,  "initial data", NOW(), -1, "initial data", NOW());

insert into course_student_refs (id, studentId, courseId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt)
    VALUES (REPLACE(UUID(), '-', ''), @student1_id, @course1_id, -1,  "initial data", NOW(), -1, "initial data", NOW()),
           (REPLACE(UUID(), '-', ''), @student2_id, @course1_id, -1,  "initial data", NOW(), -1, "initial data", NOW()),
           (REPLACE(UUID(), '-', ''), @student3_id, @course2_id, -1,  "initial data", NOW(), -1, "initial data", NOW());
commit;
