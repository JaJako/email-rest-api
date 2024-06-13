package de.jjakobus.emailrestservice;

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

    return insertDto.state().equals(EmailState.SENT)
        && insertDto.from().equals(email.from())
        && insertDto.to().equals(email.to())
        && insertDto.cc().equals(email.cc())
        && insertDto.subject().equals(email.subject())
        && insertDto.body().equals(email.body())
        && insertDto.modifiedDate().equals(email.modifiedDate());
  }

  /**
   * Creates example {@link InsertEmailDto} that matches {@link #createExampleEmail(int)} example information-wise.
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
   * Creates example {@link EmailDto} that matches {@link #createExampleInsertEmail()} example information-wise.
   *
   * @param id id of email
   * @return example email
   */
  public static EmailDto createExampleEmail(int id) {

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
