package de.jjakobus.emailrestservice.model.dtos;

import de.jjakobus.emailrestservice.model.EmailState;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Representation of a new draft email that should get inserted. New emails do not have an ID yet.
 * State is "DRAFT" always.
 *
 * @param from sender address
 * @param to receiver addresses
 * @param cc "carbon copy" receiver addresses
 * @param subject subject of the email
 * @param body content
 * @author jjakobus
 */
public record InsertDraftEmailDto(
    EmailAddressDto from,
    List<EmailAddressDto> to,
    List<EmailAddressDto> cc,
    String subject,
    String body) {

  public InsertDraftEmailDto(
      EmailAddressDto from,
      List<EmailAddressDto> to,
      List<EmailAddressDto> cc,
      String subject,
      String body) {
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.subject = requireNonNull(subject, "subject must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
  }

  /**
   * Returns the state of the email, which is "DRAFT" all the time.
   *
   * @return state "DRAFT"
   */
  public EmailState state() {
    return EmailState.DRAFT;
  }
}
