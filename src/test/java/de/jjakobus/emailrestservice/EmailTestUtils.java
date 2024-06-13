package de.jjakobus.emailrestservice;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;

import java.util.Date;
import java.util.List;

/**
 * Utilities for tests in context of emails.
 *
 * @author jjakobus
 */
public class EmailTestUtils {

  private EmailTestUtils() {
    // Disallow creation by implementing private constructor.
    throw new AssertionError("Class must not be instantiated.");
  }

  /**
   * Checks whether given {@link EmailDto} contains all the information given by the {@link InsertEmailDto}.
   *
   * @param email email to check
   * @param insertDto inserted email
   * @return whether email contains all information
   */
  public static boolean containsAllInformationFromInsertDto(
      EmailDto email,
      InsertEmailDto insertDto) {

    return insertDto.state().equals(email.state())
        && insertDto.from().equals(email.from())
        && insertDto.to().equals(email.to())
        && insertDto.cc().equals(email.cc())
        && insertDto.subject().equals(email.subject())
        && insertDto.body().equals(email.body())
        && insertDto.modifiedDate().equals(email.modifiedDate());
  }

  /**
   * Creates example {@link InsertEmailDto} that matches {@link #createExampleEmailEntity(long)} and
   * {@link #createExampleEmail(long)} example information-wise.
   *
   * @return example insert email
   */
  public static InsertEmailDto createExampleInsertEmail() {

    return new InsertEmailDto(
        EmailState.DRAFT,
        new EmailAddressDto("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>"),
        List.of(
            new EmailAddressDto("juergen.vogel@web.de", null),
            new EmailAddressDto("hans-peter@gmail.com", "Hans Peter")),
        List.of(new EmailAddressDto("peter.lustig@gmail.com", "Peter Lustig")),
        "Löwenzahn",
        "Planung neuer Sendung",
        new Date(42));
  }

  /**
   * Creates example {@link Email} entity that matches {@link #createExampleInsertEmail()} and
   * {@link #createExampleEmail(long)} example information-wise.
   *
   * @param id id to use
   * @return example email entity
   */
  public static Email createExampleEmailEntity(long id) {

    Email email = new Email(
        EmailState.DRAFT,
        new EmailAddress("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>"),
        List.of(
            new EmailAddress("juergen.vogel@web.de", null),
            new EmailAddress("hans-peter@gmail.com", "Hans Peter")),
        List.of(new EmailAddress("peter.lustig@gmail.com", "Peter Lustig")),
        "Löwenzahn",
        "Planung neuer Sendung",
        new Date(42));
    // Override generated id.
    email.setId(id);

    return email;
  }

  /**
   * Creates example {@link EmailDto} that matches {@link #createExampleInsertEmail()} and
   * {@link #createExampleEmailEntity(long)} example information-wise.
   *
   * @param id id of email
   * @return example email
   */
  public static EmailDto createExampleEmail(long id) {

    return new EmailDto(
        id,
        EmailState.DRAFT,
        new EmailAddressDto("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>"),
        List.of(
            new EmailAddressDto("juergen.vogel@web.de", null),
            new EmailAddressDto("hans-peter@gmail.com", "Hans Peter")),
        List.of(new EmailAddressDto("peter.lustig@gmail.com", "Peter Lustig")),
        "Löwenzahn",
        "Planung neuer Sendung",
        new Date(42));
  }
}
