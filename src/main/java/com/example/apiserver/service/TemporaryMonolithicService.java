package com.example.apiserver.service;

import com.example.apiserver.AppConfig;
import com.example.apiserver.service.dao.CourseStudentRefsDAO;
import com.example.apiserver.service.dao.CoursesDAO;
import com.example.apiserver.service.dao.UsersDAO;
import com.example.apiserver.service.model.Course;
import com.example.apiserver.service.model.CourseStudentRef;
import com.example.apiserver.service.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;

public class TemporaryMonolithicService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryMonolithicService.class);

    private final DataSource dataSource;
    private final CoursesDAO coursesDAO;
    private final UsersDAO usersDAO;
    private final CourseStudentRefsDAO courseStudentRefsDAO;

    public TemporaryMonolithicService(final DataSource dataSource,
                                      final UsersDAO usersDAO,
                                      final CoursesDAO coursesDAO,
                                      final CourseStudentRefsDAO courseStudentRefsDAO) {
        this.dataSource = dataSource;
        this.usersDAO = usersDAO;
        this.coursesDAO = coursesDAO;
        this.courseStudentRefsDAO = courseStudentRefsDAO;
    }

    @Nullable
    public User getUserById(final String id) {
        try (final var connection = dataSource.getConnection()) {
            return usersDAO.getUserById(id, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting user by id.", e);
            throw new RuntimeException(e);
        }
    }

    public User createUser(final String id,
                           final String nfcCardId,
                           final String name,
                           final boolean isStudent,
                           final boolean isTeacher,
                           final String teacherId,
                           final String createdBy) {
        try (final var connection = dataSource.getConnection()) {
            final var now = Instant.now();
            final var result = usersDAO.createUser(
                    connection,
                    id,
                    nfcCardId,
                    name,
                    isStudent,
                    isTeacher,
                    teacherId,
                    AppConfig.SOURCE,
                    createdBy,
                    now);
            return User.of(
                    id,
                    nfcCardId,
                    name,
                    isStudent,
                    isTeacher,
                    teacherId,
                    AppConfig.SOURCE,
                    createdBy,
                    now.toEpochMilli(),
                    AppConfig.SOURCE,
                    createdBy,
                    now.toEpochMilli(),
                    false);
        } catch (final Exception e) {
            LOGGER.error("error creating user.", e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public User getUserByNfcCardId(final String nfcCardId) {
        try (final var connection = dataSource.getConnection()) {
            return usersDAO.getUserByNfcCardId(nfcCardId, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting user by nfc card id.", e);
            throw new RuntimeException(e);
        }
    }

    public List<User> getUsersByTeacherId(final String teacherId) {
        try (final var connection = dataSource.getConnection()) {
            return usersDAO.getUsersByTeacherIds(List.of(teacherId), false, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting users by teacher id.", e);
            throw new RuntimeException(e);
        }
    }

    public void updateUser(final String id,
                           @Nullable final String nfcCardId,
                           @Nullable final String name,
                           final String updatedBy) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            usersDAO.updateUser(id, nfcCardId, name, AppConfig.SOURCE, updatedBy, now, connection);
        } catch (final Exception e) {
            LOGGER.error("error updating user.", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteUsers(final List<String> ids,
                            final String deleter) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            usersDAO.deleteByIds(ids, AppConfig.SOURCE, deleter, now, connection);
        } catch (final Exception e) {
            LOGGER.error("error deleting users.", e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public Course getCourseById(final String id) {
        try (final var connection = dataSource.getConnection()) {
            return coursesDAO.getCourseById(id, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting course by id.", e);
            throw new RuntimeException(e);
        }
    }

    public List<Course> getCoursesByIds(final List<String> ids) {
        try (final var connection = dataSource.getConnection()) {
            return coursesDAO.getCoursesByIds(ids, false, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting course by ids.", e);
            throw new RuntimeException(e);
        }
    }

    public List<Course> getCoursesByTeacherId(final String teacherId) {
        try (final var connection = dataSource.getConnection()) {
            return coursesDAO.getCoursesByTeacherId(teacherId, false, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting courses by teacher id.", e);
            throw new RuntimeException(e);
        }
    }

    public Course createCourse(final String id,
                               final String name,
                               final String teacherId,
                               final String createdBy) {
        try (final var connection = dataSource.getConnection()) {
            final var now = Instant.now();
            final var res = coursesDAO.createCourse(
                    connection,
                    id,
                    name,
                    teacherId,
                    AppConfig.SOURCE,
                    createdBy,
                    now);
            return Course.of(
                    id,
                    name,
                    teacherId,
                    AppConfig.SOURCE,
                    createdBy,
                    now.toEpochMilli(),
                    AppConfig.SOURCE,
                    createdBy,
                    now.toEpochMilli(),
                    false);
        } catch (final Exception e) {
            LOGGER.error("error creating course.", e);
            throw new RuntimeException(e);
        }
    }

    public void updateCourse(final String id,
                             @Nullable final String name,
                             final String updatedBy) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            coursesDAO.updateCourse(id, name, AppConfig.SOURCE, updatedBy, now, connection);
        } catch (final Exception e) {
            LOGGER.error("error updating course.", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteCourses(final List<String> ids,
                              final String deleter) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            coursesDAO.deleteByIds(ids, AppConfig.SOURCE, deleter, now, connection);
        } catch (final Exception e) {
            LOGGER.error("error deleting courses.", e);
            throw new RuntimeException(e);
        }
    }

    public List<CourseStudentRef> getCourseStudentRefsByStudentIds(final List<String> studentIds) {
        try (final var connection = dataSource.getConnection()) {
            return courseStudentRefsDAO.getRefsByStudentIds(studentIds, false, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting course student refs by student ids.", e);
            throw new RuntimeException(e);
        }
    }

    public List<CourseStudentRef> getCourseStudentRefsByCourseIds(final List<String> studentIds) {
        try (final var connection = dataSource.getConnection()) {
            return courseStudentRefsDAO.getRefsByStudentIds(studentIds, false, connection);
        } catch (final Exception e) {
            LOGGER.error("error getting course student refs by student ids.", e);
            throw new RuntimeException(e);
        }
    }

    public void createCourseStudentRefs(final List<CourseStudentRef.CourseStudentRefCreation> refs,
                                        final String createdBy) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            courseStudentRefsDAO.createRefs(
                    connection,
                    refs,
                    AppConfig.SOURCE,
                    createdBy,
                    now);
        } catch (final Exception e) {
            LOGGER.error("error creating course student refs.", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteCourseStudentRefs(final List<String> ids,
                                        final String deleter) {
        final var now = Instant.now();
        try (final var connection = dataSource.getConnection()) {
            courseStudentRefsDAO.deleteByIds(ids, AppConfig.SOURCE, deleter, now, connection);
        } catch (final Exception e) {
            LOGGER.error("error deleting course student refs.", e);
            throw new RuntimeException(e);
        }
    }
}
