package passoff.server.serverTests;


import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import service.Service.GameService;

import java.util.HashSet;

public class GameServiceTest {

  static GameService gameService;
  static GameRep gameDAO;
  static AuthRep authDAO;

  static AuthData authData;


  @BeforeAll
  static void init() {
    gameDAO = new MemoryRep.MemoryGame();
    authDAO = new MemoryRep.MemoryAuth();
    gameService = new GameService(gameDAO, authDAO);

    authData = new AuthData("Username", "authToken");

    authDAO.addAuth(authData);
  }

  @BeforeEach
  void setup() {
    gameDAO.clear();
  }

  @Test
  @DisplayName("Create Valid Game")
  void createGameTestPositive() throws UnauthorizedException {
    int gameID1 = gameService.createGame(authData.authToken());
    Assertions.assertTrue(gameDAO.gameExists(gameID1));

    int gameID2 = gameService.createGame(authData.authToken());
    Assertions.assertNotEquals(gameID1, gameID2);
  }

  @Test
  @DisplayName("Create Invalid Game")
  void createGameTestNegative() throws UnauthorizedException {
    Assertions.assertThrows(UnauthorizedException.class, () -> gameService.createGame("badToken"));
  }

  @Test
  @DisplayName("Proper List Games")
  void listGamesTestPositive() throws UnauthorizedException {
    int gameID1 = gameService.createGame(authData.authToken());
    int gameID2 = gameService.createGame(authData.authToken());
    int gameID3 = gameService.createGame(authData.authToken());

    HashSet<GameData> expected = HashSet.newHashSet(8);
    expected.add(new GameData(gameID1, null, null, null, null));
    expected.add(new GameData(gameID2, null, null, null, null));
    expected.add(new GameData(gameID3, null, null, null, null));

    Assertions.assertEquals(expected, gameService.listGames(authData.authToken()));
  }

  @Test
  @DisplayName("Improper List Games")
  void listGamesTestNegative() {
    Assertions.assertThrows(UnauthorizedException.class, () -> gameService.listGames("badToken"));
  }

  @Test
  @DisplayName("Proper Join Game")
  void joinGameTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
    int gameID = gameService.createGame(authData.authToken());

    gameService.joinGame(authData.authToken(), gameID, "WHITE");

    GameData expectedGameData = new GameData(gameID, authData.username(), null, null, null);

    Assertions.assertEquals(expectedGameData, gameDAO.getGame(gameID));
  }

  @Test
  @DisplayName("Improper Join Game")
  void joinGameTestNegative() throws UnauthorizedException {
    int gameID = gameService.createGame(authData.authToken());
    Assertions.assertThrows(UnauthorizedException.class, () -> gameService.joinGame("badToken", gameID, "WHITE"));
    Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), 11111, "WHITE"));
    Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), gameID, "INVALID"));
  }

}