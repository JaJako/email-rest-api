package de.jjakobus.emailrestservice.model.exceptions;

/**
 * Exception when no stored email matches given id.
 *
 * @author jjakobus
 */
public class EmailNotFoundException extends Exception {

  public EmailNotFoundException() {
  }

  public EmailNotFoundException(String message) {
    super(message);
  }

  public EmailNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String toString() {
    return "EmailNotFoundException{} " + super.toString();
  }
}
