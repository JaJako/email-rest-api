package de.jjakobus.emailrestservice.model.dtos;

import de.jjakobus.emailrestservice.model.EmailState;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Representation of a new email that should get inserted. Those email do not have an ID yet.
 *
 * @param state state of the email
 * @param from sender address
 * @param to receiver addresses
 * @param cc "carbon copy" receiver addresses
 * @param subject subject of the email
 * @param body content
 * @param modifiedDate date of the email received (= last modification date)
 * @author jjakobus
 */
public record InsertEmailDto(
    EmailState state,
    EmailAddressDto from,
    List<EmailAddressDto> to,
    List<EmailAddressDto> cc,
    String subject,
    String body,
    Date modifiedDate) {

  public InsertEmailDto(
      EmailState state,
      EmailAddressDto from,
      List<EmailAddressDto> to,
      List<EmailAddressDto> cc,
      String subject,
      String body,
      Date modifiedDate) {
    this.state = requireNonNull(state, "state must not be null.");
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.subject = requireNonNull(subject, "subject must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
    this.modifiedDate = requireNonNull(modifiedDate, "modifiedDate must not be null.");
  }
}
