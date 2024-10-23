package service;


import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.Service.UserService;

public class UserServiceTest {

  static UserService userService;
  static UserRep userDAO;
  static AuthRep authDAO;
  static UserData defaultUser;


  @BeforeAll
  static void init() {
    userDAO = new MemoryRep.MemoryUser();
    authDAO = new MemoryRep.MemoryAuth();
    userService = new UserService(userDAO, authDAO);
  }

  @BeforeEach
  void setup() {
    userDAO.clear();
    authDAO.clear();

    defaultUser = new UserData("Username", "password", "email");
  }

  @Test
  @DisplayName("Create Valid User")
  void newUserTestPositive() throws BadRequestException, DataAccessException {
    AuthData resultAuth = userService.createUser(defaultUser);
    Assertions.assertEquals(authDAO.getAuth(resultAuth.authToken()), resultAuth);
  }

  @Test
  @DisplayName("Create Invalid User")
  void newUserTestNegative() throws BadRequestException {
    userService.createUser(defaultUser);
    Assertions.assertThrows(BadRequestException.class, () -> userService.createUser(defaultUser));
  }

  @Test
  @DisplayName("Proper Login User")
  void existingUserTestPositive() throws BadRequestException, UnauthorizedException, DataAccessException {
    userService.createUser(defaultUser);
    AuthData authData = userService.loginUser(defaultUser);
    Assertions.assertEquals(authDAO.getAuth(authData.authToken()), authData);
  }

  @Test
  @DisplayName("Improper Login User")
  void existingUserTestNegative() throws BadRequestException {
    Assertions.assertThrows(UnauthorizedException.class, () -> userService.loginUser(defaultUser));

    userService.createUser(defaultUser);
    UserData badPassUser = new UserData(defaultUser.username(), "wrongPass", defaultUser.email());
    Assertions.assertThrows(UnauthorizedException.class, () -> userService.loginUser(badPassUser));
  }

  @Test
  @DisplayName("Proper Logout User")
  void logoutUserTestPositive() throws BadRequestException, UnauthorizedException {
    AuthData auth = userService.createUser(defaultUser);
    userService.logoutUser(auth.authToken());
    Assertions.assertThrows(DataAccessException.class, () -> authDAO.getAuth(auth.authToken()));
  }

  @Test
  @DisplayName("Improper Logout User")
  void logoutUserTestNegative() throws BadRequestException {
    AuthData auth = userService.createUser(defaultUser);
    Assertions.assertThrows(UnauthorizedException.class, () -> userService.logoutUser("badAuthToken"));
  }

  @Test
  @DisplayName("Proper Clear DB")
  void clearTestPositive() throws BadRequestException {
    AuthData auth = userService.createUser(defaultUser);
    userService.clear();
    Assertions.assertThrows(DataAccessException.class, () -> userDAO.getUser(defaultUser.username()));
    Assertions.assertThrows(DataAccessException.class, () -> authDAO.getAuth(auth.authToken()));
  }

}