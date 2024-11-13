package client;

import client.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;
    static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @BeforeEach
    void setup() throws Exception {
        server.clearDB();
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterEach
    void cleanup() {
        server.clearDB();
    }

    @Test
    public void registerPositive() {
        assertTrue(facade.register("username", "password", "email"));
    }

    @Test
    public void registerNegative() {
        facade.register("username", "password", "email");
        assertFalse(facade.register("username", "password", "email"));
    }

    @Test
    public void loginPositive() {
        facade.register("username", "password", "email");
        assertTrue(facade.login("username", "password"));
    }

    @Test
    public void loginNegative() {
        facade.register("username", "password", "email");
        assertFalse(facade.login("username", "pass"));
    }

    @Test
    public void logoutPositive() {
        facade.register("username", "password", "email");
        assertTrue(facade.logout());
    }

    @Test
    public void logoutNegative() {
        assertFalse(facade.logout());
    }

    @Test
    public void createGamePositive() {
        facade.register("username", "password", "email");
        assertTrue(facade.createGame("gameName") >= 0);
    }

    @Test
    public void createGameNegative() {
        assertEquals(-1, facade.createGame("gameName"));
    }

    @Test
    public void listGamesPositive() {
        facade.register("username", "password", "email");
        facade.createGame("gameName");
        assertEquals(1, facade.listGames().size());
    }

    @Test
    public void listGamesNegative() {
        assertEquals(facade.listGames(), HashSet.newHashSet(8));
    }

    @Test
    public void joinGamePositive() {
        facade.register("username", "password", "email");
        int id = facade.createGame("gameName");
        assertTrue(facade.joinGame(id, "WHITE"));
    }

    @Test
    public void joinGameNegative() {
        facade.register("username", "password", "email");
        int id = facade.createGame("gameName");
        facade.joinGame(id, "WHITE");
        assertFalse(facade.joinGame(id, "WHITE"));
    }

}
