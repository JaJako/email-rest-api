package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Stores and manages emails using JPA repository connected with database.
 *
 * @author jjakobus
 */
public interface EmailRepositoryService extends JpaRepository<Email, Long> {

}
