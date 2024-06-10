package de.jjakobus.emailrestservice.model;

import static java.util.Objects.requireNonNull;

/**
 * Represents an address that can be used to send and receive emails.
 *
 * @param name the pre-"@" part
 * @param domain the post-"@" part
 * @param displayName more descriptive name to display (e.g. "Peter Mueller <peter.mueller(a)gmx.net>").
 * @author jjakobus
 */
public record EmailAddress(
    String name,
    String domain,
    String displayName
) {

  /** Format of an email address. */
  private static final String EMAIL_ADDRESS_FORMAT = "%s@%s";

  public EmailAddress(
      String name,
      String domain,
      String displayName) {
    this.name = requireNonNull(name, "name must not be null.");
    this.domain = requireNonNull(domain, "domain must not be null.");
    this.displayName = requireNonNull(displayName, "displayName must not be null.");
  }

  /**
   * Returns the full email address as a string.
   *
   * @return full email address
   */
  public String toFullAddress() {
    return String.format(EMAIL_ADDRESS_FORMAT, name, domain);
  }
}
