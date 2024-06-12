package de.jjakobus.emailrestservice.model;

import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents a single email.
 *
 * @author jjakobus
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Email {

  /** ID of email. */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  /** The current state of the email. */
  @NotNull
  @Enumerated(EnumType.STRING)
  private EmailState state;

  /** The sender of the email */
  @NotNull
  @Embedded
  private EmailAddress from;

  /** The main receivers of the email. */
  @NotNull
  @ElementCollection
  private List<EmailAddress> to;

  /** The "carbon copy" receivers of the mail. */
  @NotNull
  @ElementCollection
  private List<EmailAddress> cc;

  /** Subject of the email (can be empty). */
  @NotNull
  private String subject;

  /** Body (the main content) of the mail (can be empty). */
  @NotNull
  private String body;

  /** The date the email was modified last. */
  @NotNull
  private LocalDateTime modifiedDate;


  /* constructors */

  protected Email() {
    // Required by JPA.
  }

  /**
   * Creates a new email with given information.
   *
   * @param state current state
   * @param from sender
   * @param to receivers (can be an empty list)
   * @param cc cc receivers (can be an empty list)
   * @param subject subject of the email (can be an empty string)
   * @param body main content (can be an empty string)
   * @param modifiedDate date of last modification
   */
  public Email(
      EmailState state,
      EmailAddress from,
      List<EmailAddress> to,
      List<EmailAddress> cc,
      String subject,
      String body,
      LocalDateTime modifiedDate) {
    this.state = requireNonNull(state, "state must not be null.");
    this.from = requireNonNull(from, "from must not be null.");
    this.to = requireNonNull(to, "to must not be null.");
    this.cc = requireNonNull(cc, "cc must not be null.");
    this.subject = requireNonNull(subject, "subject must not be null.");
    this.body = requireNonNull(body, "body must not be null.");
    this.modifiedDate = requireNonNull(modifiedDate, "modifiedDate must not be null.");
  }

  /**
   * Creates a new {@link EmailDto} with information from this {@link Email} entity. DTO captures the entities' current
   * state and is equal (information-wise). Updates to the entity are not reflected by the DTO.
   *
   * @return new DTO instance
   */
  public EmailDto toDto() {

    return new EmailDto(
        id,
        state,
        from.toDto(),
        to.stream().map(EmailAddress::toDto).toList(),
        cc.stream().map(EmailAddress::toDto).toList(),
        subject,
        body,
        modifiedDate);
  }

  /* getter + setter methods. */

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public EmailState getState() {
    return state;
  }

  public void setState(EmailState state) {
    this.state = requireNonNull(state, "state must not be null.");
  }

  public EmailAddress getFrom() {
    return from;
  }

  public void setFrom(EmailAddress from) {
    this.from = requireNonNull(from, "from must not be null.");
  }

  public List<EmailAddress> getTo() {
    return to;
  }

  public void setTo(List<EmailAddress> to) {
    this.to = requireNonNull(to, "to must not be null.");
  }

  public List<EmailAddress> getCc() {
    return cc;
  }

  public void setCc(List<EmailAddress> cc) {
    this.cc = requireNonNull(cc, "cc must not be null.");
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = requireNonNull(subject, "subject must not be null.");
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = requireNonNull(body, "body must not be null.");
  }

  public LocalDateTime getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(LocalDateTime modifiedDate) {
    this.modifiedDate = requireNonNull(modifiedDate, "modifiedDate must not be null.");
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
    Email email = (Email) o;
    return state == email.state
        && Objects.equals(from, email.from)
        && Objects.equals(to, email.to)
        && Objects.equals(cc, email.cc)
        && Objects.equals(body, email.body)
        && Objects.equals(modifiedDate, email.modifiedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, from, to, cc, body, modifiedDate);
  }

  @Override
  public String toString() {
    return "Email{" +
        "id=" + id +
        ", state=" + state +
        ", from=" + from +
        ", to=" + to +
        ", cc=" + cc +
        ", body='" + body + '\'' +
        ", modifiedDate=" + modifiedDate +
        '}';
  }
}
