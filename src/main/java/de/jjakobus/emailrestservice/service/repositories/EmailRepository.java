package de.jjakobus.emailrestservice.service.repositories;

import de.jjakobus.emailrestservice.model.Email;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


/**
 * Stores and manages emails using JPA repository connected with database.
 *
 * @author jjakobus
 */
public interface EmailRepository extends CrudRepository<Email, Long> {

  /**
   * Searches for all emails with given email address as sender (in from).
   *
   * @param address sender email address
   * @return all emails with given from address
   */
  List<Email> findAllByFrom_Address(String address);
}
