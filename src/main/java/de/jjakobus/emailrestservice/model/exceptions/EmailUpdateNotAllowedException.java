package de.jjakobus.emailrestservice.model.exceptions;

/**
 * Exception when an update of an email is not allowed, e.g. it is no draft.
 *
 * @author jjakobus
 */
public class EmailUpdateNotAllowedException extends Exception {

  public EmailUpdateNotAllowedException() {
  }

  public EmailUpdateNotAllowedException(String message) {
    super(message);
  }

  public EmailUpdateNotAllowedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String toString() {
    return "EmailUpdateNotAllowedException{} " + super.toString();
  }
}
