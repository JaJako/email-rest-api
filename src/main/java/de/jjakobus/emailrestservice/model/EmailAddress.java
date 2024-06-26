package de.jjakobus.emailrestservice.model;

import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents an address that can be used to send and receive emails.
 *
 * @author jjakobus
 */
@Embeddable
public class EmailAddress {

  /** The unique email address. */
  @NotNull
  private String address;

  /**
   * More descriptive name to display, can be null.
   * E.g. "Peter Mueller <peter.mueller(a)gmx.de>".
   */
  private String displayName;

  /* constructors */

  protected EmailAddress() {
    // Required by JPA.
  }

  /**
   * Creates a new email address.
   *
   * @param address unique email address
   * @param displayName more descriptive name to display (can be null)
   */
  public EmailAddress(
      String address,
      @Nullable String displayName) {
    this.address = requireNonNull(address, "address must not be null.");
    this.displayName = displayName;
  }

  /**
   * Creates a new {@link EmailAddressDto} with information from this {@link EmailAddress} entity. DTO captures the
   * entities' current state and is equal (information-wise). Updates to the entity are not reflected by the DTO.
   *
   * @return new DTO instance
   */
  public EmailAddressDto toDto() {

    return new EmailAddressDto(
        address,
        displayName);
  }

  /* getter + setter */

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = requireNonNull(address, "address must not be null.");
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(@Nullable String displayName) {
    this.displayName = displayName;
  }

  /* equals, hashCode, toString */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmailAddress that = (EmailAddress) o;
    return Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(address);
  }

  @Override
  public String toString() {
    return displayName != null
        ? displayName
        : address;
  }
}
