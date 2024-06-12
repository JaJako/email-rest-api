package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.model.exceptions.EmailNotFoundException;
import de.jjakobus.emailrestservice.model.exceptions.EmailUpdateNotAllowedException;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Handles all store-related operations regarding emails.
 *
 * @author jjakobus
 */
@Service
public class EmailStoreService {

  /** Repository of emails. */
  private final EmailRepository emailRepository;

  /**
   * Creates a new service for managing stored emails.
   *
   * @param emailRepository repository of emails
   */
  @Autowired
  public EmailStoreService(EmailRepository emailRepository) {
    this.emailRepository = requireNonNull(emailRepository, "emailRepository must not be null.");
  }

  /**
   * Saves the given new email to the email store and returns the stored email as {@link EmailDto}.
   * As specified by Spring JPA repository, there can be changes made to returned email compared to given one!
   *
   * @param newEmail email to save
   * @return saved email
   */
  public EmailDto saveEmail(InsertEmailDto newEmail) {
    return null; // TODO
  }

  /**
   * Saves all given new emails to the email store and returns all successfully stored emails as {@link EmailDto}.
   * As specified by Spring JPA repository, there can be changes made to returned emails compared to given ones!
   *
   * @param newEmails emails to save
   * @return saved emails
   */
  public List<EmailDto> saveEmails(List<InsertEmailDto> newEmails) {
    return null; // TODO
  }

  /**
   * Returns the email that is stored with the given id. If no email with that id is stored, an
   * {@link EmailNotFoundException} gets raised.
   *
   * @param id id to search
   * @return stored email with id, else empty optional
   * @throws EmailNotFoundException no email with given id
   */
  public EmailDto getEmail(long id) throws EmailNotFoundException {
    throw new EmailNotFoundException(""); // TODO
  }

  /**
   * Returns the emails that are stored with the given ids. Not found emails are ignored, so result List can be empty.
   *
   * @param ids ids to search
   * @return matched emails, can be empty
   */
  public List<EmailDto> getEmails(List<Long> ids) {
    return List.of(); // TODO
  }

  /**
   * Updates the stored email with given id with the updated email's version. If there is no email stored with specified
   * id, an {@link EmailNotFoundException} gets raised. If email is no DRAFT and updated email changes more than the
   * state, {@link EmailUpdateNotAllowedException} gets raised.
   *
   * @param id id of email to update
   * @param updatedEmail updated email's version
   * @throws EmailNotFoundException no email with given id
   * @throws EmailUpdateNotAllowedException given email must not be updated
   */
  public void updateEmail(
      long id,
      EmailDto updatedEmail
  ) throws EmailNotFoundException, EmailUpdateNotAllowedException {

    throw new EmailNotFoundException(""); // TODO
  }

  /**
   * Deletes the stored email with given id. If there is no email stored with specified id, an
   * {@link EmailNotFoundException} gets raised.
   *
   * @param id id of email to delete
   * @throws EmailNotFoundException no email with given id
   */
  public void deleteEmail(long id) throws EmailNotFoundException {
    throw new EmailNotFoundException(""); // TODO
  }

  /**
   * Deletes all stored emails with given ids. Emails not found by some given ids, are ignored.
   *
   * @param ids ids of emails to delete
   */
  public void deleteEmails(List<Long> ids) {
    return; // TODO
  }
}
