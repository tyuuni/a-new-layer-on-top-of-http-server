CREATE TABLE IF NOT EXISTS `users`
(
    `id`           VARCHAR(50)  NOT NULL,
    `nfcCardId`    VARCHAR(50)  NOT NULL COMMENT '',
    `name`         VARCHAR(100) NOT NULL DEFAULT '',
    `isStudent`    BOOL         NOT NULl DEFAULT false,
    `isTeacher`    BOOL         NOT NULl DEFAULT false,
    `teacherId`    VARCHAR(50)  NOT NULL DEFAULT '',
    creationSource INT          NOT NULL DEFAULT 0,
    createdBy      VARCHAR(50)  NOT NULL,
    createdAt      DATETIME     NOT NULL,
    updateSource   INT          NOT NULL DEFAULT 0,
    updatedBy      VARCHAR(50)  NOT NULL,
    updatedAt      DATETIME     NOT NULL,
    isDeleted      INT          NOT NULL DEFAULT 0,
    `comment`      VARCHAR(100) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE (`nfcCardId`),
    INDEX (`teacherId`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `courses`
(
    `id`           VARCHAR(50)  NOT NULL,
    `name`         VARCHAR(100) NOT NULL DEFAULT '',
    `teacherId`    VARCHAR(50)  NOT NULL DEFAULT '',
    creationSource INT          NOT NULL DEFAULT 0,
    createdBy      VARCHAR(50)  NOT NULL,
    createdAt      DATETIME     NOT NULL,
    updateSource   INT          NOT NULL DEFAULT 0,
    updatedBy      VARCHAR(50)  NOT NULL,
    updatedAt      DATETIME     NOT NULL,
    isDeleted      INT          NOT NULL DEFAULT 0,
    `comment`      VARCHAR(100) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    INDEX (`teacherId`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `course_student_refs`
(
    `id`           VARCHAR(50)  NOT NULL,
    `studentId`    VARCHAR(50)  NOT NULL,
    `courseId`     VARCHAR(50)  NOT NULL,
    creationSource INT          NOT NULL DEFAULT 0,
    createdBy      VARCHAR(50)  NOT NULL,
    createdAt      DATETIME     NOT NULL,
    updateSource   INT          NOT NULL DEFAULT 0,
    updatedBy      VARCHAR(50)  NOT NULL,
    updatedAt      DATETIME     NOT NULL,
    isDeleted      INT          NOT NULL DEFAULT 0,
    `comment`      VARCHAR(100) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    INDEX (`studentId`),
    INDEX (`courseId`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 ROW_FORMAT = DYNAMIC;