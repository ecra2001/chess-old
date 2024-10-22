package service;

import dataaccess.AuthRep;
import dataaccess.DataAccessException;
import dataaccess.UserRep;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserAuthService {

  UserRep userDAO;
  AuthRep authDAO;

  public UserAuthService(UserRep userDAO, AuthRep authDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
  }

  public AuthData createUser(UserData userData) throws DataAccessException {
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(userData.username(), authToken);

    userDAO.createUser(userData);
    authDAO.addAuth(authData);

    return authData;
  }

  public void clear() {
    userDAO.clear();
    authDAO.clear();
  }
}