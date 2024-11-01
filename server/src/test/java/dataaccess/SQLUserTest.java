package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserTest {

  private SQLUser sqlUser;

  @BeforeEach
  void setUp() throws DataAccessException {
    sqlUser = new SQLUser();
    sqlUser.clear();
  }

  @Test
  void createUserTestPositive() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    assertDoesNotThrow(() -> sqlUser.createUser(user));

    UserData retrievedUser = sqlUser.getUser("testuser");
    assertEquals("testuser", retrievedUser.username());
    assertEquals("testuser@example.com", retrievedUser.email());
  }

  @Test
  void createUserTestNegative() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    assertDoesNotThrow(() -> sqlUser.createUser(user));

    // Attempt to create the same user again, expecting an exception
    assertThrows(DataAccessException.class, () -> sqlUser.createUser(user));
  }

  @Test
  void getUserTestPositive() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    sqlUser.createUser(user);

    UserData retrievedUser = sqlUser.getUser("testuser");
    assertNotNull(retrievedUser);
    assertEquals("testuser", retrievedUser.username());
    assertEquals("testuser@example.com", retrievedUser.email());
  }

  @Test
  void getUserTestNegative() {
    assertThrows(DataAccessException.class, () -> sqlUser.getUser("nonexistentuser"));
  }

  @Test
  void authenticateUserTestPositive() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    sqlUser.createUser(user);

    assertTrue(sqlUser.authenticateUser("testuser", "password123"));
  }

  @Test
  void authenticateUserTestNegative() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    sqlUser.createUser(user);

    assertFalse(sqlUser.authenticateUser("testuser", "wrongpassword"));
  }

  @Test
  void clearTest() throws DataAccessException {
    UserData user = new UserData("testuser", "password123", "testuser@example.com");
    sqlUser.createUser(user);

    sqlUser.clear();
    assertThrows(DataAccessException.class, () -> sqlUser.getUser("testuser"));
  }
}