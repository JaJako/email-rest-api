package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Analyses stored emails and classifies individual mails as SPAM based on set filters. Service is scheduled to run
 * every day at 10:00. Current implementation allows to set email addresses (sender) as filter only.
 *
 * @author jjakobus
 */
@Service
public class EmailSpamFilterService {

  /** Repository of emails. */
  private final EmailRepository emailRepository;

  /** Spam filters for sender email. */
  private final Set<EmailAddress> filteredEmails;

  /**
   * Creates a new service for managing stored emails.
   *
   * @param emailRepository repository of emails
   */
  @Autowired
  public EmailSpamFilterService(EmailRepository emailRepository) {
    this.emailRepository = requireNonNull(emailRepository, "emailRepository must not be null.");
    filteredEmails = new HashSet<>();
  }

  /**
   * Scans all stored emails and classifies those as SPAM that are matching set filters.
   */
  @Scheduled(cron = "${email-rest-service.spam-filter-cron}")
  public void classifySpamEmails() {
    // Go through filters (email addresses).
    List<Email> filteredSpamEmails = filteredEmails.stream()
        .map(EmailAddress::getAddress)
        // Collect matching stored emails (list each).
        .map(emailRepository::findAllByFrom_Address)
        .flatMap(Collection::stream)
        // Look at SENT emails only (not at deleted, draft or already spam).
        .filter(email -> email.getState() == EmailState.SENT)
        .toList();

    // Mark all spam mails as SPAM.
    filteredSpamEmails.forEach(email -> email.setState(EmailState.SPAM));

    // Save = update spam emails.
    emailRepository.saveAll(filteredSpamEmails);
  }

  /**
   * Adds a new email address to be filtered as SPAM.
   *
   * @param emailAddress new filter email address
   */
  public void addFilterAddress(EmailAddress emailAddress) {
    filteredEmails.add(emailAddress);
  }
}
