package de.jjakobus.emailrestservice.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents a single Email. Only mails' information with status DRAFT can be modified!
 *
 * @author jjakobus
 */
public class Email {

  /** The sender of the email */
  private final EmailAddress from;

  /** The main receivers of the email. */
  private final List<EmailAddress> to;

  /** The "carbon copy" receivers of the mail. */
  private final List<EmailAddress> cc;

  /** Body (the main content) of the mail. */
  private final String body;

  /**
   * The date the email's content was created/modified last.
   * A change of the state is not represented in the date.
   */
  private final Date date;

  /** The current state of the email. */
  private EmailState state;

  /**
   * Creates a new email with given information.
   *
   * @param from sender
   * @param to receivers (can be an empty list)
   * @param cc cc receivers (can be an empty list)
   * @param state current state
   * @param date creation/modification date
   * @param body main content (can be an empty string)
   */
  public Email(
      EmailAddress from,
      List<EmailAddress> to,
      List<EmailAddress> cc,
      EmailState state,
      Date date,
      String body
  ) {
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.state = requireNonNull(state, "state must not be null.");
    this.date = requireNonNull(date, "date must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
  }

  /**
   * Creates a "minimal" email with empty receivers (main, cc) and empty body.
   *
   * @param from sender
   * @param state current state
   * @param date creation/modification date
   */
  public Email(
      EmailAddress from,
      EmailState state,
      Date date
  ) {
    this(from, new ArrayList<>(), new ArrayList<>(), state, date, "");
  }

  /**
   * Changes the Email's state to given new state.
   *
   * @param newState new state
   */
  public void changeState(EmailState newState) {
    state = requireNonNull(newState, "newState must not be null.");
  }

  /* GETTER methods. */

  public EmailAddress getFrom() {
    return from;
  }

  public List<EmailAddress> getTo() {
    return to;
  }

  public List<EmailAddress> getCc() {
    return cc;
  }

  public String getBody() {
    return body;
  }

  public Date getDate() {
    return date;
  }

  public EmailState getState() {
    return state;
  }

  /* equals, hashCode, toString */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    // Use instanceof because a "StoredEmail" should be comparable to an non-stored "Email".
    if (!(o instanceof Email email)) {
      return false;
    }
    return Objects.equals(from, email.from)
        && Objects.equals(to, email.to)
        && Objects.equals(cc, email.cc)
        && Objects.equals(body, email.body)
        && Objects.equals(date, email.date)
        && state == email.state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to, cc, body, date, state);
  }

  @Override
  public String toString() {
    return "Email{" +
        "from=" + from +
        ", to=" + to +
        ", cc=" + cc +
        ", body='" + body + '\'' +
        ", date=" + date +
        ", state=" + state +
        '}';
  }
}
