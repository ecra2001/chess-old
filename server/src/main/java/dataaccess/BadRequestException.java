package dataaccess;

/**
 * Indicates bad request
 */
public class BadRequestException extends Exception {
  public BadRequestException(String message) {
    super(message);
  }
}