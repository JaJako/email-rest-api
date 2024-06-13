package de.jjakobus.emailrestservice.model.dtos;

import org.springframework.lang.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Represents an email address in context of DTOs.
 *
 * @param address full email address
 * @param displayName more descriptive name to display, can be null. E.g. "Peter Mueller <peter.mueller(a)gmx.de>".
 * @author jjakobus
 */
public record EmailAddressDto(
    String address,
    String displayName) {

  public EmailAddressDto(
      String address,
      @Nullable String displayName) {
    this.address = requireNonNull(address, "address must not be null.");
    this.displayName = displayName;
  }
}
