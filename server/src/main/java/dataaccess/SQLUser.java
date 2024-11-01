package dataaccess;
import model.UserData;

import java.sql.SQLException;

public class SQLUser implements UserRep {
    public SQLUser() {
        try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("chess");
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS user (
                                    username VARCHAR(255) NOT NULL,
                                    password VARCHAR(255) NOT NULL,
                                    email VARCHAR(255),
                                    PRIMARY KEY (username)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
        @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                statement.setString(1, username);
                try (var results = statement.executeQuery()) {
                    results.next();
                    var password = results.getString("password");
                    var email = results.getString("email");
                    return new UserData(username, password, email);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("User not found: " + username);
        }
    }
    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO user (username, password, email) VALUES(?, ?, ?)")) {
                statement.setString(1, user.username());
                statement.setString(2, user.password());
                statement.setString(3, user.email());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("User already exists: " + user.username());
        }
    }
    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        return user.password().equals(password);
    }
    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE user")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException ignored) { }
    }
}