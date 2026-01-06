package com.example.apiserver.service.dao;

import com.example.apiserver.service.model.CourseStudentRef;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourseStudentRefsDAO {
    private static final String DEFAULT_SELECTION = "select id, studentId, courseId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt from course_student_refs";
    private static final String DEFAULT_INSERTION_COLUMNS = "insert into course_student_refs (id, studentId, courseId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt)";
    private static final String DEFAULT_INSERTION_VALUES = "( ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    static CourseStudentRef mapToModel(final ResultSet resultSet) throws SQLException {
        return CourseStudentRef.of(
                resultSet.getString("id"),
                resultSet.getString("studentId"),
                resultSet.getString("courseId"),
                resultSet.getInt("creationSource"),
                resultSet.getString("createdBy"),
                resultSet.getTimestamp("createdAt").getTime(),
                resultSet.getInt("updateSource"),
                resultSet.getString("updatedBy"),
                resultSet.getTimestamp("updatedAt").getTime()
        );
    }

    static List<CourseStudentRef> mapToModels(final ResultSet resultSet) throws SQLException {
        final List<CourseStudentRef> refs = new ArrayList<>();
        while (resultSet.next()) {
            refs.add(mapToModel(resultSet));
        }
        return refs;
    }

    public List<CourseStudentRef> getRefsByStudentIds(final List<String> studentIds, final boolean includeDeleted, final Connection connection) throws SQLException {
        if (studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        final String sql = String.format("%s WHERE studentId in (?%s) %s",
                DEFAULT_SELECTION,
                ", ?".repeat(studentIds.size() - 1),
                includeDeleted ? "" : "and isDeleted = 0");
        final var statement = connection.prepareStatement(sql);
        int paramIndex = 1;
        for (final String studentId : studentIds) {
            statement.setString(paramIndex++, studentId);
        }
        final ResultSet resultSet = statement.executeQuery();
        return mapToModels(resultSet);
    }

    public List<CourseStudentRef> getRefsByCourseIds(final List<String> courseIds, final boolean includeDeleted, final Connection connection) throws SQLException {
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        final String sql = String.format("%s WHERE courseId in (?%s) %s",
                DEFAULT_SELECTION,
                ", ?".repeat(courseIds.size() - 1),
                includeDeleted ? "" : "and isDeleted = 0");
        final var statement = connection.prepareStatement(sql);
        int paramIndex = 1;
        for (final String courseId : courseIds) {
            statement.setString(paramIndex++, courseId);
        }
        final ResultSet resultSet = statement.executeQuery();
        return mapToModels(resultSet);
    }

    public int deleteByIds(final List<String> ids,
                           final int updateSource,
                           final String updatedBy,
                           final Instant updatedAt,
                           final Connection connection) throws SQLException {
        if (ids.isEmpty()) {
            return 0;
        }
        final String sql = String.format("UPDATE `course_student_refs` SET isDeleted = 1, updateSource = ?, updatedBy = ?, updatedAt = ? WHERE isDeleted = 0 AND id IN (?%s)",
                ",?".repeat(ids.size() - 1));
        final var statement = connection.prepareStatement(sql);
        int paramIndex = 1;
        statement.setInt(paramIndex++, updateSource);
        statement.setString(paramIndex++, updatedBy);
        statement.setTimestamp(paramIndex++, Timestamp.from(updatedAt));
        for (final String id : ids) {
            statement.setString(paramIndex++, id);
        }
        return statement.executeUpdate();
    }

    public int createRefs(final Connection connection,
                          final List<CourseStudentRef.CourseStudentRefCreation> creations,
                          final int creationSource,
                          final String createdBy,
                          final Instant createdAt) throws SQLException {
        if (creations.isEmpty()) {
            return 0;
        }
        final String sql = String.format("%s VALUES %s%s",
                DEFAULT_INSERTION_COLUMNS,
                DEFAULT_INSERTION_VALUES,
                (", " + DEFAULT_INSERTION_VALUES).repeat(creations.size() - 1));
        final var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        final Timestamp timestamp = Timestamp.from(createdAt);
        int paramIndex = 1;
        for (final var creation : creations) {
            statement.setString(paramIndex++, creation.getId());
            statement.setString(paramIndex++, creation.getStudentId());
            statement.setString(paramIndex++, creation.getCourseId());
            statement.setInt(paramIndex++, creationSource);
            statement.setString(paramIndex++, createdBy);
            statement.setTimestamp(paramIndex++, timestamp);
            statement.setInt(paramIndex++, creationSource);
            statement.setString(paramIndex++, createdBy);
            statement.setTimestamp(paramIndex++, timestamp);
            statement.setInt(paramIndex++, 0);
        }
        return statement.executeUpdate();
    }
}