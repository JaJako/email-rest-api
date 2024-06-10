package de.jjakobus.emailrestservice.model;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * {@link Email} that is stored in the service's database.
 *
 * @author jjakobus
 */
public class StoredEmail extends Email {

  /** ID of stored email. */
  private final int id;

  /**
   * Creates a new stored Email.
   *
   * @param id id in store
   * @param from sender
   * @param to receivers (can be an empty list)
   * @param cc cc receivers (can be an empty list)
   * @param state current state
   * @param date creation/modification date
   * @param body main content (can be an empty string)
   */
  public StoredEmail(
      int id,
      EmailAddress from,
      List<EmailAddress> to,
      List<EmailAddress> cc,
      EmailState state,
      Date date,
      String body
  ) {
    super(from, to, cc, state, date, body);

    this.id = id;
  }

  public static StoredEmail from(Email email, int id) {
    return new StoredEmail(
        id,
        email.getFrom(),
        email.getTo(),
        email.getCc(),
        email.getState(),
        email.getDate(),
        email.getBody());
  }

  /* Getter */

  /**
   * Returns the ID of the stored email.
   *
   * @return ID
   */
  public int getId() {
    return id;
  }

  /* Equals, hashCode, toString */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StoredEmail that = (StoredEmail) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "StoredEmail{" +
        "id=" + id +
        '}';
  }
}
