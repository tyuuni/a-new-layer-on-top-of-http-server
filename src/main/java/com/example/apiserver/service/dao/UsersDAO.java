package com.example.apiserver.service.dao;

import com.example.apiserver.service.model.User;
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

public class UsersDAO {
    private final static String DEFAULT_SELECTION = "select id, nfcCardId, name, isStudent, isTeacher, teacherId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt, isDeleted from users";
    private final static String DEFAULT_INSERTION_COLUMNS = "insert into users ( id, nfcCardId, name, isStudent, isTeacher, teacherId, creationSource, createdBy, createdAt, updateSource, updatedBy, updatedAt )";
    private final static String DEFAULT_INSERTION_VALUES = "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    static User mapToModel(final ResultSet resultSet) throws SQLException {
        return User.of(
                resultSet.getString("id"),
                resultSet.getString("nfcCardId"),
                resultSet.getString("name"),
                resultSet.getBoolean("isStudent"),
                resultSet.getBoolean("isTeacher"),
                resultSet.getString("teacherId"),
                resultSet.getInt("creationSource"),
                resultSet.getString("createdBy"),
                resultSet.getTimestamp("createdAt").getTime(),
                resultSet.getInt("updateSource"),
                resultSet.getString("updatedBy"),
                resultSet.getTimestamp("updatedAt").getTime(),
                resultSet.getBoolean("isDeleted")
        );
    }

    static List<User> mapToModels(final ResultSet resultSet) throws SQLException {
        final var users = new ArrayList<User>();
        while (resultSet.next()) {
            users.add(mapToModel(resultSet));
        }
        return users;
    }

    @Nullable
    public User getUserById(final String id,
                            final Connection connection) throws SQLException {
        final var users = getUsersByIds(Collections.singletonList(id), false, connection);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

    public List<User> getUsersByIds(final List<String> ids,
                                    final boolean includeDeleted,
                                    final Connection connection) throws SQLException {
        if (ids.size() == 0) {
            return Collections.emptyList();
        }
        final var sql = String.format("%s WHERE id in (?%s) %s", DEFAULT_SELECTION, ", ?".repeat(ids.size() - 1), !includeDeleted ? "and isDeleted = 0" : "");
        final var statement = connection.prepareStatement(sql);
        int i = 1;
        for (final var id : ids) {
            statement.setString(i++, id);
        }
        final var resultSet = statement.executeQuery();
        return mapToModels(resultSet);
    }

    @Nullable
    public User getUserByNfcCardId(final String cardId,
                                   final Connection connection) throws SQLException {
        final var users = getUsersByNfcCardIds(Collections.singletonList(cardId), false, connection);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

    public List<User> getUsersByNfcCardIds(final List<String> cardIds,
                                           final boolean includeDeleted,
                                           final Connection connection) throws SQLException {
        if (cardIds.size() == 0) {
            return Collections.emptyList();
        }
        final var sql = String.format("%s WHERE nfcCardId in (?%s) %s", DEFAULT_SELECTION, ", ?".repeat(cardIds.size() - 1), !includeDeleted ? "and isDeleted = 0" : "");
        final var statement = connection.prepareStatement(sql);
        int i = 1;
        for (final var id : cardIds) {
            statement.setString(i++, id);
        }
        final var resultSet = statement.executeQuery();
        return mapToModels(resultSet);
    }

    public List<User> getUsersByTeacherIds(final List<String> teacherIds,
                                           final boolean includeDeleted,
                                           final Connection connection) throws SQLException {
        if (teacherIds.size() == 0) {
            return Collections.emptyList();
        }
        final var sql = String.format("%s WHERE teacherId in (?%s) %s", DEFAULT_SELECTION, ", ?".repeat(teacherIds.size() - 1), !includeDeleted ? "and isDeleted = 0" : "");
        final var statement = connection.prepareStatement(sql);
        int i = 1;
        for (final var id : teacherIds) {
            statement.setString(i++, id);
        }
        final var resultSet = statement.executeQuery();
        return mapToModels(resultSet);
    }


    public int createUser(final Connection connection,
                          final String id,
                          final String nfcCardId,
                          final String name,
                          final boolean isStudent,
                          final boolean isTeacher,
                          final String teacherId,
                          final int creationSource,
                          final String createdBy,
                          final Instant createdAt) throws SQLException {
        final var prepareStatement = connection.prepareStatement(String.format("%s VALUES %s", DEFAULT_INSERTION_COLUMNS, DEFAULT_INSERTION_VALUES), Statement.RETURN_GENERATED_KEYS);
        int ith = 1;
        prepareStatement.setString(ith++, id);
        prepareStatement.setString(ith++, nfcCardId);
        prepareStatement.setString(ith++, name);
        prepareStatement.setBoolean(ith++, isStudent);
        prepareStatement.setBoolean(ith++, isTeacher);
        prepareStatement.setString(ith++, teacherId);
        prepareStatement.setInt(ith++, creationSource);
        prepareStatement.setString(ith++, createdBy);
        prepareStatement.setTimestamp(ith++, Timestamp.from(createdAt));
        prepareStatement.setInt(ith++, creationSource);
        prepareStatement.setString(ith++, createdBy);
        prepareStatement.setTimestamp(ith++, Timestamp.from(createdAt));

        prepareStatement.executeUpdate();
        final var resultSet = prepareStatement.getGeneratedKeys();
        if (!resultSet.next()) {
            return 0;
        }
        return resultSet.getInt(1);
    }


    public int updateUser(final String id,
                          @Nullable final String nfcCardId,
                          @Nullable final String name,
                          final int updateSource,
                          final String updatedBy,
                          final Instant updatedAt,
                          final Connection connection) throws SQLException {
        if (null == nfcCardId && null == name) {
            return 0;
        }
        final var sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE `users` SET updateSource = ?, updatedBy = ?, updatedAt = ?");
        if (nfcCardId != null) {
            sqlBuilder.append(", nfcCardId = ?");
        }
        if (name != null) {
            sqlBuilder.append(", name = ?");
        }
        sqlBuilder.append(" WHERE id = ? AND isDeleted = 0");
        final var prepareStatement = connection.prepareStatement(sqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS);
        int ith = 1;
        prepareStatement.setInt(ith++, updateSource);
        prepareStatement.setString(ith++, updatedBy);
        prepareStatement.setTimestamp(ith++, Timestamp.from(updatedAt));
        if (nfcCardId != null) {
            prepareStatement.setString(ith++, nfcCardId);
        }
        if (name != null) {
            prepareStatement.setString(ith++, name);
        }
        prepareStatement.setString(ith++, id);
        final var result = prepareStatement.executeUpdate();
        return result;
    }

    public int deleteByIds(final List<String> ids,
                           final int source,
                           final String deleter,
                           final Instant deletedAt,
                           final Connection connection) throws SQLException {
        if (ids.size() == 0) {
            return 0;
        }
        final var sql = String.format("UPDATE `users` SET isDeleted = 1, updateSource = ?, updatedBy = ?, updatedAt = ? WHERE isDeleted = 0 AND id IN (?%s)", ",?".repeat(ids.size() - 1));
        final var prepareStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int ith = 1;
        prepareStatement.setInt(ith++, source);
        prepareStatement.setString(ith++, deleter);
        prepareStatement.setTimestamp(ith++, Timestamp.from(deletedAt));
        for (final var id : ids) {
            prepareStatement.setString(ith++, id);
        }
        final var result = prepareStatement.executeUpdate();
        return result;
    }

    public int createUsers(final Connection connection,
                           final List<User.UserCreation> creations,
                           final int creationSource,
                           final String createdBy,
                           final Instant createdAt) throws SQLException {
        if (creations.size() == 0) {
            return 0;
        }
        final var prepareStatement = connection.prepareStatement(
                String.format("%s VALUES %s%s", DEFAULT_INSERTION_COLUMNS, DEFAULT_INSERTION_VALUES, (", " + DEFAULT_INSERTION_VALUES).repeat(creations.size() - 1)),
                Statement.RETURN_GENERATED_KEYS);
        final var time = Timestamp.from(createdAt);
        int ith = 1;
        for (final var creation : creations) {
            prepareStatement.setString(ith++, creation.getId());
            prepareStatement.setInt(ith++, creation.getNfcCardId());
            prepareStatement.setString(ith++, creation.getName());
            prepareStatement.setBoolean(ith++, creation.isStudent());
            prepareStatement.setBoolean(ith++, creation.isTeacher());
            prepareStatement.setString(ith++, creation.getTeacherId());
            prepareStatement.setInt(ith++, creationSource);
            prepareStatement.setString(ith++, createdBy);
            prepareStatement.setTimestamp(ith++, time);
            prepareStatement.setInt(ith++, creationSource);
            prepareStatement.setString(ith++, createdBy);
            prepareStatement.setTimestamp(ith++, time);
        }
        final int rows = prepareStatement.executeUpdate();
        return rows;
    }


}
