package model;


public record AuthData(String username, String authToken) {
  public String getAuthToken() {
    return authToken;
  }
}