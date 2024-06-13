package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.model.exceptions.EmailNotFoundException;
import de.jjakobus.emailrestservice.model.exceptions.EmailUpdateNotAllowedException;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Handles all store-related operations regarding emails.
 *
 * @author jjakobus
 */
@Service
public class EmailStoreService {

  /* Exception messages. */
  private static final String MSG_NO_EMAIL_WITH_ID = "There is no email with id '%s'.";
  private static final String MSG_UPDATE_NOT_ALLOWED = "Update of email (id: %s) is not allowed, reason %s.";

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
    requireNonNull(newEmail, "newEmail must not be null.");

    Email newEmailEntity = createEmailEntityFromNewEmail(newEmail);
    Email insertedEmailEntity = emailRepository.save(newEmailEntity);
    return insertedEmailEntity.toDto();
  }

  /**
   * Creates a new {@link Email} entity containing all information from given new email {@link InsertEmailDto}.
   *
   * @param newEmail new email to create entity for
   * @return email entity
   */
  private static Email createEmailEntityFromNewEmail(InsertEmailDto newEmail) {

    return new Email(
        newEmail.state(),
        createEmailAddressEntityFrom(newEmail.from()),
        newEmail.to().stream()
            .map(EmailStoreService::createEmailAddressEntityFrom)
            .toList(),
        newEmail.cc().stream()
            .map(EmailStoreService::createEmailAddressEntityFrom)
            .toList(),
        newEmail.subject(),
        newEmail.body(),
        newEmail.modifiedDate()
    );
  }

  /**
   * Creates a new {@link EmailAddress} entity containing all information from given address Dto
   * {@link EmailAddressDto}.
   *
   * @param addressDto address to create entity for
   * @return address entity
   */
  private static EmailAddress createEmailAddressEntityFrom(EmailAddressDto addressDto) {

    return new EmailAddress(
        addressDto.address(),
        addressDto.displayName());
  }

  /**
   * Saves all given new emails to the email store and returns all successfully stored emails as {@link EmailDto}.
   * As specified by Spring JPA repository, there can be changes made to returned emails compared to given ones!
   *
   * @param newEmails emails to save
   * @return saved emails
   */
  public List<EmailDto> saveEmails(List<InsertEmailDto> newEmails) {
    requireNonNull(newEmails, "newEmails must not be null.");

    List<Email> newEmailEntities = newEmails.stream()
        .map(EmailStoreService::createEmailEntityFromNewEmail)
        .toList();
    Iterable<Email> insertedEmailEntities = emailRepository.saveAll(newEmailEntities);

    return emailEntitiesToDtos(insertedEmailEntities);
  }

  /**
   * Maps given {@link Iterable} of {@link Email} entities to DTO type {@link EmailDto} and returns them as a List.
   *
   * @param emailEntities email entities to map
   * @return list of mapped email DTOs
   */
  private List<EmailDto> emailEntitiesToDtos(Iterable<Email> emailEntities) {

    return StreamSupport.stream(emailEntities.spliterator(), false)
        .map(Email::toDto)
        .toList();
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

    return emailRepository.findEmailById(id)
        .orElseThrow(() -> new EmailNotFoundException(
            String.format(MSG_NO_EMAIL_WITH_ID, id)));
  }

  /**
   * Returns the emails that are stored with the given ids. Not found emails are ignored, so result List can be empty.
   *
   * @param ids ids to search
   * @return matched emails, can be empty
   */
  public List<EmailDto> getEmails(List<Long> ids) {
    requireNonNull(ids, "ids must not be null.");

    Iterable<Email> matchedEmailEntities = emailRepository.findAllById(ids);
    return emailEntitiesToDtos(matchedEmailEntities);
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
    requireNonNull(updatedEmail, "updatedEmail must not be null.");

    Email emailEntity = emailRepository.findById(id)
        .orElseThrow(() -> new EmailNotFoundException(
            String.format(MSG_NO_EMAIL_WITH_ID, id)));

    // Check update is allowed (throws exception if not).
    checkUpdateAllowed(emailEntity, updatedEmail);

    emailEntity.setState(updatedEmail.state());
    emailEntity.setFrom(
        createEmailAddressEntityFrom(updatedEmail.from()));
    emailEntity.setTo(
        updatedEmail.to().stream()
            .map(EmailStoreService::createEmailAddressEntityFrom)
            .toList());
    emailEntity.setCc(
        updatedEmail.cc().stream()
            .map(EmailStoreService::createEmailAddressEntityFrom)
            .toList());
    emailEntity.setSubject(updatedEmail.subject());
    emailEntity.setBody(updatedEmail.body());
    emailEntity.setModifiedDate(updatedEmail.modifiedDate());

    // Save = update entity.
    emailRepository.save(emailEntity);
  }

  /**
   * Checks whether updating given original email with updated email is allowed or not. Rules:
   * 1) ID must not be updated in any case
   * 2) state of DRAFT emails can be changed to SENT (or be unchanged)
   * 3) content of DRAFT emails can be updated (no state change)
   * 4) state of non-draft emails can be changed between DELETED, SPAM, SENT (but not DRAFT)
   * 5) content of non-draft emails must not be changed
   * Throws {@link EmailUpdateNotAllowedException}, if update is not allowed.
   *
   * @param origEmail original email
   * @param updatedEmail updated email
   * @throws EmailUpdateNotAllowedException if update is not allowed
   */
  private static void checkUpdateAllowed(
      Email origEmail,
      EmailDto updatedEmail
  ) throws EmailUpdateNotAllowedException {

    // 1) Check ID is unchanged.
    if (origEmail.getId() != updatedEmail.id()) {
      throw new EmailUpdateNotAllowedException(
          String.format(MSG_UPDATE_NOT_ALLOWED, origEmail.getId(), "changed id"));

    }

    // Check if DRAFT email.
    if (origEmail.getState() == EmailState.DRAFT) {

      // 2) Check state of updated mail is DRAFT (unchanged) or SENT.
      if (updatedEmail.state() != EmailState.DRAFT
          && updatedEmail.state() != EmailState.SENT) {
        throw new EmailUpdateNotAllowedException(
            String.format(MSG_UPDATE_NOT_ALLOWED, origEmail.getId(), "DRAFT email to other than DRAFT or SENT"));

      }

      // 3) Check content change only in state DRAFT (state changed to SENT).
      if (updatedEmail.state() != EmailState.DRAFT
          && haveDifferentContent(origEmail, updatedEmail)) {
        throw new EmailUpdateNotAllowedException(
            String.format(MSG_UPDATE_NOT_ALLOWED, origEmail.getId(), "no content change on DRAFT email to SENT"));

      }

    } else {
      // Non-draft email.

      // 4) Check state of updated mail is anything but DRAFT.
      if (updatedEmail.state() == EmailState.DRAFT) {
        throw new EmailUpdateNotAllowedException(
            String.format(MSG_UPDATE_NOT_ALLOWED, origEmail.getId(), "non-DRAFT email to DRAFT"));

      }

      // 5) Check all fields except state are unchanged (exception on changed content).
      if (haveDifferentContent(origEmail, updatedEmail)) {
        throw new EmailUpdateNotAllowedException(
            String.format(MSG_UPDATE_NOT_ALLOWED, origEmail.getId(), "non-DRAFT changed content"));

      }
    }

  }

  /**
   * Checks whether original email (entity) and updated email (DTO) have different content. All fields are relevant
   * except state of the mail.
   *
   * @param origEmail original email
   * @param updatedEmail updated email
   * @return whether emails are unequal content-wise
   */
  private static boolean haveDifferentContent(Email origEmail, EmailDto updatedEmail) {
    // To compare content of emails temporarily override orig state with updated state.
    // This way equals will return false, if any other information has changed (not allowed).

    // Get DTO of that entity to compare, reset state of original entity afterward.
    EmailState origState = origEmail.getState();
    origEmail.setState(updatedEmail.state());

    EmailDto origEmailDto = origEmail.toDto();
    origEmail.setState(origState);

    return !origEmailDto.equals(updatedEmail);
  }

  /**
   * Deletes the stored email with given id. If there is no email stored with specified id, an
   * {@link EmailNotFoundException} gets raised.
   *
   * @param id id of email to delete
   * @throws EmailNotFoundException no email with given id
   */
  public void deleteEmail(long id) throws EmailNotFoundException {
    // Check if an email of with given ID exists.
    if (emailRepository.existsById(id)) {
      emailRepository.deleteById(id);

    } else {
      // If no email with ID, throw exception.
      throw new EmailNotFoundException(
          String.format(MSG_NO_EMAIL_WITH_ID, id));

    }
  }

  /**
   * Deletes all stored emails with given ids. Emails not found by some given ids, are ignored.
   *
   * @param ids ids of emails to delete
   */
  public void deleteEmails(List<Long> ids) {
    requireNonNull(ids, "ids must not be null.");

    emailRepository.deleteAllById(ids);
  }
}
