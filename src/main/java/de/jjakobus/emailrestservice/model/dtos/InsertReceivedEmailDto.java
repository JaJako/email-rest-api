package de.jjakobus.emailrestservice.model.dtos;

import de.jjakobus.emailrestservice.model.EmailState;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Representation of a new received email that should get inserted. Received emails already have a date which gets used
 * as creation (and modified) date. But no ID is present because those are unique to this server, only.
 * State is "SENT" always.
 *
 * @param from sender address
 * @param to receiver addresses
 * @param cc "carbon copy" receiver addresses
 * @param subject subject of the email
 * @param body content
 * @param modifiedDate date of the email received (= last modification date)
 * @author jjakobus
 */
public record InsertReceivedEmailDto(
    EmailAddressDto from,
    List<EmailAddressDto> to,
    List<EmailAddressDto> cc,
    String subject,
    String body,
    LocalDateTime modifiedDate) {

  public InsertReceivedEmailDto(
      EmailAddressDto from,
      List<EmailAddressDto> to,
      List<EmailAddressDto> cc,
      String subject,
      String body,
      LocalDateTime modifiedDate) {
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.subject = requireNonNull(subject, "subject must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
    this.modifiedDate = requireNonNull(modifiedDate, "modifiedDate must not be null.");
  }

  /**
   * Returns the state of the email, which is "SENT" all the time.
   *
   * @return state "SENT"
   */
  public EmailState state() {
    return EmailState.SENT;
  }
}
