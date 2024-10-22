package dataaccess;

/**
 * Indicates that the provided request was bad
 */
public class BadRequestException extends Exception {
  public BadRequestException(String message) {
    super(message);
  }
}