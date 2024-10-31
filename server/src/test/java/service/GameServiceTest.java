package service;


import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
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
  void addGameTestPositive() throws UnauthorizedException {
    int game1 = gameService.createGame(authData.authToken(), "name");
    Assertions.assertTrue(gameDAO.gameExists(game1));

    int game2 = gameService.createGame(authData.authToken(), "name");
    Assertions.assertNotEquals(game1, game2);
  }

  @Test
  @DisplayName("Create Invalid Game")
  void addGameTestNegative() throws UnauthorizedException {
    Assertions.assertThrows(UnauthorizedException.class, () -> gameService.createGame("badToken", "name"));
  }

  @Test
  @DisplayName("Proper List Games")
  void listGamesTestPositive() throws UnauthorizedException {
    int gameID1 = gameService.createGame(authData.authToken(), "name");
    int gameID2 = gameService.createGame(authData.authToken(), "name");
    int gameID3 = gameService.createGame(authData.authToken(), "name");

    HashSet<GameData> expected = HashSet.newHashSet(8);
    expected.add(new GameData(gameID1, null, null, "name", null));
    expected.add(new GameData(gameID2, null, null, "name", null));
    expected.add(new GameData(gameID3, null, null, "name", null));

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
    int game = gameService.createGame(authData.authToken(), "name");

    gameService.joinGame(authData.authToken(), game, "WHITE");

    GameData expectedGameData = new GameData(game, authData.username(), null, "name", null);

    Assertions.assertEquals(expectedGameData, gameDAO.getGame(game));
  }

  @Test
  @DisplayName("Improper Join Game")
  void joinGameTestNegative() throws UnauthorizedException {
    int game = gameService.createGame(authData.authToken(), "name");
    Assertions.assertThrows(UnauthorizedException.class, () -> gameService.joinGame("badToken", game, "WHITE"));
    Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), 11111, "WHITE"));
    Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), game, "INVALID"));
  }

  @Test
  @DisplayName("Proper Clear DB")
  void clearTestPositive() throws UnauthorizedException {
    gameService.createGame(authData.authToken(), "name");
    gameService.clear(gameDAO);
    Assertions.assertEquals(gameDAO.listGames(), HashSet.newHashSet(16));
  }

}