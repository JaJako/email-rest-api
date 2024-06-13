package de.jjakobus.emailrestservice.service.repositories;

import de.jjakobus.emailrestservice.model.Email;
import org.springframework.data.repository.CrudRepository;


/**
 * Stores and manages emails using JPA repository connected with database.
 *
 * @author jjakobus
 */
public interface EmailRepository extends CrudRepository<Email, Long> {

}
