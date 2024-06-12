package de.jjakobus.emailrestservice.model.dtos;

import de.jjakobus.emailrestservice.model.EmailState;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represent an (already stored) email in context of DTOs.
 *
 * @param id ID of email
 * @param state the current state of the email
 * @param from the sender of the email
 * @param to the main receivers of the email
 * @param cc the "carbon copy" receivers of the mail
 * @param subject subject of the email (can be empty)
 * @param body body (the main content) of the mail (can be empty)
 * @param modifiedDate the date the email was modified last
 * @author jjakobus
 */
public record EmailDto(
    long id,
    EmailState state,
    EmailAddressDto from,
    List<EmailAddressDto> to,
    List<EmailAddressDto> cc,
    String subject,
    String body,
    LocalDateTime modifiedDate) {

  public EmailDto(
      long id,
      EmailState state,
      EmailAddressDto from,
      List<EmailAddressDto> to,
      List<EmailAddressDto> cc,
      String subject,
      String body,
      LocalDateTime modifiedDate) {
    this.id = id;
    this.state = requireNonNull(state, "state must not be null.");
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.subject = requireNonNull(subject, "subject must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
    this.modifiedDate = requireNonNull(modifiedDate, "modifiedDate must not be null.");
  }
}
