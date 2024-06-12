package de.jjakobus.emailrestservice.controller;

import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.model.exceptions.EmailNotFoundException;
import de.jjakobus.emailrestservice.model.exceptions.EmailUpdateNotAllowedException;
import de.jjakobus.emailrestservice.service.EmailStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Defines CRUD endpoints of the Email REST API and delegates the tasks to responsible service(s).
 *
 * @author jjakobus
 */
@RestController()
@RequestMapping("${email-rest-service.request-path}")
public class EmailRestServiceController {

  /** Service managing emails store. */
  private final EmailStoreService emailStore;

  /**
   * Creates a new controller to handle REST requests.
   *
   * @param emailStore service managing emails store
   */
  @Autowired
  public EmailRestServiceController(EmailStoreService emailStore) {
    this.emailStore = requireNonNull(emailStore, "emailStore must not be null.");
  }

  /**
   * Inserts given email into store and returns stored email if successful.
   *
   * @param newEmail new email
   * @return stored email
   */
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(path = "/insert")
  public EmailDto handleInsertEmail(
      @RequestBody InsertEmailDto newEmail) {

    return emailStore.saveEmail(newEmail);
  }

  /**
   * Inserts a list of new emails into store and returns all emails stored successfully.
   *
   * @param newEmails new emails
   * @return stored emails
   */
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(path = "/insert", params = "bulk")
  public List<EmailDto> handleBulkInsertEmail(
      @RequestBody List<InsertEmailDto> newEmails) {

    return emailStore.saveEmails(newEmails);
  }

  /**
   * Returns the email matching given id. If there is no matching email, NOT_FOUND status gets returned.
   *
   * @param id searched id
   * @return matching email, if present; else NOT_FOUND
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/query")
  public EmailDto handleQueryEmailById(@RequestParam long id) {

    EmailDto matchedEmail;
    try {
      matchedEmail = emailStore.getEmail(id);

    } catch (EmailNotFoundException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No email found matching id '" + id + "'.",
          e);

    }

    return matchedEmail;
  }

  /**
   * Returns the emails matching given ids. If for some ids no email is found, returned list will be shorter than list
   * of ids. If no email gets found, an empty list gets returned.
   *
   * @param ids searched ids
   * @return list of all found emails (can be shorter than given ids)
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/query", params = "bulk")
  public List<EmailDto> handleBulkQueryEmailById(@RequestParam List<Long> ids) {

    return emailStore.getEmails(ids);
  }

  /**
   * Updates the email of given id with supplied updated email. If there is no matching email, NOT_FOUND status gets
   * returned.
   *
   * @param id id of email to update
   * @param updatedEmail updated email
   */
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(path = "/update/{id}")
  public void handleUpdateEmail(
      @PathVariable long id,
      @RequestBody EmailDto updatedEmail) {

    try {
      emailStore.updateEmail(id, updatedEmail);

    } catch (EmailNotFoundException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No email found to update with id '" + id + "'.",
          e);

    } catch (EmailUpdateNotAllowedException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Update of email not allowed, see details for reason.",
          e);

    }
  }

  /**
   * Deletes the email of given id. If there is no matching email, NOT_FOUND status gets returned.
   *
   * @param id id of email to delete
   */
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(path = "/delete/{id}")
  public void handleDeleteEmail(@PathVariable long id) {

    try {
      emailStore.deleteEmail(id);

    } catch (EmailNotFoundException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No email found to delete with id '" + id + "'.",
          e);
    }
  }

  /**
   * Deletes all emails of given ids. If for some ids no email is found, there will be no consequence.
   *
   * @param ids ids of emails to delete
   */
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(path = "/delete", params = "bulk")
  public void handleDeleteEmails(@RequestParam List<Long> ids) {

    emailStore.deleteEmails(ids);
  }
}
