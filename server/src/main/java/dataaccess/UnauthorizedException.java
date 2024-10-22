package dataaccess;

/**
 * Indicates that the provided authToken was invalid
 */
public class UnauthorizedException extends Exception{
  public UnauthorizedException(String message) {
    super(message);
  }
}