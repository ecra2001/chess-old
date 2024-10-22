package server;

import service.Service;
import spark.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.Service.GameService;
import service.Service.UserService;

public class Server {

    UserRep userDAO;
    AuthRep authDAO;
    GameRep gameDAO;

    Service.UserService userService;
    GameService gameService;

    Handler.UserHandler userHandler;
    Handler.GameHandler gameHandler;

    public Server() {

        userDAO = new MemoryRep.MemoryUser();
        authDAO = new MemoryRep.MemoryAuth();
        gameDAO = new MemoryRep.MemoryGame();

        userService = new Service.UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userHandler = new Handler.UserHandler(userService);
        gameHandler = new Handler.GameHandler(gameService);

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.delete("/db", this::clear);
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

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
            userService.clear();
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
