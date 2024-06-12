package de.jjakobus.emailrestservice.service.repositories;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


/**
 * Stores and manages emails using JPA repository connected with database.
 *
 * @author jjakobus
 */
public interface EmailRepository extends CrudRepository<Email, Long> {

  /*
   * Custom query definitions that use EmailDto (the DTO record) as return type.
   * Because entity "Email" and record "EmailDto" have the same fields, CrudRepository implicitly maps them.
   */

  /**
   * Returns the stored email with given ID, if there is a matching email.
   * Otherwise, an empty Optional gets returned.
   *
   * @param id id to search for
   * @return email with id, else empty Optional
   */
  Optional<EmailDto> findEmailById(long id);
}
