package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.Service.GameService;
import service.UserAuthService;

public class Server {

    UserRep userDAO;
    AuthRep authDAO;
    GameRep gameDAO;

    UserAuthService userAuthService;
    GameService gameService;

    UserAuthHandler userAuthHandler;
    Handler.GameHandler gameHandler;

    public Server() {

        userDAO = new MemoryRep.MemoryUser();
        authDAO = new MemoryRep.MemoryAuth();
        gameDAO = new MemoryRep.MemoryGame();

        userAuthService = new UserAuthService(userDAO, authDAO);
        gameService = new GameService(gameDAO);

        userAuthHandler = new UserAuthHandler(userAuthService);
        gameHandler = new Handler.GameHandler(gameService);

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.post("/user", userAuthHandler::register);
        Spark.delete("/db", this::clear);

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request req, Response resp) {

        try {
            userAuthService.clear();
            GameService.clear(gameDAO);

            resp.status(200);
            return "{}";
        }
        catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: %s\"}".formatted(new Gson().toJson(e.getMessage()));
        }


    }
}
