package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Stores and manages email addresses using JPA repository connected with database.
 *
 * @author jjakobus
 */
public interface EmailAddressRepositoryService extends CrudRepository<EmailAddress, String> {

  /*
   * Custom query definitions that use EmailAddressDto (the DTO record) as return type.
   * Because entity "EmailAddress" and record "EmailAddressDto" have the same fields,
   * CrudRepository implicitly maps them.
   */

  /**
   * Returns the stored email address by its full address, if there is a matching address.
   * Otherwise, an empty Optional gets returned.
   *
   * @param address email address to search for
   * @return searched email address, else empty Optional
   */
  Optional<EmailAddressDto> findByAddress(String address);
}
