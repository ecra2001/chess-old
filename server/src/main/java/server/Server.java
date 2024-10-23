package server;

import service.Service;
import spark.*;
import dataaccess.*;
import service.Service.GameService;

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
        Spark.exception(BadRequestException.class, this::badRequestExceptionHandler);
        Spark.exception(UnauthorizedException.class, this::unauthorizedExceptionHandler);
        Spark.exception(Exception.class, this::genericExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request req, Response resp) {

        userService.clear();
        gameService.clear(gameDAO);

        resp.status(200);
        return "{}";
    }

    private void badRequestExceptionHandler(BadRequestException ex, Request req, Response resp) {
        resp.status(400);
        resp.body("{ \"message\": \"Error: bad request\" }");
    }

    private void unauthorizedExceptionHandler(UnauthorizedException ex, Request req, Response resp) {
        resp.status(401);
        resp.body("{ \"message\": \"Error: unauthorized\" }");
    }

    private void genericExceptionHandler(Exception ex, Request req, Response resp) {
        resp.status(500);
        resp.body("{ \"message\": \"Error: %s\" }".formatted(ex.getMessage()));
    }
}
